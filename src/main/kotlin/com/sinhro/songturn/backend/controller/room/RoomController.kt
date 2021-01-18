package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.service.RoomActionService
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/room")
class RoomController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService,
        private val userService: UserService,
        private val roomActionService: RoomActionService,
) {

    @GetMapping("/myrooms")
    fun myRooms(): CommonResponse<MyRoomsRespBody> {
        return CommonResponse.buildSuccess(MyRoomsRespBody(
                roomAndPlaylistService.userRooms()
        ))
    }

    @PostMapping("/create")
    fun createRoom(
            @RequestBody req: CommonRequest<CreateRoomReqData>
    ): CommonResponse<CreateRoomRespBody> {
        req.data?.let {
            return CommonResponse.buildSuccess(CreateRoomRespBody(
                    roomAndPlaylistService.createRoom(it)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/remove")
    fun removeRoom(
            @RequestBody req: CommonRequest<RemoveRoomReqData>
    ): CommonResponse<RemoveRoomRespBody> {
        req.data?.let {
            val removedRooms = roomAndPlaylistService.removeRoom(it)
            return CommonResponse.buildSuccess(RemoveRoomRespBody(
                    removedRooms.count()
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/enter")
    fun enterRoom(
            @RequestBody req: CommonRequest<EnterRoomReqData>
    ): CommonResponse<EnterRoomRespBody> {
        req.data?.let {
            return CommonResponse.buildSuccess(EnterRoomRespBody(
                    roomAndPlaylistService.enterRoom(it)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/leave")
    fun leaveRoom(
            @RequestBody req: CommonRequest<LeaveRoomReqData>
    ): CommonResponse<LeaveRoomRespBody> {
        req.data?.let {
            roomAndPlaylistService.leaveRoom(it)
            return CommonResponse.buildSuccess(LeaveRoomRespBody())
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/whatchanged")
    fun shouldUpdate(
            @RequestBody req: CommonRequest<WhatShouldUpdateReqData>
    ): CommonResponse<WhatShouldUpdateRespBody> {
        req.data?.let {
            return CommonResponse.buildSuccess(WhatShouldUpdateRespBody(
                    roomAndPlaylistService.whatShouldUpdate(it)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/users")
    fun usersInRoom(
            @RequestBody req: CommonRequest<UsersInRoomReqData>
    ): CommonResponse<UsersInRoomRespBody> {
        req.data?.let {
            return CommonResponse.buildSuccess(UsersInRoomRespBody(
                    roomAndPlaylistService.getUsersInRoom(it)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/change")
    fun changeRoom() {
        //ToDo
    }

    @PostMapping("/playlists")
    fun playlistsInRoom(
            @RequestBody req: CommonRequest<GetPlaylistsReqData>
    ): CommonResponse<GetPlaylistsRespBody> {
        req.data?.let { data ->
            return CommonResponse.buildSuccess(
                    GetPlaylistsRespBody(
                            roomAndPlaylistService.getRoomPlaylists(data)
                    )
            )

        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
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

}