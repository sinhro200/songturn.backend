package com.sinhro.songturn.backend.jooq

import org.jooq.exception.DataAccessException

class CommonExceptionTextExtractor {
    companion object {
        fun extract(text: String?): String {
            if (text.isNullOrBlank())
                return ""
            return text
                    .substringAfter("ErrorMessage{","")
                    .substringBefore("}","")
        }
    }
}

fun DataAccessException.extractCommonErrorMessage() : String {
    return CommonExceptionTextExtractor.extract(this.cause?.message.toString())
//    return CommonExceptionTextExtractor.extract(this.message)
}