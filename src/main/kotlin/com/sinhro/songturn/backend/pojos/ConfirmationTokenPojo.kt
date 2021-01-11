package com.sinhro.songturn.backend.pojos

import java.beans.ConstructorProperties
import java.time.LocalDateTime
import java.util.*


class ConfirmationTokenPojo
    @ConstructorProperties(
            "id",
            "token",
            "created_date",     //  createdDate
            "user_id"           //  userId
    )
    constructor(
            public var id : Long? = null,
            public var token: String? = null,
            public var createdDate: LocalDateTime? = null,
            public var userId: Int? = null
    )
{
    companion object{
        fun createNew(userId : Int)
                :ConfirmationTokenPojo {
            val confirmation = ConfirmationTokenPojo()
            confirmation.createdDate = LocalDateTime.now()
            confirmation.token = UUID.randomUUID().toString()
            confirmation.userId = userId

            return confirmation
        }
    }
//    var id : Long? = null
//    var token: String? = null
//    var createdDate: LocalDateTime? = null
//    var userId: Long? = null

//    @ConstructorProperties(
//            "id",
//            "token",
//            "created_date",
//            "user_id"
//    )
//    constructor(
//            id : Long? = null,
//            token: String? = null,
//            createdDate: LocalDateTime? = null,
//            userId: Long? = null
//    ){
//        this.id = id
//        this.token = token
//        this.createdDate = createdDate
//        this.userId = userId
//    }
}