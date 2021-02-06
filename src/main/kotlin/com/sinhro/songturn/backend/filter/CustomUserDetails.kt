package com.sinhro.songturn.backend.filter

import com.sinhro.songturn.backend.service.UserService
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo

class CustomUserDetails : UserDetails {
    private lateinit var login: String
    private lateinit var password: String
    private lateinit var grantedAuthorities: Collection<GrantedAuthority?>

    override fun getAuthorities(): Collection<GrantedAuthority?> {
        return grantedAuthorities
    }

    override fun getPassword(): String {
        return password
    }

    override fun getUsername(): String {
        return login
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }

    companion object {
        fun fromUserPojoToCustomUserDetails(
                userPojo: UserPojo, userService: UserService
        ): CustomUserDetails {
            val c = CustomUserDetails()
            c.login = userPojo.login
//            c.password = userPojo.password
            c.grantedAuthorities = listOf(
                    SimpleGrantedAuthority(
                            userService.getUserRole(userPojo).name
                    )
            )
            return c
        }
    }
}