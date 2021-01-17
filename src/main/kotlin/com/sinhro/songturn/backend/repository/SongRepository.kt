package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.pojos.SongPojo
import com.sinhro.songturn.backend.tables.Song
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SongRepository @Autowired constructor(
        private val dsl: DSLContext
) {
    private val tableSong = Song.SONG

    fun getSongsInPlaylist(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId))
                .fetch()
                .into(SongPojo::class.java)
    }
}