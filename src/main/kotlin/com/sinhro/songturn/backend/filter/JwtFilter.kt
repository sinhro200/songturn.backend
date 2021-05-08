package com.sinhro.songturn.backend.filter

import com.sinhro.songturn.backend.providers.JwtAuthProvider
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.GenericFilterBean
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

@Component
class JwtFilter : GenericFilterBean() {
    @Autowired
    private lateinit var jwtAuthProvider: JwtAuthProvider

    @Autowired
    private lateinit var userService: UserService

    override fun doFilter(
            servletRequest: ServletRequest,
            servletResponse: ServletResponse,
            filterChain: FilterChain
    ) {
        val tokenFromRequest = getTokenFromRequest(servletRequest as HttpServletRequest)
        tokenFromRequest?.let { token ->
            if (jwtAuthProvider.validateToken(token)) {
                val userId: Int = jwtAuthProvider.getIdFromToken(token)
                val userPojo = userService.findById(userId)
                        ?: throw CommonException(CommonError(ErrorCodes.AUTH_USER_NOT_FOUND))
                val customUserDetails: CustomUserDetails =
                        CustomUserDetails.fromUserPojoToCustomUserDetails(userPojo, userService)


                val auth = UsernamePasswordAuthenticationToken(
                        customUserDetails, null, customUserDetails.authorities
                )
                SecurityContextHolder.getContext().authentication = auth
                userService.updateLastOnline()
            }else{
                throw CommonException(CommonError(ErrorCodes.AUTH_JWT_INVALID))
            }
        }

        filterChain.doFilter(servletRequest, servletResponse)
    }

    private fun getTokenFromRequest(request: HttpServletRequest): String? {
        val bearer = request.getHeader(AUTHORIZATION)
        return if (!bearer.isNullOrEmpty() && bearer.startsWith("Bearer ")) {
            bearer.substring(7)
        } else null
        //throw CommonException(CommonError(ErrorCodes.AUTHORIZATION_FAILED))
    }

    companion object {
        const val AUTHORIZATION = "Authorization"
    }
}