package com.sinhro.songturn.backend.extentions

import com.sinhro.songturn.rest.ErrorTypes
import com.sinhro.songturn.rest.core.CommonError
import org.springframework.http.HttpStatus

fun CommonError.getHttpStatusCode(): HttpStatus {
    fun getCodeSimple(ce: CommonError): HttpStatus {
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
    fun getCodeDetailed(ce: CommonError): HttpStatus? {
        return null
    }

    return getCodeDetailed(this) ?: getCodeSimple(this)
}