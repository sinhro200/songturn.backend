package com.sinhro.songturn.backend.controller

import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class Test {

    @PostMapping("/test/1")
    fun test(
            @RequestBody value : Int
    ){
        throw CommonException(CommonError(ErrorCodes.NIMUSC_SERVER_EXC))
    }
}