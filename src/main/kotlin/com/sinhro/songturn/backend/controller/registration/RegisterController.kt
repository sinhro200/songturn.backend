package com.sinhro.songturn.backend.controller.registration

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.RegisterRespBody
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.RegisterReqData
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class RegisterController @Autowired constructor(
        private val userService: UserService

) {

    @Value("\${mail-confirmation.shouldConfirm}")
    private var shouldConfirm: Boolean = false

    @PostMapping("/register")
    fun registerUser(
            @RequestBody registrationRequest:
            CommonRequest<RegisterReqData>
    ): CommonResponse<RegisterRespBody> {
        registrationRequest.data?.let {

            val savedUserPojo = userService.validateAndRegisterUser(
                    it.userInfo,
                    shouldConfirm
            )
            return CommonResponse.buildSuccess(
                    RegisterRespBody(
                            if (shouldConfirm) "We send confirmation to your email" else "Registered successfully"
                    )
            )
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Error. Request with empty data"))
    }

    @RequestMapping("/confirm-account")
    fun confirmUserAccount(
            @RequestParam("token") confirmationToken: String
    ): ResponseEntity<*>? {
//        if (confirmationToken.isNullOrEmpty())
//            throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Confirmation token null or empty"))

        try {
            userService.setVerifiedTrueByConfirmationToken(confirmationToken)?.let {
                return ResponseEntity.ok("account for user ${it.nickname} verified")
            }
        } catch (ce: CommonException) {
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