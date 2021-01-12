package com.sinhro.songturn.backend.pojos

import com.sinhro.songturn.rest.model.FullUserInfo
import com.sinhro.songturn.rest.model.RoomInfo

class RoomPojo(
        var id: Int? = null,
        var invite: String? = null,
        var token: String? = null,
        var title: String? = null,
        var ownerId: Int? = null,
        var rs_priority_rarely_ordering_users: Boolean? = null,
        var rs_allow_votes: Boolean? = null,
        var rs_song_owners_visible: Boolean? = null
){
    companion object{
        fun toRoomInfo(roomPojo: RoomPojo) : RoomInfo{
            return RoomInfo(
                    roomPojo.title,
                    roomPojo.invite,
                    roomPojo.token,
                    roomPojo.ownerId
            )
        }
    }
}