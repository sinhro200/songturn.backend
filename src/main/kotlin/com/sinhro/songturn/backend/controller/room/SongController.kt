package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.pojos.SongPojo
import com.sinhro.songturn.backend.service.NimuscService
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
import com.sinhro.songturn.backend.service.SongService
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
import java.util.stream.Collectors

@RestController
@RequestMapping("/song")
class SongController @Autowired constructor(
        private val roomAndPlaylistService: RoomAndPlaylistService,
        private val songService: SongService,
        private val nimuscService: NimuscService
) {

    @PostMapping("/get")
    fun getPlaylistSongs(
            @RequestBody req: CommonRequest<GetSongsReqData>
    ): CommonResponse<GetSongsRespBody> {
        req.data?.let { data ->
            if (data.roomToken != null && data.playlistTitle != null) {
                val roomToken = data.roomToken!!
                val playlistTitle = data.playlistTitle!!
                val room = roomAndPlaylistService.getRoomFromToken(roomToken)
                val playlist = roomAndPlaylistService.getPlaylist(room, playlistTitle)
                val songs = songService.getSongsInPlaylist(playlist)
                        .stream()
                        .map { SongPojo.toSongInfo(it) }
                        .collect(Collectors.toList())
                return CommonResponse.buildSuccess(GetSongsRespBody(songs))
            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }

    @PostMapping("/order")
    fun orderSong(
            @RequestBody req: CommonRequest<OrderSongReqData>
    ): CommonResponse<OrderSongRespBody> {
        req.data?.let { data ->
            if (data.roomToken != null && data.songLink != null) {
                val roomToken = data.roomToken!!
                val playlistTitle = data.playlistTitle!!
                val room = roomAndPlaylistService.getRoomFromToken(roomToken)
                val playlist = roomAndPlaylistService.getPlaylist(room, playlistTitle)

                val songInfo = nimuscService.getAudio(data.songLink!!, data.musicServiceAuthInfo)


            }
        }
        throw CommonException(CommonError(ErrorCodes.REQUEST_DATA_EXC))
    }
}