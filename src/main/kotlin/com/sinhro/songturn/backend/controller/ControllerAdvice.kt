package com.sinhro.songturn.backend.controller

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
                getCode(ex.commonError)
        )
    }

    companion object {
        private fun getCode(ce: CommonError): HttpStatus {
            return getCodeDetailed(ce) ?: getCodeSimple(ce)
        }

        private fun getCodeSimple(ce: CommonError): HttpStatus {
            when (ce.errorCode.type) {
                ErrorTypes.Unauthorized -> return HttpStatus.UNAUTHORIZED
                ErrorTypes.Forbidden -> return HttpStatus.FORBIDDEN
                ErrorTypes.NimuscServer -> return HttpStatus.BAD_REQUEST
                ErrorTypes.NimuscMusicServices -> return HttpStatus.BAD_REQUEST
                ErrorTypes.Internal -> return HttpStatus.INTERNAL_SERVER_ERROR
            }
            return HttpStatus.BAD_REQUEST
        }

        //ToDo detailed handling
        private fun getCodeDetailed(ce: CommonError): HttpStatus? {
            return null
        }
    }
}