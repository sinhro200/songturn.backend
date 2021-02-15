package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.rest.model.PlaylistInfo
import com.sinhro.songturn.rest.model.PlaylistSongs
import com.sinhro.songturn.rest.model.RoomInfo
import com.sinhro.songturn.rest.model.SongInfo
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService
) {

    @GetMapping("/myrooms")
    fun myRooms(): MyRoomsRespBody {
        return MyRoomsRespBody(
                roomAndPlaylistService.userRooms()
        )
    }

    @PostMapping("/create")
    fun createRoom(
            @RequestBody data: CreateRoomReqData
    ): CreateRoomRespBody {
        return CreateRoomRespBody(
                roomAndPlaylistService.createRoom(data)
        )

    }

    @PostMapping("/remove")
    fun removeRoom(
            @RequestBody data: RemoveRoomReqData
    ): RemoveRoomRespBody {

        val removedRooms = roomAndPlaylistService.removeRoom(data)
        return RemoveRoomRespBody(
                removedRooms.count()
        )
    }

    @PostMapping("/enter")
    fun enterRoom(
            @RequestBody data: EnterRoomReqData
    ): EnterRoomRespBody {
        return EnterRoomRespBody(
                roomAndPlaylistService.enterRoom(data)
        )
    }

    @PostMapping("/leave")
    fun leaveRoom(
            @RequestBody data: LeaveRoomReqData
    ): LeaveRoomRespBody {
        roomAndPlaylistService.leaveRoom(data)
        return LeaveRoomRespBody()
    }

    @PostMapping("/info")
    fun roomInfo(
            @RequestBody data: RoomInfoReqData
    ): RoomInfo {
        return roomAndPlaylistService.roomInfo(data.roomToken)
    }

    @PostMapping("/whatchanged")
    fun shouldUpdate(
            @RequestBody data: WhatShouldUpdateReqData
    ): WhatShouldUpdateRespBody {
        return WhatShouldUpdateRespBody(
                roomAndPlaylistService.whatShouldUpdate(data)
        )
    }

    @PostMapping("/users")
    fun usersInRoom(
            @RequestBody data: UsersInRoomReqData
    ): UsersInRoomRespBody {
        return UsersInRoomRespBody(
                roomAndPlaylistService.getUsersInRoom(data.roomToken)
        )
    }

    @PostMapping("/change")
    fun changeRoom() {
        //ToDo
    }

    @PostMapping("/playlists")
    fun playlistsInRoom(
            @RequestBody data: GetPlaylistsReqData
    ): GetPlaylistsRespBody {
        return GetPlaylistsRespBody(
                        roomAndPlaylistService.roomPlaylists(data.roomToken))

    }

    @PostMapping("/addPlaylist")
    fun addPlaylist() {
        //ToDO
    }

    @PostMapping("/removePlaylist")
    fun removePlaylist() {
        //ToDO
    }

    @PostMapping("/changePlaylist")
    fun changePlaylist() {
        //ToDO
    }

    @PostMapping("/fullroominfo")
    fun fullRoomInfo(
            @RequestBody roomInfoReqData: RoomInfoReqData
    ): FullRoomInfoRespBody {
        val room: RoomInfo = roomAndPlaylistService.roomInfo(roomInfoReqData.roomToken)
        val playlists: List<PlaylistInfo> =
                roomAndPlaylistService.roomPlaylists(roomInfoReqData.roomToken)

        val songsInPlaylists = mutableMapOf<Int, PlaylistSongs>()
        playlists.forEach { playlist ->
            songsInPlaylists[playlist.id] = roomAndPlaylistService.getSongs(roomInfoReqData.roomToken, playlist.title)
        }
        val users = roomAndPlaylistService.getUsersInRoom(roomInfoReqData.roomToken)

        return FullRoomInfoRespBody(
                        room, users, playlists, songsInPlaylists
                )

    }
}