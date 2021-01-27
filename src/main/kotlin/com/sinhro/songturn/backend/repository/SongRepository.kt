package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.routines.CalculateSongRating
import com.sinhro.songturn.backend.tables.Song
import com.sinhro.songturn.backend.tables.VotedSongs
import com.sinhro.songturn.backend.tables.pojos.Song as SongPojo
import com.sinhro.songturn.backend.tables.pojos.VotedSongs as VotedSongsPojo
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.math.sign

@Component
class SongRepository @Autowired constructor(
        private val dsl: DSLContext
) {
    private val tableSong = Song.SONG
    private val tableVotedSongs = VotedSongs.VOTED_SONGS

    fun songsInPlaylistRandomOrder(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId))
                .fetch()
                .into(SongPojo::class.java)
    }

    fun songsInPlaylistByOrderedTime(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId))
                .orderBy(tableSong.ORDERED_AT)
                .fetch()
                .into(SongPojo::class.java)
    }

    fun songsInPlaylistByRatingAndOrderedTime(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId))
                .orderBy(tableSong.RATING,tableSong.ORDERED_AT)
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

    fun calculateSongRating(
            songId: Int
    ): SongPojo? {
        return dsl.selectFrom("calculate_song_rating($songId)")
                .fetchOne()
                ?.into(SongPojo::class.java)

        //По идее должно так работать
        //но выдаёт ошибку
//        val func = CalculateSongRating()
//        func.setCalculatedSongId(songId)
//        func.execute(dsl.configuration())
//        return func.returnValue?.into(SongPojo::class.java)
    }

    fun voteForSong(
            userId: Int, songId: Int,
            act: Int
    ): SongPojo? {
        val action = sign(act.toDouble()).toInt()
        val votedSongRecord = dsl.selectFrom(tableVotedSongs)
                .where(tableVotedSongs.USER_ID.eq(userId)
                        .and(tableVotedSongs.SONG_ID.eq(songId)))
                .fetchOne()

        if (votedSongRecord != null) {
            if (votedSongRecord.action == action)
                return getSongById(votedSongRecord.songId)
            else {
                votedSongRecord.action = action
                votedSongRecord.store()
                return calculateSongRating(songId)
            }
        } else {
            val rec = dsl.newRecord(tableVotedSongs)
                    .setSongId(songId)
                    .setUserId(userId)
                    .setAction(action)
            rec.store()
//            return rec.into(SongPojo::class.java)
            return calculateSongRating(songId)
        }
    }
}