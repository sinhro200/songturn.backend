package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.extentions.*
import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Playlist as PlaylistPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.backend.tables.pojos.Song as SongPojo
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
        private val randomStringGenerator: RandomStringGenerator,
        private val roomActionRepository: RoomActionRepository
) {

    @Value("\${room_invite.chars}")
    private lateinit var invChars: String

    @Value("\${room_invite.length}")
    private var invLength: Int = 6

    @Value("\${room_count_for_one_user}")
    private var countRoomsUserOwns: Int = 1

    fun userWannaListen(listenPlaylistReqData: ListenPlaylistReqData): PlaylistInfo {
        val room = findRoomByToken(listenPlaylistReqData.roomToken)
        val playlist = roomPlaylistRepository.getPlaylistInRoom(
                room.id,
                listenPlaylistReqData.playlistTitle
        ) ?: throw CommonException(CommonError(ErrorCodes.PLAYLIST_NOT_FOUND))
        val currentUser = userService.currentUser()

        if (playlist.listenerId != null && playlist.listenerId != currentUser.id)
            throw CommonException(CommonError(
                    ErrorCodes.PLAYLIST_ALREADY_HAS_LISTENER))

        return roomPlaylistRepository.setListenerId(playlist, currentUser.id).toPlaylistInfo()
    }

    fun userDontWantListen(stopListenPlaylistReqData: StopListenPlaylistReqData)
            : PlaylistInfo {
        val room = findRoomByToken(stopListenPlaylistReqData.roomToken)
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
                RoomPojo(
                        null,
                        createDefaultEmptyInvite(),
                        null,
                        createRoomReqData.title,
                        currentUser.id,
                        createRoomReqData.roomSettings?.priorityRarelyOrderingUsers,
                        createRoomReqData.roomSettings?.allowVotes,
                        createRoomReqData.roomSettings?.songOwnersVisible,
                        createRoomReqData.roomSettings?.anyCanListen
                )
        )
        val newRoomPojo = RoomPojo(
                null, null,
                jwtRoomProvider.generateToken(
                        roomPojo.title, roomPojo.id.toString()
                ),
                null, null, null,
                null, null, null
        )

        val updatedRoomPojo = roomPlaylistRepository.updateRoom(roomPojo, newRoomPojo)

        //RoomAction
//        roomActionRepository.roomCreated(owner,createdRoom)
        roomActionRepository.initUserChangeActions(currentUser, updatedRoomPojo)
        roomActionRepository.initUserUpdateActions(currentUser, updatedRoomPojo)
        roomActionRepository.userUpdateAction(
                currentUser,
                updatedRoomPojo,
                RoomActionType.ROOM_INFO
        )


        createPlaylist(updatedRoomPojo, currentUser)
        return updatedRoomPojo.toRoomInfo()
    }

    fun roomInfo(
            token: String
    ): RoomInfo {
        val room = findRoomByToken(token)
        val user = userService.currentUser()

        roomActionRepository.userUpdateAction(
                user, room, RoomActionType.ROOM_INFO
        )
        return room.toRoomInfo()
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
        removedRooms.forEach {
            roomActionRepository.removeAllActionsByRoom(it)
        }
        return removedRooms.map { it.toRoomInfo() }
    }

    fun enterRoom(
            enterRoomReqData: EnterRoomReqData
    ): RoomInfo {
        val room = roomPlaylistRepository.findByInvite(enterRoomReqData.inviteCode)
                ?: throw CommonException(CommonError(ErrorCodes.ROOM_NOT_FOUND))

        val user = userService.currentUser()

        userService.setUserInRoom(user, room)

        //RoomAction
        roomActionRepository.initUserUpdateActions(user, room)
        roomActionRepository.userUpdateAction(
                user, room,
                RoomActionType.ROOM_INFO
        )
        roomActionRepository.changeAction(
                user, room,
                RoomActionType.ROOM_USERS,
                true
        )

        return room.toRoomInfo()
    }

    fun updateRoominfo(

    ) {

    }

    fun leaveRoom(
            leaveRoomReqData: LeaveRoomReqData
    ): RoomInfo {
        val room = findRoomByToken(leaveRoomReqData.roomToken)
        val user = userService.currentUser()

        userService.setUserOutRoom(user, room)

        return room.toRoomInfo()
    }

    fun whatShouldUpdate(whatShouldUpdateReqData: WhatShouldUpdateReqData)
            : List<RoomActionInfo> {
        val room = findRoomByToken(whatShouldUpdateReqData.roomToken)

        //RoomAction
        val actionTypes = roomActionRepository.whatShouldUpdate(
                userService.currentUser(),
                room
        )
        return actionTypes.map { RoomActionInfo(it) }
    }

    fun getUsersInRoom(
            roomToken: String
    ): List<PublicUserInfo> {
        val user = userService.currentUser()
        val room = findRoomByToken(roomToken)

        val usersInRoomPublicInfos = userService.usersInRoom(room).map { it.toPublicUserInfo() }
        //RoomAction
        roomActionRepository.userUpdateAction(
                user, room, RoomActionType.ROOM_USERS
        )
        return usersInRoomPublicInfos
    }

    fun roomPlaylists(
            roomToken: String
    ): List<PlaylistInfo> {
        val user = userService.currentUser()
        val room = findRoomByToken(roomToken)
        val playlists = roomPlaylistRepository.getAllPlaylistsInRoom(room.id)
        //RoomAction
        roomActionRepository.userUpdateAction(
                user, room, RoomActionType.PLAYLIST_INFO
        )
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

    private fun findRoomByToken(roomToken: String): RoomPojo {
        val id: String = jwtRoomProvider.getRoomIdFromToken(roomToken)
        return roomPlaylistRepository.findRoomById(Integer.valueOf(id)) ?: throw CommonException(
                CommonError(ErrorCodes.ROOM_NOT_FOUND)
        )
    }

    fun getSongs(
            roomToken: String,
            playlistTitle: String
    ): List<SongInfo> {
        val room = findRoomByToken(roomToken)
        val playlist = getRoomPlaylist(room, playlistTitle)

        val songPojos = songRepository.songsInPlaylist(playlist.id)
        val user = userService.currentUser()
        roomActionRepository.userUpdateAction(
                user, room,
                RoomActionType.PLAYLIST_SONGS
        )
        return if (room.rsAnyCanListen || playlist.listenerId == user.id)
            songPojos.map { it.toFullSongInfo() }
        else
            songPojos.map { it.toPublicSongInfo() }
    }



    fun orderSong(
            orderSongReqData: OrderSongReqData
    ): SongInfo {
        val room = findRoomByToken(orderSongReqData.roomToken)
        val playlist = getRoomPlaylist(room, orderSongReqData.playlistTitle)
        val user = userService.currentUser()

        val songInfo = nimuscService.getAudio(
                orderSongReqData.songLink,
                orderSongReqData.musicServiceAuthInfo
        )

        val savedSong = songRepository.saveSong(SongPojo(
                null,
                songInfo.artist, songInfo.title, songInfo.link, songInfo.duration,
                null, songInfo.expiresAt,
                playlist.id, user.id, null, orderSongReqData.songLink
        ), playlist.id)

        //RoomAction
        roomActionRepository.changeAction(
                user, room, RoomActionType.PLAYLIST_SONGS
        )

        return if (room.rsAnyCanListen || playlist.listenerId == user.id)
            savedSong.toFullSongInfo()
        else
            savedSong.toPublicSongInfo()
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