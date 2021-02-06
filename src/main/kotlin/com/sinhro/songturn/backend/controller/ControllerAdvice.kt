package com.sinhro.songturn.backend.controller

import com.sinhro.songturn.backend.extentions.getHttpStatusCode
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.backend.jooq.extractCommonErrorMessage
import com.sinhro.songturn.rest.ErrorTypes
import com.sinhro.songturn.rest.core.CommonException
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest

@ControllerAdvice
class ControllerAdvice {
    private var log= LoggerFactory.getLogger(ControllerAdvice::class.java)

    @ExceptionHandler(DataAccessException::class)
    fun handlerDataAccessExceptions(
            ex: DataAccessException, request: WebRequest
    ) :ResponseEntity<Any>{
        log.info("Got data access exception in controller advice exception handler. ${ex.toString()}")
        log.debug(ex.stackTraceToString())
        return handler(
                CommonException(CommonError(
                        ErrorCodes.INTERNAL_SERVER_EXC, ex.extractCommonErrorMessage())),
                request
        )
    }

    @ExceptionHandler(CommonException::class)
    fun handler(
            ex: CommonException, request: WebRequest
    ) :ResponseEntity<Any>{
        log.info("Got common exception in controller advice exception handler. ${ex.toString()}")
        log.debug(ex.stackTraceToString())
        return ResponseEntity(
                ex.commonError,
                ex.commonError.getHttpStatusCode()
        )
    }
}