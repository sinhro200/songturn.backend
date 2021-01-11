package com.sinhro.songturn.backend.pojos

import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.PublicUserInfo
import java.beans.ConstructorProperties

class UserPojo(
        var id: Int? = null,
        var login: String? = null,
        var password: String? = null,
        var email: String? = null,
        var role_id: Int? = null,
        var first_name: String? = null,
        var last_name: String? = null,
        var nickname: String? = null,
        var isVerified: Boolean? = null
) {
    @ConstructorProperties(
            "id",
            "nickname",
            "email",
            "login",
            "password",
            "role_id",
            "first_name",
            "last_name",
            "is_verified"
    )
    constructor(
            id: Int?,
            nickname: String?,
            email: String?,
            login: String?,
            password: String?,
            role_id: Int?,
            first_name: String?,
            last_name: String?,
            isVerified: Boolean?
    ) : this(id, login, password, email, role_id, first_name, last_name, nickname, isVerified)

    companion object {
        fun toFullUserInfo(userPojo: UserPojo): FullUserInfo {
            return FullUserInfo(
                    userPojo.id,
                    userPojo.login,
                    userPojo.email,
                    userPojo.first_name,
                    userPojo.last_name,
                    userPojo.nickname
            )
        }

        fun toPublicUserInfo(userPojo: UserPojo): PublicUserInfo {
            return PublicUserInfo(
                    userPojo.first_name,
                    userPojo.last_name,
                    userPojo.nickname
            )
        }

        fun updateNotEmptyValues(userPojo: UserPojo, fullUserInfo: FullUserInfo){
            fullUserInfo.nickname?.let { userPojo.nickname = it }
            fullUserInfo.firstName?.let { userPojo.first_name = it }
            fullUserInfo.lastName?.let { userPojo.last_name = it }
            fullUserInfo.login?.let { userPojo.login = it }
            fullUserInfo.email?.let { userPojo.email = it }
        }
    }
}