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
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController @Autowired constructor(
        var userService: UserService
) {

    @GetMapping("/user/me")
    fun getInfo(): CommonResponse<FullUserInfoRespBody> {
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
    fun setInformation(
            @RequestBody userInfo: CommonRequest<FullUserInfoReqData>
    ): CommonResponse<FullUserInfoRespBody> {
        if (userInfo.data == null || userInfo.data!!.fullUserInfo == null)
            throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC,"Request data is null"))
        val newFullUserInfo = userInfo.data!!.fullUserInfo!!

        val authorizedUserPrincipal = SecurityContextHolder.getContext()
                .authentication
                .principal
        val cud: CustomUserDetails = authorizedUserPrincipal as CustomUserDetails
        val authorizedUserPojo = userService.findByLogin(cud.username)

        authorizedUserPojo?.let { authUserPojo ->
            if (authUserPojo.id == null)
                throw CommonException(
                        CommonError(ErrorCodes.INTERNAL_SERVER_EXC),"User found by login ${cud.username} dont have id")
            if (newFullUserInfo.id != null && authUserPojo.id != newFullUserInfo.id)
                throw CommonException(CommonError(
                        ErrorCodes.AUTH_DONT_HAVE_PERMISSIONS,
                        "Dont have permissions to change user data"
                ))

                UserPojo.updateNotEmptyValues(authUserPojo,
                        userInfo.data!!.fullUserInfo!!)

            userService.updateUser(authUserPojo)?.let { savedUserPojo ->
                return CommonResponse(FullUserInfoRespBody(
                        UserPojo.toFullUserInfo(savedUserPojo)
                ))
            }
            throw CommonException(
                    CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                            "Cant update user data"
                    ),
                    "Cant update user data. $authUserPojo"
            )
        }
        throw CommonException(CommonError(ErrorCodes.AUTH_USER_NOT_FOUND))
    }
}