package com.sinhro.songturn.backend.controller.authorization

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.AuthReqData
import com.sinhro.songturn.rest.request_response.AuthRespBody
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.providers.JwtAuthProvider
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController()
@RequestMapping("/login")
class AuthorizationController @Autowired constructor(
        private val userService: UserService,
        private val jwtAuthProvider: JwtAuthProvider
) {
    @PostMapping
    fun auth(@RequestBody request: CommonRequest<AuthReqData>)
            : CommonResponse<AuthRespBody> {
        request.data?.let { req ->
            if (req.login.isNullOrEmpty() || req.password.isNullOrEmpty())
                throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Wrong request"))
            val log = req.login!!
            val pass = req.password!!

            val userPojo = userService.findByAnyAndCheckPass(log, pass)
            userPojo.isVerified?.let {
                if (!it)
                    throw CommonException(CommonError(
                            ErrorCodes.AUTH_USER_NOT_VERIFIED,
                            "User not verified")
                    )
            }

            val token = jwtAuthProvider.generateToken(userPojo.login!!)
            val ui = UserPojo.toFullUserInfo(userPojo)
            return CommonResponse.buildSuccess(AuthRespBody(ui, token))
        }
        throw CommonException(CommonError(
                ErrorCodes.REQUEST_DATA_EXC,
                "Empty req data")
        )
    }
}