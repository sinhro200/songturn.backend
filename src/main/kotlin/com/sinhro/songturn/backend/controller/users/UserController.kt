package com.sinhro.songturn.backend.controller.users

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.backend.filter.CustomUserDetails
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.request_response.FullUserInfoReqData
import com.sinhro.songturn.rest.request_response.FullUserInfoRespBody
import com.sinhro.songturn.rest.request_response.PublicUserInfoRespBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.*

@RestController
class UserController @Autowired constructor(
        var userService: UserService
) {

    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: Int?) :
            CommonResponse<PublicUserInfoRespBody>{
        id?.let {
            val user = userService.findById(id)
            user?.let {
                return CommonResponse.buildSuccess(
                        PublicUserInfoRespBody(UserPojo.toPublicUserInfo(it))
                )
            }
            throw CommonException(CommonError(ErrorCodes.BAD_REQUEST))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @GetMapping("/user/me")
    fun getMe(): CommonResponse<FullUserInfoRespBody> {
        val principal = SecurityContextHolder.getContext().authentication.principal
        val cud: CustomUserDetails = principal as CustomUserDetails
        val ue: UserPojo? = userService.findByLogin(cud.getUsername())
        ue?.let {
            return CommonResponse.buildSuccess(
                    FullUserInfoRespBody(UserPojo.toFullUserInfo(it))
            )
        }
        throw CommonException(CommonError(ErrorCodes.AUTH_USER_NOT_FOUND))
    }

    @PostMapping("/user/changeme")
    fun updateMe(
            @RequestBody userInfo: CommonRequest<FullUserInfoReqData>
    ): CommonResponse<FullUserInfoRespBody> {
        if (userInfo.data == null || userInfo.data!!.fullUserInfo == null)
            throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC, "Request data is null"))
        val newFullUserInfo = userInfo.data!!.fullUserInfo!!


        val authorizedUserPojo: UserPojo = userService.currentUser()

        if (authorizedUserPojo.id == null)
            throw CommonException(
                    CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "User found by login ${authorizedUserPojo.login} dont have id")
        if (newFullUserInfo.id != null && authorizedUserPojo.id != newFullUserInfo.id)
            throw CommonException(CommonError(
                    ErrorCodes.AUTH_DONT_HAVE_PERMISSIONS,
                    "Dont have permissions to change user data"
            ))

        UserPojo.updateNotEmptyValues(authorizedUserPojo, newFullUserInfo)

        userService.updateUser(authorizedUserPojo)?.let { savedUserPojo ->
            return CommonResponse(FullUserInfoRespBody(
                    UserPojo.toFullUserInfo(savedUserPojo)
            ))
        }
        throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                        "Cant update user data"
                ),
                "Cant update user data. $authorizedUserPojo"
        )
    }
}