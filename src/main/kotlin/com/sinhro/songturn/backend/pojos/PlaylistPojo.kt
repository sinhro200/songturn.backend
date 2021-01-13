package com.sinhro.songturn.backend.pojos

import com.sinhro.songturn.rest.model.PlaylistInfo

class PlaylistPojo(
        var id: Int? = null,
        var title: String? = null,
        var description: String? = null,
        var room_id: Int? = null,
        var current_song_id: Int? = null,
        var listener_id: Int? = null,
) {
    companion object {
        fun toPlaylistInfo(pp: PlaylistPojo): PlaylistInfo {
            return PlaylistInfo(
                    pp.id,
                    pp.title,
                    pp.description,
                    pp.room_id,
                    pp.current_song_id,
                    pp.listener_id
            )
        }
    }
}