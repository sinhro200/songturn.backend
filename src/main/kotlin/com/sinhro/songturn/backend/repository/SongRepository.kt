package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.tables.Song
import com.sinhro.songturn.backend.tables.VotedSongs
import org.jooq.DSLContext
import org.jooq.Record2
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.stream.Collectors
import kotlin.math.sign
import com.sinhro.songturn.backend.tables.pojos.Song as SongPojo

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

    fun songsNotInQueueByRatingAndOrderedTime(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId)
                        .andNot(tableSong.IN_QUEUE))
                .orderBy(tableSong.RATING, tableSong.ORDERED_AT)
                .fetch()
                .into(SongPojo::class.java)
    }

    fun songsInQueueByRatingAndOrderedTime(playlistId: Int): MutableList<SongPojo> {
        return dsl.selectFrom(tableSong)
                .where(tableSong.PLAYLIST_ID.eq(playlistId).and(tableSong.IN_QUEUE))
                .orderBy(tableSong.RATING.desc(), tableSong.ORDERED_AT)
                .fetch()
                .into(SongPojo::class.java)
    }

    fun getSongVotes(songs: List<SongPojo>, userId: Int): MutableMap<Int, Int> {
        return dsl.select(tableVotedSongs.SONG_ID, tableVotedSongs.ACTION)
                .from(tableVotedSongs)
                .where(tableVotedSongs.USER_ID.eq(userId).and(
                        tableVotedSongs.SONG_ID.`in`(songs.map { song -> song.id })
                ))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        Record2<Int, Int>::component1,
                        Record2<Int, Int>::component2
                ))
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
                .setInQueue(song.inQueue)
        songRec.store()

        return songRec.into(SongPojo::class.java)
    }

    fun getSongById(songId: Int): SongPojo? {
        return dsl.selectFrom(tableSong)
                .where(tableSong.ID.eq(songId))
                .fetchOne()
                ?.into(SongPojo::class.java)
    }

    fun setSongOutOfQueue(songId: Int): SongPojo? {
        return dsl.update(tableSong)
                .set(tableSong.IN_QUEUE, false)
                .where(tableSong.ID.eq(songId))
                .returning()
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
        val song = getSongById(songId)
        if (song == null || !song.inQueue)
            return song


        val action = sign(act.toDouble()).toInt()
        val votedSongRecord = dsl.selectFrom(tableVotedSongs)
                .where(tableVotedSongs.USER_ID.eq(userId)
                        .and(tableVotedSongs.SONG_ID.eq(songId)))
                .fetchOne()

        if (votedSongRecord != null) {
            if (votedSongRecord.action == action)
                return song
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