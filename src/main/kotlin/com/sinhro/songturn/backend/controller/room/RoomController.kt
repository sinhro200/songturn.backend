package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.pojos.PlaylistPojo
import com.sinhro.songturn.backend.pojos.RoomPojo
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.service.RoomActionService
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.model.RoomActionInfo
import com.sinhro.songturn.rest.model.RoomActionType
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.stream.Collectors

@RestController
@RequestMapping("/room")
class RoomController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService,
        private val userService: UserService,
        private val roomActionService: RoomActionService,
) {

    @GetMapping("/myrooms")
    fun myRooms(): CommonResponse<MyRoomsRespBody> {
        val currentUser = userService.currentUser()
        val userRooms = roomAndPlaylistService.userRooms(currentUser)
                .stream()
                .map { RoomPojo.toRoomInfo(it) }
                .collect(Collectors.toList())
        return CommonResponse.buildSuccess(MyRoomsRespBody(
                userRooms
        ))
    }

    @PostMapping("/create")
    fun createRoom(
            @RequestBody req: CommonRequest<CreateRoomReqData>
    ): CommonResponse<CreateRoomRespBody> {
        req.data?.let {
            it.title?.let {
                val currentUser = userService.currentUser()
                val createdRoom = roomAndPlaylistService.createRoom(it, currentUser)

                roomActionService.initUserCreatedRoomActions(currentUser,createdRoom)
                roomActionService.initUserActions(currentUser,createdRoom)
                roomActionService.userAction(currentUser,createdRoom,RoomActionType.ROOM_INFO_UPDATED)

                return CommonResponse.buildSuccess(CreateRoomRespBody(
                        RoomPojo.toRoomInfo(createdRoom)
                ))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/remove")
    fun removeRoom(
            @RequestBody req: CommonRequest<RemoveRoomReqData>
    ): CommonResponse<RemoveRoomRespBody> {
        req.data?.let {
            it.inviteOrToken?.let {
                val currentUser = userService.currentUser()
                val removedRooms = roomAndPlaylistService.removeRoom(it, currentUser)
                return CommonResponse.buildSuccess(RemoveRoomRespBody(
                        removedRooms.count()
                ))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/enter")
    fun enterRoom(
            @RequestBody req: CommonRequest<EnterRoomReqData>
    ): CommonResponse<EnterRoomRespBody> {
        req.data?.let {
            it.inviteCode?.let {
                roomAndPlaylistService.findByInv(it).let { room ->
                    val user = userService.currentUser()
                    userService.userEnteredRoom(user, room)

                    roomActionService.initUserActions(user, room)
                    roomActionService.roomAction(
                            user, room,
                            RoomActionType.ROOM_INFO_UPDATED
                    )

                    return CommonResponse.buildSuccess(EnterRoomRespBody(
                            RoomPojo.toRoomInfo(room)
                    ))

                }
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/leave")
    fun leaveRoom(
            @RequestBody req: CommonRequest<LeaveRoomReqData>
    ): CommonResponse<LeaveRoomRespBody> {
        req.data?.let {
            it.roomToken?.let { token ->
                val room = roomAndPlaylistService.getRoomFromToken(token)
                userService.userLeftRoom(userService.currentUser(), room)
                return CommonResponse.buildSuccess(LeaveRoomRespBody())
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/whatchanged")
    fun shouldUpdate(
            @RequestBody req: CommonRequest<WhatShouldUpdateReqData>
    ): CommonResponse<WhatShouldUpdateRespBody> {
        req.data?.let {
            it.roomToken?.let { token ->
                val room = roomAndPlaylistService.getRoomFromToken(token)

                val actionTypes = roomActionService.whatShouldUpdate(
                        userService.currentUser(),
                        room
                )

                return CommonResponse.buildSuccess(WhatShouldUpdateRespBody(
                        actionTypes.stream()
                                .map { RoomActionInfo(it) }
                                .collect(Collectors.toList())
                ))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/users")
    fun usersInRoom(
            @RequestBody req: CommonRequest<UsersInRoomReqData>
    ): CommonResponse<UsersInRoomRespBody> {
        req.data?.let {
            it.roomToken?.let { token ->
                val user = userService.currentUser()
                val room = roomAndPlaylistService.getRoomFromToken(token)
                val users = userService.usersInRoom(room)
                        .stream()
                        .map { UserPojo.toPublicUserInfo(it) }
                        .collect(Collectors.toList())
                roomActionService.userAction(
                        user,room,RoomActionType.ROOM_USERS_UPDATED
                )
                return CommonResponse.buildSuccess(UsersInRoomRespBody(users))
            }
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
            data.roomToken?.let {
                val user = userService.currentUser()
                val room = roomAndPlaylistService.getRoomFromToken(it)
                val playlists = roomAndPlaylistService.getAllRoomPlaylists(
                        room
                )
                roomActionService.userAction(
                        user,room,RoomActionType.PLAYLIST_INFO_UPDATED
                )
                return CommonResponse.buildSuccess(
                        GetPlaylistsRespBody(
                                playlists.stream()
                                        .map { PlaylistPojo.toPlaylistInfo(it) }
                                        .collect(Collectors.toList())
                        )
                )
            }
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