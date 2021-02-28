package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.controller.registration.ConfirmationMailBuilder
import com.sinhro.songturn.backend.extentions.toFullUserInfo
import com.sinhro.songturn.backend.extentions.toPublicUserInfo
import com.sinhro.songturn.backend.extentions.toUserPojo
import com.sinhro.songturn.backend.filter.CustomUserDetails
import com.sinhro.songturn.backend.providers.JwtAuthProvider
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.backend.repository.UserRepository
import com.sinhro.songturn.backend.tables.pojos.ConfirmationToken as ConfirmationTokenPojo
import com.sinhro.songturn.backend.tables.pojos.Role as RolePojo
import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.PublicUserInfo
import com.sinhro.songturn.rest.model.RegisterDemoUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.AuthReqData
import com.sinhro.songturn.rest.request_response.AuthRespBody
import com.sinhro.songturn.rest.validation.ValidationResult
import com.sinhro.songturn.rest.validation.Validator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import java.lang.Exception
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*

@Component
class UserService @Autowired constructor(
        private val validator: Validator,
        private val passwordEncoder: PasswordEncoder,
        private val userRepository: UserRepository,
        private val jwtAuthProvider: JwtAuthProvider,
        private val emailSenderService: EmailSenderService,
        private val confirmationMailBuilder: ConfirmationMailBuilder
) {

    private val log  = LoggerFactory.getLogger(UserService::class.java)

    fun authorizeUser(
            authReqData: AuthReqData
    ): AuthRespBody {
        val user = findByAnyAndCheckPass(authReqData.login, authReqData.password)

        if (!user.isVerified)
            throw CommonException(CommonError(
                    ErrorCodes.AUTH_USER_NOT_VERIFIED,
                    "User not verified")
            )
        val token = jwtAuthProvider.generateToken(user.login)

        return AuthRespBody(user.toFullUserInfo(), token)
    }

    fun validateAndRegisterUser(
            registerUserInfo: RegisterUserInfo,
            shouldVerify: Boolean
    ): FullUserInfo {
        log.info("Got register user info to validate : $registerUserInfo")
        val validationErrors: Map<String, List<ValidationResult>> =
                validator
                        .validate(registerUserInfo)
                        .resultForErrorFields()
        if (validationErrors.isNotEmpty()) {
            throw CommonException(
                    CommonError(
                            ErrorCodes.REGISTER_FAILED,
                            "Register failed, fields not correct",
                            "There is some restrictions on user fields, check extra",
                            validationErrors
                    )
            )
        }

        validateUserInfo(registerUserInfo)

        val savedUser = initAndSave(registerUserInfo, !shouldVerify)
        if (shouldVerify) {
            var confToken: ConfirmationTokenPojo? = null
            try {
                confToken = ConfirmationTokenPojo(
                        null, UUID.randomUUID().toString(),
                        OffsetDateTime.now(ZoneOffset.UTC), savedUser.id
                )
                confToken = userRepository.saveConfirmationToken(confToken)

                emailSenderService.sendEmail(confirmationMailBuilder.createConfirmationMail(
                        savedUser.email, confToken.token
                ))
            } catch (e: Exception) {
                userRepository.removeUser(savedUser)
                confToken?.let { userRepository.removeConfirmationToken(it) }
                throw CommonException(CommonError(
                        ErrorCodes.REGISTER_FAILED), e)
            }
        }

        return savedUser.toFullUserInfo()
    }

    fun validateAndRegisterUser(
            registerDemoUserInfo: RegisterDemoUserInfo
    ): String {
        val validationErrors: Map<String, List<ValidationResult>> =
                validator
                        .validate(registerDemoUserInfo)
                        .resultForErrorFields()
        if (validationErrors.isNotEmpty()) {
            throw CommonException(
                    CommonError(
                            ErrorCodes.REGISTER_FAILED,
                            "Register failed, fields not correct",
                            "There is some restrictions on user fields, check extra",
                            validationErrors
                    )
            )
        }

        validateUserInfo(registerDemoUserInfo)

        val savedUser = initAndSave(registerDemoUserInfo)

        val token = jwtAuthProvider.generateToken(savedUser.login)

        return token

//        return savedUser.toFullUserInfo()
    }

    fun userLogout(){
        val user = currentUser()

        if (isUserDemo(user))
            removeUser(user)
        else{
            setUserOutRoom(user)
        }
    }


    private fun removeCurrentUser() {
        removeUser(currentUser())
    }

    private fun removeUser(user:UserPojo) {
        userRepository.removeUser(user)
    }

    private fun initAndSave(registerUserInfo: RegisterUserInfo, isVerified: Boolean): UserPojo {
        val userPojo = registerUserInfo.toUserPojo(
                passwordEncoder,
                userRepository.defaultRole().id,
                isVerified
        )

        return userRepository.saveUser(userPojo)
    }

    private fun initAndSave(registerDemoUserInfo: RegisterDemoUserInfo): UserPojo {
        val userPojo = registerDemoUserInfo.toUserPojo(
                userRepository.defaultRole().id
        )

        return userRepository.saveUser(userPojo)
    }

    fun setVerifiedTrueByConfirmationToken(confirmationToken: String): UserPojo {
        /*  ###     OLD version
            ###     why by email I dont remember
            val user = userService.findByEmailIgnoreCase(token.userEntity?.email)
            user.isVerified = true
            if (userService.setVerified(user).isVerified) {
                confirmationTokenService.removeByConfirmationToken(confirmationToken)
                ResponseEntity.ok("account verified")
            } else ResponseEntity.badRequest().body("Account not verified")
        */
        val user = userRepository.findUserByConfirmationToken(confirmationToken)
                ?: throw CommonException(
                        CommonError(
                                ErrorCodes.INTERNAL_SERVER_EXC,
                                "Could not find user by confirmation token"
                        ),
                        "User not found by confirmation token. Confirmation token is $confirmationToken"
                )
        return userRepository.setUserVerified(user) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                        "Could not verify user"),
                "Cant verify user"
        )
    }

    private fun findByAnyAndCheckPass(
            anyCred: String,
            pass: String
    ): UserPojo {
        var userFound = false
        findByLogin(anyCred)?.let {
            userFound = true
            if (isPassCorrect(it, pass))
                return it
        }
        findByEmail(anyCred)?.let {
            userFound = true
            if (isPassCorrect(it, pass))
                return it
        }
        if (userFound)
            throw CommonException(CommonError(ErrorCodes.AUTH_PASSWORD_INCORRECT))
        else
            throw CommonException(CommonError(ErrorCodes.AUTH_USER_NOT_FOUND))
    }

    fun findByLogin(login: String): UserPojo? {
        return userRepository.findUserByLogin(login)
    }

    fun findByEmail(email: String): UserPojo? {
        return userRepository.findUserByEmail(email)
    }

    fun findById(id: Int): UserPojo? {
        return userRepository.findUserById(id)
    }

    private fun isPassCorrect(user: UserPojo, password: String): Boolean {
        return passwordEncoder.matches(password, user.password)
    }

    fun users(): List<FullUserInfo> {
        return userRepository.users().map { it.toFullUserInfo() }
    }

    fun getUserRole(userPojo: UserPojo): RolePojo {
        return userRepository.getRoleByRoleId(userPojo.roleId) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "Role with id: ${userPojo.roleId} not found. User with this role : $userPojo"
        )
    }

    /**
     * @Warning updates ALL data from userPojo by id
     *
     */
    fun updateUser(userPojo: UserPojo, registerUserInfo: RegisterUserInfo): UserPojo? {
        validateUserInfo(registerUserInfo)

        val oldUserPojo = userRepository.findUserById(userPojo.id) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "User not found"
        )

        return userRepository.updateUser(
                oldUserPojo,
                registerUserInfo.toUserPojo(passwordEncoder)
        )
    }

    fun validateUserInfo(registerUserInfo: RegisterUserInfo) {
        userRepository.findUserByEmail(registerUserInfo.email)?.let {
            throw CommonException(CommonError(ErrorCodes.EMAIL_IS_USED))
        }

        userRepository.findUserByLogin(registerUserInfo.login)?.let {
            throw CommonException(CommonError(ErrorCodes.LOGIN_IS_USED))
        }
    }

    fun validateUserInfo(registerDemoUserInfo: RegisterDemoUserInfo) {
        userRepository.findUserByLogin(registerDemoUserInfo.login)?.let {
            throw CommonException(CommonError(ErrorCodes.LOGIN_IS_USED))
        }

        userRepository.findUserByEmail(registerDemoUserInfo.login)?.let {
            throw CommonException(CommonError(ErrorCodes.LOGIN_IS_USED))
        }
    }

    fun setUserInRoom(userPojo: UserPojo, roomPojo: RoomPojo): UserPojo {
        return userRepository.setUserInRoom(userPojo.id, roomPojo.id)
                ?: throw CommonException(
                        CommonError(ErrorCodes.INTERNAL_SERVER_EXC)
                )
    }

    fun setUserOutRoom(userPojo: UserPojo): UserPojo {
        return userRepository.setUserNotInRoom(userPojo.id)
                ?: throw CommonException(
                        CommonError(ErrorCodes.INTERNAL_SERVER_EXC)
                )
    }

    fun usersInRoom(roomPojo: RoomPojo): List<FullUserInfo> {
        return userRepository.getUsersInRoom(roomPojo.id).map { it.toFullUserInfo() }
    }

    fun currentUser(): UserPojo {
        val authorizedUserPrincipal = SecurityContextHolder.getContext()
                .authentication
                .principal
        val cud: CustomUserDetails = authorizedUserPrincipal as CustomUserDetails
        val authorizedUserPojo = findByLogin(cud.username)

        return authorizedUserPojo ?: throw CommonException(
                CommonError(ErrorCodes.AUTHORIZATION_FAILED),
                "Cant get current authenticated user.")
    }

    fun validateUserInRoom(userPojo: UserPojo, roomPojo: RoomPojo) {
        if (userPojo.roomId != roomPojo.id)
            throw CommonException(CommonError(ErrorCodes.USER_NOT_IN_ROOM))
    }

    fun updateLastOnline() {
        val user = currentUser()
        userRepository.updateLastOnline(user)
    }

    fun isUserDemo(userPojo: UserPojo): Boolean {
        return userPojo.password == null
    }

    fun removeMe(): PublicUserInfo {
        return userRepository.removeUser(currentUser())?.toPublicUserInfo()?:throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC,"cant remove user")
        )
    }


}