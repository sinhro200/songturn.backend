package com.sinhro.songturn.backend.controller

import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.service.UserService
import org.jooq.Record9
import org.jooq.Result
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


//@Secured("ROLE_ADMIN")
@RestController
class UsersController @Autowired constructor(
        var userService: UserService
) {


    @GetMapping("/test/admin_api/users/")
    fun getUsers(): MutableList<UserPojo> {
        return userService.users()
    }

    @GetMapping("/test/admin_api/sas/")
    fun getSas(): String {
        return "sas"
    }
}