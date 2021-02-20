package com.sinhro.songturn.backend.controller.room

import com.sinhro.songturn.backend.extentions.toPublicSongInfo
import com.sinhro.songturn.backend.service.RoomAndPlaylistService
//import com.sinhro.songturn.rest.core.CommonRequest
//import com.sinhro.songturn.rest.core.ResponseBody
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
            @RequestBody req: ListenPlaylistReqData
    ): ListenPlaylistRespBody {
        return ListenPlaylistRespBody(
                roomAndPlaylistService.userWannaListen(req)
        )
    }

    @PostMapping("/stoplisten")
    fun stopListen(
            @RequestBody req: StopListenPlaylistReqData
    ): StopListenPlaylistRespBody {

        return StopListenPlaylistRespBody(
                roomAndPlaylistService.userDontWantListen(req)
        )

    }

    @PostMapping("/getsongs")
    fun getPlaylistSongs(
            @RequestBody data: PlaylistSongsReqData
    ): PlaylistSongsRespBody {
        return PlaylistSongsRespBody(
                roomAndPlaylistService.getSongs(
                        data.roomToken, data.playlistTitle)
        )
    }

    @PostMapping("/getsongsvoted")
    fun getPlaylistSongsVoted(
            @RequestBody data: PlaylistSongsReqData
    ): PlaylistSongsVotedRespBody {
        return PlaylistSongsVotedRespBody(
                roomAndPlaylistService.getSongsVoted(
                        data.roomToken, data.playlistTitle)
        )
    }

    @PostMapping("/ordersong")
    fun orderSong(
            @RequestBody data: OrderSongReqData
    ): OrderSongRespBody {
        val songInfo = roomAndPlaylistService.orderSong(data)

        return OrderSongRespBody(songInfo.toPublicSongInfo())

    }

    @PostMapping("/setCurrentPlayingSong")
    fun setCurrentPlayingSong(
            @RequestBody data: SetCurrentPlayingSongReqData
    ): SetCurrentPlayingSongRespBody {

        val updatedPlaylistInfo = roomAndPlaylistService.setCurrentPlayingSong(
                data.roomToken, data.playlistTitle, data.songId
        )
        return SetCurrentPlayingSongRespBody(updatedPlaylistInfo)
    }

    @PostMapping("/currentPlayingSong")
    fun currentPlayingSong(
            @RequestBody data: CurrentPlayingSongReqData
    ): CurrentPlayingSongRespBody {

        val updatedPlaylistInfo = roomAndPlaylistService.currentPlayingSong(
                data.roomToken, data.playlistTitle
        )
        return CurrentPlayingSongRespBody(
                updatedPlaylistInfo
        )

    }

    @PostMapping("/voteforsong")
    fun voteForSong(
            @RequestBody data: VoteForSongReqData
    ): VoteForSongRespBody {
        val updatedPlaylistInfo = roomAndPlaylistService.voteForSong(
                data.roomToken, data.playlistTitle, data.songId, data.action
        )
        return VoteForSongRespBody(
                updatedPlaylistInfo
        )
    }
}