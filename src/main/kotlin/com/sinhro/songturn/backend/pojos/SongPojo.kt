package com.sinhro.songturn.backend.pojos

import com.sinhro.songturn.rest.model.SongInfo
import java.time.LocalDateTime

class SongPojo(
        var id: Int? = null,
        var artist: String? = null,
        var title: String? = null,
        var link: String? = null,
        var duration: Int? = null,
        var ordered_at: LocalDateTime? = null,
        var expires_at: LocalDateTime? = null,
        var playlist_id: Int? = null,
        var user_id: Int? = null,
        var rating: Int? = null,
        var link_from_user: String? = null
) {


    companion object {
        fun toSongInfo(songPojo: SongPojo): SongInfo {
            return SongInfo(
                    songPojo.title,
                    songPojo.artist,
                    songPojo.link,
                    songPojo.duration,
                    songPojo.expires_at
            )
        }
    }
}