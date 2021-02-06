package com.sinhro.songturn.backend.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.sinhro.songturn.backend.extentions.getHttpStatusCode
import com.sinhro.songturn.rest.core.CommonError
//import com.sinhro.songturn.rest.core.ResponseBody
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import kotlin.jvm.Throws


@Component
class ExceptionHandlerFilter(@Qualifier("objectMapperPrimary") @Autowired
                             private val mapper: ObjectMapper) : OncePerRequestFilter() {

    private var log = LoggerFactory.getLogger(ExceptionHandlerFilter::class.java)

    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        var errorDTO: Any? = null
        var status: Int = 400
        try {
            filterChain.doFilter(request, response)
        } catch (e: CommonException) {
            log.info("Got CommonException in exception Filter. ${e.toString()}")
            e.printStackTrace()
            errorDTO = e.commonError
            status = e.commonError.getHttpStatusCode().value()
        }

        /*catch (e: JwtAuthProvider.TokenExpiredException) {
            errorDTO = CommonResponse.Companion.buildError(
                    ErrorCodes.AUTH_JWT_EXPIRED, e.getMessage(), null, null
            )
        } catch (e: JwtAuthProvider.InvalidTokenException) {
            errorDTO = CommonResponse.Companion.buildError(
                    ErrorCodes.AUTH_JWT_INVALID, e.getMessage(), null, null
            )
        }*/
        catch (e: Exception) {
            log.info("Got exception in exception Filter. $e")
            e.printStackTrace()
            errorDTO = CommonError(ErrorCodes.INTERNAL_SERVER_EXC)

        }
        if (errorDTO != null) {
            response.contentType = "application/json"
            response.status = status
            val out = response.writer
            out.print(mapper.writeValueAsString(errorDTO))
            out.flush()
//            out.flush()
        }
    }
}