package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.extentions.toPublicSongInfo
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.core.CommonRequest
import com.sinhro.songturn.rest.core.CommonResponse
import com.sinhro.songturn.rest.request_response.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/playlist")
class PlaylistController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService
) {

    @PostMapping("/wannalisten")
    fun wannaListen(
            @RequestBody req: CommonRequest<ListenPlaylistReqData>
    ): CommonResponse<ListenPlaylistRespBody> {
        req.data?.let { listenPlaylistReqData ->
            return CommonResponse.buildSuccess(ListenPlaylistRespBody(
                    roomAndPlaylistService.userWannaListen(listenPlaylistReqData)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/stoplisten")
    fun stopListen(
            @RequestBody req: CommonRequest<StopListenPlaylistReqData>
    ): CommonResponse<StopListenPlaylistRespBody> {
        req.data?.let {
            return CommonResponse.buildSuccess(StopListenPlaylistRespBody(
                    roomAndPlaylistService.userDontWantListen(it)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/getsongs")
    fun getPlaylistSongs(
            @RequestBody req: CommonRequest<GetSongsReqData>
    ): CommonResponse<GetSongsRespBody> {
        req.data?.let { data ->
            return CommonResponse.buildSuccess(GetSongsRespBody(
                    roomAndPlaylistService.getSongs(data.roomToken, data.playlistTitle)
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/ordersong")
    fun orderSong(
            @RequestBody req: CommonRequest<OrderSongReqData>
    ): CommonResponse<OrderSongRespBody> {
        req.data?.let { data ->
            val songInfo = roomAndPlaylistService.orderSong(data)

            return CommonResponse.buildSuccess(OrderSongRespBody(songInfo.toPublicSongInfo()))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/setCurrentPlayingSong")
    fun setCurrentPlayingSong(
            @RequestBody reqSet: CommonRequest<SetCurrentPlayingSongReqData>
    ): CommonResponse<SetCurrentPlayingSongRespBody> {
        reqSet.data?.let {
            val updatedPlaylistInfo = roomAndPlaylistService.setCurrentPlayingSong(
                    it.roomToken, it.playlistTitle, it.songId
            )
            return CommonResponse.buildSuccess(SetCurrentPlayingSongRespBody(updatedPlaylistInfo))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/currentPlayingSong")
    fun currentPlayingSong(
            @RequestBody reqSet: CommonRequest<CurrentPlayingSongReqData>
    ): CommonResponse<CurrentPlayingSongRespBody> {
        reqSet.data?.let {
            val updatedPlaylistInfo = roomAndPlaylistService.currentPlayingSong(
                    it.roomToken, it.playlistTitle
            )
            return CommonResponse.buildSuccess(CurrentPlayingSongRespBody(
                    updatedPlaylistInfo
            ))
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }
}