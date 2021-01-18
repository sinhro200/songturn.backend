package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.backend.tables.RoomAction
import com.sinhro.songturn.backend.tables.WhatShouldUpdate
import com.sinhro.songturn.backend.tables.records.RoomActionRecord
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.RoomActionType
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.stream.Collectors

@Component
class RoomActionService @Autowired constructor(
        private val dsl: DSLContext
) {
    private val tableActions = RoomAction.ROOM_ACTION

    fun roomAction(userPojo: UserPojo,
                   roomPojo: RoomPojo,
                   roomActionType: RoomActionType,
                   initAction: Boolean = false
    ) = action(userPojo, roomPojo, roomActionType, initAction, true)


    fun userAction(userPojo: UserPojo,
                   roomPojo: RoomPojo,
                   roomActionType: RoomActionType,
                   initAction: Boolean = false
    ) = action(userPojo, roomPojo, roomActionType, initAction, false)

    private fun action(
            userPojo: UserPojo,
            roomPojo: RoomPojo,
            roomActionType: RoomActionType,
            initAction: Boolean,
            isRoomChanged: Boolean
    ) {
        val userid = userPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "user dont has id")
        val roomid = roomPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "room dont has id")
        if (initAction) {
            dsl.insertInto(tableActions)
                    .columns(
                            tableActions.USER_ID,
                            tableActions.ROOM_ID,
                            tableActions.ACTION_TYPE,
                            tableActions.IS_ROOM_CHANGED)
                    .values(
                            userid,
                            roomid,
                            roomActionType.code,
                            isRoomChanged)
        } else {
            dsl.update(tableActions)
                    .set(
                            tableActions.TIMESTAMP,
                            LocalDateTime.now(ZoneOffset.UTC))
                    .where(tableActions.USER_ID.eq(userid)
                            .and(tableActions.ROOM_ID.eq(roomid))
                            .and(tableActions.ACTION_TYPE.eq(roomActionType.code))
                            .and(tableActions.IS_ROOM_CHANGED.eq(isRoomChanged))
                    )
        }
    }

    fun initUserActions(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ): List<RoomActionRecord?> {
        val userid = userPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "user dont has id")
        val roomid = roomPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "room dont has id")

        val list = mutableListOf<RoomActionRecord?>()
        for (actionType in RoomActionType.values()) {
            val res = dsl.insertInto(tableActions)
                    .columns(
                            tableActions.USER_ID,
                            tableActions.ROOM_ID,
                            tableActions.ACTION_TYPE,
                            tableActions.TIMESTAMP)
                    .values(
                            userid,
                            roomid,
                            actionType.code,
                            LocalDateTime.of(1970,1,1,0,0))
                    .returning()
                    .fetchOne()
            list.add(res)
        }
        return list
    }

    fun initUserCreatedRoomActions(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ): List<RoomActionRecord?> {
        val userid = userPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "user dont has id")
        val roomid = roomPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "room dont has id")

        val list = mutableListOf<RoomActionRecord?>()
        for (actionType in RoomActionType.values()) {
            val res = dsl.insertInto(tableActions)
                    .columns(
                            tableActions.USER_ID,
                            tableActions.ROOM_ID,
                            tableActions.ACTION_TYPE,
                            tableActions.IS_ROOM_CHANGED,
                            tableActions.TIMESTAMP
                    )
                    .values(
                            userid,
                            roomid,
                            actionType.code,
                            true,
                            LocalDateTime.now(ZoneOffset.UTC)
                    )
                    .returning()
                    .fetchOne()
            list.add(res)
        }
        return list
    }

    fun whatShouldUpdate(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ): MutableList<RoomActionType> {
        val userid = userPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "user dont has id")
        val roomid = roomPojo.id
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "room dont has id")

        val wsu = WhatShouldUpdate()
        val actionTypeCodes = dsl.selectFrom(wsu.call(roomid, userid))
                .fetch()
                .getValues(tableActions.ACTION_TYPE)
        return actionTypeCodes.stream()
                .map { RoomActionType.usingCode(it) }
                .filter { it != null }
                .collect(Collectors.toList())
    }
}