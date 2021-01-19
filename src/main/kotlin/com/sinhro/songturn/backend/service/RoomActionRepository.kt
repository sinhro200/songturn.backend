package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.backend.tables.RoomAction
import com.sinhro.songturn.backend.tables.WhatShouldUpdate
import com.sinhro.songturn.backend.tables.records.RoomActionRecord
import com.sinhro.songturn.rest.model.RoomActionType
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.stream.Collectors

@Component
class RoomActionRepository @Autowired constructor(
        private val dsl: DSLContext
) {
    private val tableActions = RoomAction.ROOM_ACTION

    fun changeAction(userPojo: UserPojo,
                     roomPojo: RoomPojo,
                     roomActionType: RoomActionType,
                     initAction: Boolean = false
    ) = doAction(userPojo, roomPojo, roomActionType, initAction, true)


    fun userUpdateAction(userPojo: UserPojo,
                         roomPojo: RoomPojo,
                         roomActionType: RoomActionType,
                         initAction: Boolean = false
    ) = doAction(userPojo, roomPojo, roomActionType, initAction, false)

    private fun doAction(
            user: UserPojo,
            room: RoomPojo,
            roomActionType: RoomActionType,
            initAction: Boolean,
            isChangeAction: Boolean
    ) {
        if (initAction) {
            dsl.insertInto(tableActions)
                    .columns(
                            tableActions.USER_ID,
                            tableActions.ROOM_ID,
                            tableActions.ACTION_TYPE,
                            tableActions.IS_CHANGE_ACTION)
                    .values(
                            user.id,
                            room.id,
                            roomActionType.code,
                            isChangeAction)
        } else {
            dsl.update(tableActions)
                    .set(
                            tableActions.TIMESTAMP,
                            LocalDateTime.now(ZoneOffset.UTC))
                    .where(tableActions.USER_ID.eq(user.id)
                            .and(tableActions.ROOM_ID.eq(room.id))
                            .and(tableActions.ACTION_TYPE.eq(roomActionType.code))
                            .and(tableActions.IS_CHANGE_ACTION.eq(isChangeAction)))
                    .execute()
        }
    }

    fun initUserUpdateActions(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ) {
        for (actionType in RoomActionType.values()) {
            dsl.newRecord(tableActions)
                    .setUserId(userPojo.id)
                    .setRoomId(roomPojo.id)
                    .setActionType(actionType.code)
                    .setTimestamp(LocalDateTime.of(
                            1970, 1, 1, 0, 0))
                    .store()
        }

    }

    fun initUserChangeActions(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ) {
        for (actionType in RoomActionType.values()) {
            dsl.newRecord(tableActions)
                    .setUserId(userPojo.id)
                    .setRoomId(roomPojo.id)
                    .setActionType(actionType.code)
                    .setIsChangeAction(true)
                    .setTimestamp(LocalDateTime.now(ZoneOffset.UTC))
                    .store()
        }
    }

    fun removeAllActionsByRoom(
            roomPojo: RoomPojo
    ) {
        dsl.deleteFrom(tableActions)
                .where(tableActions.ROOM_ID.eq(roomPojo.id))
                .execute()
    }

    fun whatShouldUpdate(
            userPojo: UserPojo,
            roomPojo: RoomPojo
    ): List<RoomActionType> {

        val whatShouldUpdateFunction = WhatShouldUpdate()
        val actionTypeCodes = dsl.selectFrom(whatShouldUpdateFunction.call(
                roomPojo.id, userPojo.id))
                .fetch()
                .getValues(tableActions.ACTION_TYPE)
        return actionTypeCodes.mapNotNull { RoomActionType.usingCode(it) }
    }
}