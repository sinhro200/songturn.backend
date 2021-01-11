package com.sinhro.songturn.backend.service

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.backend.pojos.ConfirmationTokenPojo
import com.sinhro.songturn.backend.pojos.RolePojo
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.repository.UserRepository
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserService @Autowired constructor(
//        val dsl: DSLContext,
        val passwordEncoder: PasswordEncoder,
        val userRepository: UserRepository
) {
//    init {
//        dsl.configuration().set(CustomSQLExceptionTranslator())
//    }

//    private val tableUsers: Users = Users.USERS
//    private val tableRole: Role = Role.ROLE
//    private val tableConfirmation: ConfirmationToken = ConfirmationToken.CONFIRMATION_TOKEN

    fun registerUser(userPojo: UserPojo, shouldVerify: Boolean): UserPojo? {
        userRepository.validateNewUserData(userPojo)

        if (shouldVerify)
            return saveUserNotVerifiedAndCreateConfirmationToken(userPojo)
        else
            return saveUserVerified(userPojo)
    }

    fun saveUserVerified(userPojoDTO: UserPojo): UserPojo? {
        userPojoDTO.isVerified = true
        return initAndSave(userPojoDTO)
    }

    fun saveUserNotVerifiedAndCreateConfirmationToken(userPojoDTO: UserPojo): UserPojo? {
        userPojoDTO.isVerified = false

        initAndSave(userPojoDTO)?.apply {
            //  add user
            id?.let { id ->
                //user has id not null
                val confirmationToken = ConfirmationTokenPojo.createNew(id)
                userRepository.saveConfirmationToken(confirmationToken)?.let {
                    //created conf token
                    return this
                }
                //cant create conf token, should delete user
                userRepository.deleteUserById(id)
            }
            //user has not id (id is null). But it cant happen
        }
        return null
    }

    fun getConfirmationToken(user: UserPojo): String? {
        return userRepository.getConfirmationToken(user)
    }

    private fun initAndSave(userPojo: UserPojo): UserPojo? {

        userPojo.role_id = userRepository.defaultRole().id
        userPojo.password = passwordEncoder.encode(userPojo.password)

        return userRepository.saveUser(userPojo)
    }

    fun setVerifiedTrueByConfirmationToken(confirmationToken: String): UserPojo? {
        /*  ###     OLD version
            ###     why by email I dont remember
            val user = userService.findByEmailIgnoreCase(token.userEntity?.email)
            user.isVerified = true
            if (userService.setVerified(user).isVerified) {
                confirmationTokenService.removeByConfirmationToken(confirmationToken)
                ResponseEntity.ok("account verified")
            } else ResponseEntity.badRequest().body("Account not verified")
        */
        val userPojo = userRepository.findUserByConfirmationToken(confirmationToken)
        userPojo?.let {
            return userRepository.verifyUser(it)
        }
        throw CommonException(
                CommonError(
                        ErrorCodes.INTERNAL_SERVER_EXC
                ),
                "Could not find user by confirmation token"
        )
    }

    fun findByLogin(login: String): UserPojo? {
        return userRepository.findUserByLogin(login)
    }

    fun findByNickname(nickname: String): UserPojo? {
        return userRepository.findUserByNickname(nickname)
    }

    fun findByEmail(email: String): UserPojo? {
        return userRepository.findUserByEmail(email)
    }

    fun isPassCorrect(user: UserPojo, password: String): Boolean {
        return passwordEncoder.matches(password, user.password)
    }

    fun users(): MutableList<UserPojo> {
        return userRepository.users()
    }

    fun getUserRole(userPojo: UserPojo): RolePojo {
        userPojo.role_id?.let {
            return userRepository.getRoleByRoleId(it) ?: throw CommonException(
                    CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                    "Role with id: $it not found. User with this role : $userPojo"
            )
        }
        throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "User dont has role id, $userPojo"
        )
    }

    /**
     * @Warning updates ALL data from userPojo by id
     *
     */
    fun updateUser(userPojo: UserPojo): UserPojo? {
        userRepository.validateNewUserData(userPojo)

        return userRepository.updateUserData(userPojo)
    }

}