package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.tables.Song
import com.sinhro.songturn.backend.tables.pojos.Song as SongPojo
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SongRepository @Autowired constructor(
        private val dsl: DSLContext
) {
    private val tableSong = Song.SONG

    fun songsInPlaylist(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId))
                .fetch()
                .into(SongPojo::class.java)
    }

    fun saveSong(song: SongPojo, playlistId: Int): SongPojo {
        val songRec = dsl.newRecord(tableSong)
                .setArtist(song.artist)
                .setTitle(song.title)
                .setDuration(song.duration)
                .setLink(song.link)
                .setExpiresAt(song.expiresAt)
                .setLinkFromUser(song.linkFromUser)
                .setUserId(song.userId)
                .setPlaylistId(playlistId)
        songRec.store()

        return songRec.into(SongPojo::class.java)
    }

    fun getSongById(songId: Int): SongPojo? {
        return dsl.selectFrom(tableSong)
                .where(tableSong.ID.eq(songId))
                .fetchOne()
                ?.into(SongPojo::class.java)
    }
}