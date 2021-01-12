package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.pojos.RoomPojo
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.stream.Collectors

@RestController
class RoomController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService,
        private val userService: UserService
) {

    @GetMapping("/room/myrooms")
    fun myRooms(): CommonResponse<OwnedRoomsRespBody> {
        val currentUser = userService.currentUser()
        val userRooms = roomAndPlaylistService.userRooms(currentUser)
                .stream()
                .map { RoomPojo.toRoomInfo(it) }
                .collect(Collectors.toList())
        return CommonResponse.buildSuccess(OwnedRoomsRespBody(
                userRooms
        ))
    }

    @PostMapping("/room/create")
    fun createRoom(
            @RequestBody req: CommonRequest<CreateRoomReqData>
    ): CommonResponse<CreateRoomRespBody> {
        req.data?.let {
            it.title?.let {
                val currentUser = userService.currentUser()
                val createdRoom = roomAndPlaylistService.createRoom(it, currentUser)
                return CommonResponse.buildSuccess(CreateRoomRespBody(
                        RoomPojo.toRoomInfo(createdRoom)
                ))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/room/remove")
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

    @PostMapping("/room/enter")
    fun enterRoom(
            @RequestBody req: CommonRequest<EnterRoomReqData>
    ): CommonResponse<EnterRoomRespBody> {
        req.data?.let {
            it.inviteCode?.let {
                roomAndPlaylistService.findByInv(it)?.let {
                    return CommonResponse.buildSuccess(EnterRoomRespBody(
                            RoomPojo.toRoomInfo(it)
                    ))
                }
                throw CommonException(
                        CommonError(ErrorCodes.ROOM_NOT_FOUND, "Room not found"))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/room/change")
    fun changeRoom() {

    }
}