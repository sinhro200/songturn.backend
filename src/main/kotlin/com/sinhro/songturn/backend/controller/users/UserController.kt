package com.sinhro.songturn.backend.controller.users

import com.sinhro.songturn.backend.extentions.toFullUserInfo
import com.sinhro.songturn.backend.extentions.toPublicUserInfo
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.FullUserInfoRespBody
import com.sinhro.songturn.rest.request_response.PublicUserInfoRespBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
class UserController @Autowired constructor(
        var userService: UserService
) {

    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: Int?):
            CommonResponse<PublicUserInfoRespBody> {
        id?.let {
            val user = userService.findById(it)
                    ?: throw CommonException(CommonError(ErrorCodes.BAD_REQUEST, "User not found"))
            return CommonResponse.buildSuccess(
                    PublicUserInfoRespBody(user.toPublicUserInfo())
            )
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @GetMapping("/user/me")
    fun getMe(): CommonResponse<FullUserInfo> {
        return CommonResponse.buildSuccess(
                userService.currentUser().toFullUserInfo()
        )
    }

    @PostMapping("/user/changeme")
    fun updateMe(
            @RequestBody userInfo: CommonRequest<RegisterUserInfo>
    ): CommonResponse<FullUserInfo> {
        if (userInfo.data == null)
            throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Request data is null"))
        val newUserInfo = userInfo.data!!


        val authorizedUserPojo = userService.currentUser()

        userService.updateUser(authorizedUserPojo,newUserInfo)?.let { savedUserPojo ->
            return CommonResponse(
                    savedUserPojo.toFullUserInfo()
            )
        }
        throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                        "Cant update user data"
                ),
                "Cant update user data. $authorizedUserPojo"
        )
    }
}