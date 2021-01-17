package com.sinhro.songturn.backend.controller.registration

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.RegisterReqData
import com.sinhro.songturn.rest.request_response.RegisterRespBody
import com.sinhro.songturn.backend.jooq.extractCommonErrorMessage
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.service.EmailSenderService
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.backend.tables.pojos.Users
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.validation.Validator
import com.sinhro.songturn.rest.validation.ValidationResult
import org.jooq.exception.DataAccessException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RegisterController @Autowired constructor(
        private val userService: UserService,
        private val emailSenderService: EmailSenderService,
        private val confirmationMailBuilder: ConfirmationMailBuilder,
        private val validator: Validator

) {

    @Value("\${mail-confirmation.shouldConfirm}")
    private var shouldConfirm: Boolean = false

    private fun RegisterReqData.toUserPojo(): UserPojo {
        Users()
        val user = UserPojo(

        )
        user.login = this.login
        user.email = this.email
        user.nickname = this.nickname
        user.first_name = this.firstName
        user.last_name = this.lastName
        return user
    }

    @PostMapping("/register")
    fun registerUser(
            @RequestBody registrationRequest: CommonRequest<RegisterReqData>
    ): CommonResponse<RegisterRespBody> {
        registrationRequest.data?.let {
            val validationErrors: Map<String, List<ValidationResult>> =
                    validator
                            .validate(it)
                            .resultForErrorFields()
            if (validationErrors.isNotEmpty()){
                throw CommonException(
                        CommonError(
                                ErrorCodes.REGISTER_FAILED,
                                "Register failed, fields not correct",
                                "There is some restrictions on user fields, check extra",
                                validationErrors
                        )
                )
            }

            val userPojo = it.toUserPojo()
            try {
                userService.registerUser(
                        userPojo,
                        it.password,
                        shouldConfirm
                )?.let {
                    if (shouldConfirm) {
                        emailSenderService.sendEmail(confirmationMailBuilder.createConfirmationMail(
                                it.email
                                        ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC)),
                                userService.getConfirmationToken(it)
                                        ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC))
                        ))
                    }
                    return CommonResponse.buildSuccess(
                            RegisterRespBody(
                                    if (shouldConfirm) "We send confirmation to your email" else "Registered successfully"
                            )
                    )
                }
            } catch (e: DataAccessException) {
                e.printStackTrace()
                throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC, e.extractCommonErrorMessage()))
            }
            CommonException(CommonError(ErrorCodes.REGISTER_FAILED, "failed to register user"))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Error. Request with empty data"))
    }

    @RequestMapping("/confirm-account")
    fun confirmUserAccount(
            @RequestParam("token") confirmationToken: String?
    ): ResponseEntity<*>? {
        if (confirmationToken.isNullOrEmpty())
            throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Confirmation token null or empty"))

        try {
            userService.setVerifiedTrueByConfirmationToken(confirmationToken)?.let {
                return ResponseEntity.ok("account for user ${it.nickname} verified")
            }
        }catch (ce : CommonException){
            throw CommonException(
                    CommonError(
                            ErrorCodes.REQUEST_DATA_EXC,
                            "The link is invalid or broken!"
                    ),
                    "Could not find user by confirmation token",
                    ce
            )
        }

        return ResponseEntity.badRequest().body("Account not verified")
    }
}