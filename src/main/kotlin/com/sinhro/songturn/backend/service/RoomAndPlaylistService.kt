package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.pojos.PlaylistPojo
import com.sinhro.songturn.backend.pojos.RoomPojo
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.providers.JwtRoomProvider
import com.sinhro.songturn.backend.repository.RoomPlaylistRepository
import com.sinhro.songturn.backend.utils.RandomStringGenerator
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoomAndPlaylistService @Autowired constructor(
        private val roomPlaylistRepository: RoomPlaylistRepository,
        private val jwtRoomProvider: JwtRoomProvider,
        private val rsg: RandomStringGenerator
) {

    @Value("\${room_invite.chars}")
    private lateinit var invChars: String

    @Value("\${room_invite.length}")
    private var invLength: Int = 6

    @Value("\${room_count_for_one_user}")
    private var countRoomsUserOwns: Int = 1

    fun createRoom(title: String, ownerPojo: UserPojo): RoomPojo {
        ownerPojo.id?.let {
            if (roomPlaylistRepository.roomsByUserId(it).count() >= countRoomsUserOwns)
                throw CommonException(CommonError(ErrorCodes.MAX_ROOMS_OWNS,
                        "User created the maximum number of rooms."))
        }

        var roomPojo = defaultRoom(title, ownerPojo)

        roomPojo = roomPlaylistRepository.saveRoom(roomPojo)
        val roomId = roomPojo.id

        val token: String =
                jwtRoomProvider.generateToken(title, roomId.toString())
        roomPojo.token = token

        roomPlaylistRepository.updateRoom(roomPojo)

        roomPlaylistRepository.savePlaylist(
                PlaylistPojo(
                        title = "${title}_playlist",
                        description = "",
                        room_id = roomPojo.id,
                        listener_id = ownerPojo.id
                )
        )

        return roomPojo
    }

    fun userRooms(userPojo: UserPojo): MutableList<RoomPojo> {
        userPojo.id?.let {
            return roomPlaylistRepository.roomsByUserId(it)
        }
        throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC,
                "User dont has id."))
    }

    fun removeRoom(inviteOrToken: String, ownerPojo: UserPojo): MutableList<RoomPojo> {
        ownerPojo.id?.let {
            //automaticly removing playlist connected to room, when the room removed
            return roomPlaylistRepository.removeRoomByInvOrToken(inviteOrToken, it)
        }
        throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "Room owner dont has id")
    }

    fun findByInv(inv: String): RoomPojo? {
        return roomPlaylistRepository.findByInvite(inv)
    }

    fun getRoomFromToken(roomToken: String): RoomPojo? {
        val id: String = jwtRoomProvider.getRoomIdFromToken(roomToken)
        return roomPlaylistRepository.findById(Integer.valueOf(id))
    }

    fun defaultRoom(title: String, ownerPojo: UserPojo): RoomPojo {
        return RoomPojo(
                title = title,
                ownerId = ownerPojo.id,
                invite = createDefaultEmptyInvite(),
                rs_allow_votes = false,
                rs_priority_rarely_ordering_users = false,
                rs_song_owners_visible = false,
        )
    }

    private fun createEmptyInvite(chars: CharSequence, length: Int): String {
        var inv: String = rsg.generateString(chars, length)
        var roomEntity = findByInv(inv)
        while (roomEntity != null) {
            inv = rsg.generateString(chars, length)
            roomEntity = findByInv(inv)
        }
        return inv
    }

    private fun createDefaultEmptyInvite(): String {
        return createEmptyInvite(invChars, invLength)
    }
}