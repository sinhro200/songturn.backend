package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.pojos.PlaylistPojo
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.backend.service.UserService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.ListenPlaylistReqData
import com.sinhro.songturn.rest.request_response.ListenPlaylistRespBody
import com.sinhro.songturn.rest.request_response.StopListenPlaylistReqData
import com.sinhro.songturn.rest.request_response.StopListenPlaylistRespBody
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/playlist")
class PlaylistController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService,
        private val userService: UserService,
) {

    @PostMapping("/wannalisten")
    fun wannaListen(
            @RequestBody req: CommonRequest<ListenPlaylistReqData>
    ): CommonResponse<ListenPlaylistRespBody> {
        req.data?.let {
            if (it.playlistTitle != null && it.roomToken != null) {
                val room = roomAndPlaylistService.getRoomFromToken(it.roomToken!!)
                val playlist = roomAndPlaylistService.getPlaylist(room, it.playlistTitle!!)
                val currentUser = userService.currentUser()
                if (playlist.listener_id != null && playlist.listener_id != currentUser.id) {
                    throw CommonException(CommonError(ErrorCodes.PLAYLIST_ALREADY_HAS_LISTENER))
                }
                currentUser.id?.let { id ->
                    val updatedPlaylistPojo = roomAndPlaylistService.setListener(playlist, id)
                    return CommonResponse.buildSuccess(ListenPlaylistRespBody(
                            PlaylistPojo.toPlaylistInfo(updatedPlaylistPojo)
                    ))
                }
                throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "User dont has id")
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/stoplisten")
    fun stopListen(
            @RequestBody req: CommonRequest<StopListenPlaylistReqData>
    ): CommonResponse<StopListenPlaylistRespBody> {
        req.data?.let {
            if (it.playlistTitle != null && it.roomToken != null) {
                val room = roomAndPlaylistService.getRoomFromToken(it.roomToken!!)
                val playlist = roomAndPlaylistService.getPlaylist(room, it.playlistTitle!!)
                val updatedPlaylistPojo = roomAndPlaylistService.removeListener(playlist)
                return CommonResponse.buildSuccess(StopListenPlaylistRespBody(
                        PlaylistPojo.toPlaylistInfo(updatedPlaylistPojo)
                ))

            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

}