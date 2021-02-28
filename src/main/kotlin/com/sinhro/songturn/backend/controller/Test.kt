package com.sinhro.songturn.backend.controller

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Test {

    @GetMapping("/test/1")
    fun test1(){
        throw CommonException(CommonError(ErrorCodes.NIMUSC_SERVER_EXC))
    }

    @GetMapping("/test/0")
    fun test0(): String {
        return "test"
    }
}