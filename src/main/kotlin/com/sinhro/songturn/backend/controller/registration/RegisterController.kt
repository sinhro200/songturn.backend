package com.sinhro.songturn.backend.controller.registration

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
//import com.sinhro.songturn.rest.core.CommonRequest
//import com.sinhro.songturn.rest.core.ResponseBody
import com.sinhro.songturn.rest.request_response.RegisterRespBody
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.RegisterDemoUserInfo
import com.sinhro.songturn.rest.request_response.RegisterDemoReqData
import com.sinhro.songturn.rest.request_response.RegisterDemoRespBody
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
            @RequestBody registrationRequest: RegisterReqData
    ): RegisterRespBody {
        val savedUserPojo = userService.validateAndRegisterUser(
                registrationRequest.userInfo,
                shouldConfirm
        )
        return RegisterRespBody(
                shouldConfirm,
                if (shouldConfirm)
                    "We send confirmation to your email"
                else
                    "Registered successfully"
        )
    }

    @PostMapping("/registerDemo")
    fun registerDemoUser(
            @RequestBody registrationRequest: RegisterDemoReqData
    ): RegisterDemoRespBody {
        val accTok = userService.validateAndRegisterUser(
                registrationRequest.userInfo
        )
        return RegisterDemoRespBody(accTok)
    }

    @RequestMapping("/confirm-account")
    fun confirmUserAccount(
            @RequestParam("token") confirmationToken: String
    ): ResponseEntity<*>? {

        try {
            val user = userService.setVerifiedTrueByConfirmationToken(confirmationToken)
            return ResponseEntity.ok("account for user ${user.nickname} verified")

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