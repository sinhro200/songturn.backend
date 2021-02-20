package com.sinhro.songturn.backend.controller.users

import com.sinhro.songturn.backend.extentions.toFullUserInfo
import com.sinhro.songturn.backend.extentions.toPublicUserInfo
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.PublicUserInfoRespBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class UserController @Autowired constructor(
        var userService: UserService
) {

    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: Int?):
            PublicUserInfoRespBody {
        id?.let {
            val user = userService.findById(it)
                    ?: throw CommonException(CommonError(ErrorCodes.BAD_REQUEST, "User not found"))
            return PublicUserInfoRespBody(user.toPublicUserInfo())

        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @GetMapping("/user/me")
    fun getMe(): FullUserInfo {
        return userService.currentUser().toFullUserInfo()

    }

    @PostMapping("/user/changeme")
    fun updateMe(
            @RequestBody newUserInfo: RegisterUserInfo
    ): FullUserInfo {
        val authorizedUserPojo = userService.currentUser()

        userService.updateUser(authorizedUserPojo, newUserInfo)?.let { savedUserPojo ->
            return savedUserPojo.toFullUserInfo()
        }
        throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                        "Cant update user data"
                ),
                "Cant update user data. $authorizedUserPojo"
        )
    }

    @GetMapping("/user/logout")
    fun logout() {
        userService.userLogout()
    }
}