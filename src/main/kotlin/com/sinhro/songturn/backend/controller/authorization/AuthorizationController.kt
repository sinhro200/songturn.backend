package com.sinhro.songturn.backend.controller.authorization
q
import com.sinhro.songturn.rest.request_response.AuthReqData
import com.sinhro.songturn.rest.request_response.AuthRespBody
import com.sinhro.songturn.backend.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/login")
class AuthorizationController @Autowired constructor(
        private val userService: UserService
) {
    @PostMapping
    fun auth(@RequestBody request: AuthReqData)
            : AuthRespBody {
        return userService.authorizeUser(request)
    }


}