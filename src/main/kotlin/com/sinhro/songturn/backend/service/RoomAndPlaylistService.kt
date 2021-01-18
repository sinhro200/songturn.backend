package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.extentions.toPlaylistInfo
import com.sinhro.songturn.backend.extentions.toPublicUserInfo
import com.sinhro.songturn.backend.extentions.toRoomInfo
import com.sinhro.songturn.backend.extentions.toSongInfo
import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Playlist as PlaylistPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.backend.providers.JwtRoomProvider
import com.sinhro.songturn.backend.repository.RoomPlaylistRepository
import com.sinhro.songturn.backend.repository.SongRepository
import com.sinhro.songturn.backend.utils.RandomStringGenerator
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.*
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RoomAndPlaylistService @Autowired constructor(
        private val userService: UserService,
        private val roomPlaylistRepository: RoomPlaylistRepository,
        private val songRepository: SongRepository,
        private val nimuscService: NimuscService,
        private val jwtRoomProvider: JwtRoomProvider,
        private val randomStringGenerator: RandomStringGenerator
) {

    @Value("\${room_invite.chars}")
    private lateinit var invChars: String

    @Value("\${room_invite.length}")
    private var invLength: Int = 6

    @Value("\${room_count_for_one_user}")
    private var countRoomsUserOwns: Int = 1

    fun userWannaListen(listenPlaylistReqData: ListenPlaylistReqData): PlaylistInfo {
        val room = getRoomFromToken(listenPlaylistReqData.roomToken)
        val playlist = roomPlaylistRepository.getPlaylistInRoom(
                room.id,
                listenPlaylistReqData.playlistTitle
        ) ?: throw CommonException(CommonError(ErrorCodes.PLAYLIST_NOT_FOUND))
        val currentUser = userService.currentUser()

        if (playlist.listenerId != null && playlist.listenerId != currentUser.id)
            throw CommonException(CommonError(ErrorCodes.PLAYLIST_ALREADY_HAS_LISTENER))

        return roomPlaylistRepository.setListenerId(playlist, currentUser.id).toPlaylistInfo()
    }

    fun userDontWantListen(stopListenPlaylistReqData: StopListenPlaylistReqData)
            : PlaylistInfo {
        val room = getRoomFromToken(stopListenPlaylistReqData.roomToken)
        val playlist = roomPlaylistRepository.getPlaylistInRoom(room.id, stopListenPlaylistReqData.playlistTitle)
                ?: throw CommonException(CommonError(ErrorCodes.PLAYLIST_NOT_FOUND))
        return roomPlaylistRepository.clearListenerId(playlist).toPlaylistInfo()
    }

    fun userRooms(): List<RoomInfo> {
        val currentUser = userService.currentUser()
        return roomPlaylistRepository.roomsByUserId(currentUser.id)
                .map { it.toRoomInfo() }
    }

    fun createRoom(createRoomReqData: CreateRoomReqData):
            RoomInfo {
        val currentUser = userService.currentUser()

        if (roomPlaylistRepository.roomsByUserId(currentUser.id).count() >= countRoomsUserOwns)
            throw CommonException(CommonError(ErrorCodes.MAX_ROOMS_OWNS,
                    "User created the maximum number of rooms."))

        val roomPojo = roomPlaylistRepository.saveRoom(
                defaultRoom(createRoomReqData.title, currentUser)
        )
        val newRoomPojo = RoomPojo(
                null, null,
                jwtRoomProvider.generateToken(
                        roomPojo.title, roomPojo.id.toString()
                ),
                null, null, null, null, null
        )

        val updatedRoomPojo = roomPlaylistRepository.updateRoom(roomPojo, newRoomPojo)

        //ToDO RoomAction
        //roomActionService.roomCreated(owner,createdRoom)
//        roomActionService.initUserCreatedRoomActions(currentUser, createdRoom)
//        roomActionService.initUserActions(currentUser, createdRoom)
//        roomActionService.userAction(currentUser, createdRoom, RoomActionType.ROOM_INFO_UPDATED)


        createPlaylist(updatedRoomPojo, currentUser)
        return updatedRoomPojo.toRoomInfo()
    }

    private fun createPlaylist(roomPojo: RoomPojo, ownerUserPojo: UserPojo) {
        roomPlaylistRepository.savePlaylist(
                PlaylistPojo(
                        null,
                        defaultPlaylistName(roomPojo.title),
                        "",
                        roomPojo.id,
                        null,
                        ownerUserPojo.id
                )
        )
    }

    fun removeRoom(removeRoomReqData: RemoveRoomReqData): List<RoomInfo> {
        val currentUser = userService.currentUser()
        val removedRooms = roomPlaylistRepository.removeRoomByInvOrToken(
                removeRoomReqData.inviteOrToken,
                currentUser.id
        )
        return removedRooms.map { it.toRoomInfo() }
    }

//    fun createRoom(title: String, ownerPojo: UserPojo): RoomPojo {
//
//        if (roomPlaylistRepository.roomsByUserId(ownerPojo.id).count() >= countRoomsUserOwns)
//            throw CommonException(CommonError(ErrorCodes.MAX_ROOMS_OWNS,
//                    "User created the maximum number of rooms."))
//
//
//        var roomPojo = defaultRoom(title, ownerPojo)
//
//        roomPojo = roomPlaylistRepository.saveRoom(roomPojo)
//        val roomId = roomPojo.id
//
//        val token: String =
//                jwtRoomProvider.generateToken(title, roomId.toString())
//        roomPojo.token = token
//
//        roomPlaylistRepository.updateRoom(roomPojo)
//
//        roomPlaylistRepository.savePlaylist(
//                PlaylistPojo(
//                        title = defaultPlaylistName(title),
//                        description = "",
//                        room_id = roomPojo.id,
//                        listener_id = ownerPojo.id
//                )
//        )
//
//        return roomPojo
//    }

    //    fun removeRoom(inviteOrToken: String, ownerPojo: UserPojo): MutableList<RoomPojo> {
//        ownerPojo.id?.let {
//            //automaticly removing playlist connected to room, when the room removed
//            return roomPlaylistRepository.removeRoomByInvOrToken(inviteOrToken, it)
//        }
//        throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "Room owner dont has id")
//    }

    fun enterRoom(
            enterRoomReqData: EnterRoomReqData
    ): RoomInfo {
        val room = roomPlaylistRepository.findByInvite(enterRoomReqData.inviteCode)
                ?: throw CommonException(CommonError(ErrorCodes.ROOM_NOT_FOUND))

        val user = userService.currentUser()

        userService.setUserInRoom(user, room)
        //ToDo RoomAction
//        roomActionService.initUserActions(user, room)
//        roomActionService.roomAction(
//                user, room,
//                RoomActionType.ROOM_INFO_UPDATED
//        )

        return room.toRoomInfo()
    }

    fun leaveRoom(
            leaveRoomReqData: LeaveRoomReqData
    ): RoomInfo {
        val room = getRoomFromToken(leaveRoomReqData.roomToken)
        val user = userService.currentUser()

        userService.setUserOutRoom(user, room)

        return room.toRoomInfo()
    }

    fun whatShouldUpdate(whatShouldUpdateReqData: WhatShouldUpdateReqData)
            : List<RoomActionInfo> {
        val room = getRoomFromToken(whatShouldUpdateReqData.roomToken)

        //ToDo RoomAction
//        val actionTypes = roomActionService.whatShouldUpdate(
//                userService.currentUser(),
//                room
//        )
//        return actionTypes.stream()
//                .map { RoomActionInfo(it) }
//                .collect(Collectors.toList())

        return emptyList()
    }

    fun getUsersInRoom(
            usersInRoomReqData: UsersInRoomReqData
    ): List<PublicUserInfo> {
        val user = userService.currentUser()
        val room = getRoomFromToken(usersInRoomReqData.roomToken)

        val usersInRoomPublicInfos = userService.usersInRoom(room).map { it.toPublicUserInfo() }
        //ToDo RoomAction
//        roomActionService.userAction(
//                user, room, RoomActionType.ROOM_USERS_UPDATED
//        )
        return usersInRoomPublicInfos
    }

    fun getRoomPlaylists(
            getPlaylistsReqData: GetPlaylistsReqData
    ): List<PlaylistInfo> {
        val user = userService.currentUser()
        val room = getRoomFromToken(getPlaylistsReqData.roomToken)
        val playlists = roomPlaylistRepository.getAllPlaylistsInRoom(room.id)
        //ToDo RoomAction
//        roomActionService.userAction(
//                user, room, RoomActionType.PLAYLIST_INFO_UPDATED
//        )
        return playlists.map { it.toPlaylistInfo() }
    }

    fun getRoomPlaylist(roomPojo: RoomPojo, playlistTitle: String): PlaylistPojo {
        return roomPlaylistRepository.getPlaylistInRoom(roomPojo.id, playlistTitle)
                ?: throw CommonException(CommonError(ErrorCodes.PLAYLIST_NOT_FOUND))
    }

    fun findRoomUserIn(
    ): RoomInfo? {
        val user = userService.currentUser()
        val room = user.roomId?.let { roomPlaylistRepository.findRoomById(it) }
        return room?.toRoomInfo()
    }

    fun getRoomFromToken(roomToken: String): RoomPojo {
        val id: String = jwtRoomProvider.getRoomIdFromToken(roomToken)
        return roomPlaylistRepository.findRoomById(Integer.valueOf(id)) ?: throw CommonException(
                CommonError(ErrorCodes.ROOM_NOT_FOUND)
        )
    }

    fun getSongs(
            getSongsReqData: GetSongsReqData
    ): List<SongInfo> {
        val room = getRoomFromToken(getSongsReqData.roomToken)
        val playlist = getRoomPlaylist(room, getSongsReqData.playlistTitle)
        return songRepository.getSongsInPlaylist(playlist.id)
                .map { it.toSongInfo() }
    }

    fun orderSong(
            orderSongReqData: OrderSongReqData
    ) : SongInfo{
        val room = getRoomFromToken(orderSongReqData.roomToken)
        val playlist = getRoomPlaylist(room, orderSongReqData.playlistTitle)

        val songInfo = nimuscService.getAudio(
                orderSongReqData.songLink,
                orderSongReqData.musicServiceAuthInfo
        )

        //ToDo order song
        // save song in repo
        return songInfo
    }

    private fun defaultRoom(title: String, ownerPojo: UserPojo): RoomPojo {
        return RoomPojo(
                null,
                createDefaultEmptyInvite(),
                null,
                title,
                ownerPojo.id,
                false, false, false
        )
    }

    private fun createEmptyInvite(chars: CharSequence, length: Int): String {
        var inv: String = randomStringGenerator.generateString(chars, length)
        var roomEntity = roomPlaylistRepository.findByInvite(inv)
        while (roomEntity != null) {
            inv = randomStringGenerator.generateString(chars, length)
            roomEntity = roomPlaylistRepository.findByInvite(inv)
        }
        return inv
    }

    private fun createDefaultEmptyInvite(): String {
        return createEmptyInvite(invChars, invLength)
    }

    private fun defaultPlaylistName(roomTitle: String): String {
        return "${roomTitle}_playlist"
    }
}