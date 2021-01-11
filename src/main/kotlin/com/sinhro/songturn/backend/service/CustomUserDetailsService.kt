package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.filter.CustomUserDetails
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Component

@Component
class CustomUserDetailsService @Autowired constructor(
        private val userService: UserService
) : UserDetailsService {
    override fun loadUserByUsername(s: String): CustomUserDetails? {
        val userPojo = userService.findByLogin(s)
        return userPojo?.let {
            CustomUserDetails.fromUserPojoToCustomUserDetails(it,userService) }
    }

}