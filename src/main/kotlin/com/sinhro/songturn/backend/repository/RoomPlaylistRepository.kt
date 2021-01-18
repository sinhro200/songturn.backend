package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.tables.Playlist
import com.sinhro.songturn.backend.tables.Room
import com.sinhro.songturn.backend.tables.records.PlaylistRecord
import com.sinhro.songturn.backend.tables.records.RoomRecord
import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Playlist as PlaylistPojo
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.jooq.DSLContext
import org.jooq.impl.DSL.row
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class RoomPlaylistRepository @Autowired constructor(
        val dsl: DSLContext
) {

    val tableRoom: Room = Room.ROOM
    val tablePlaylist: Playlist = Playlist.PLAYLIST

    fun setListenerId(playlistPojo: PlaylistPojo, userId: Int): PlaylistPojo {
        return dsl.update(tablePlaylist)
                .set(tablePlaylist.LISTENER_ID, userId)
                .where(tablePlaylist.ID.eq(playlistPojo.id))
                .returning()
                .fetchOne()
                ?.into(PlaylistPojo::class.java)
                ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "Cant update playlist listener")

    }

    fun clearListenerId(playlistPojo: PlaylistPojo): PlaylistPojo {

        val dbPlaylistRecord = dsl.selectFrom(tablePlaylist)
                .where(tablePlaylist.ID.eq(playlistPojo.id))
                .fetchOne() ?: throw CommonException(CommonError(ErrorCodes.PLAYLIST_NOT_FOUND))
        dbPlaylistRecord.listenerId = null
        dbPlaylistRecord.store()
        return dbPlaylistRecord.into(PlaylistPojo::class.java)

    }

    fun savePlaylist(playlistPojo: PlaylistPojo): PlaylistPojo {
        val playlistRecord = dsl.newRecord(tablePlaylist)
        updatePlaylistRecordFieldsFromPojo(playlistRecord, playlistPojo)
        playlistRecord.store()
        return playlistRecord.into(PlaylistPojo::class.java)
    }

    fun saveRoom(roomPojo: RoomPojo): RoomPojo {
        val roomRecord = dsl.newRecord(tableRoom)
        updateRoomRecordFieldsFromPojo(roomRecord, roomPojo)
        roomRecord.store()
        return roomRecord.into(RoomPojo::class.java)
    }

    fun findByInvite(invite: String): RoomPojo? {
        return dsl.selectFrom(tableRoom)
                .where(tableRoom.INVITE.eq(invite))
                .fetchOne()
                ?.into(RoomPojo::class.java)
    }

    fun findRoomById(roomId: Int): RoomPojo? {
        return dsl.selectFrom(tableRoom)
                .where(tableRoom.ID.eq(roomId))
                .fetchOne()
                ?.into(RoomPojo::class.java)
    }

    fun updateRoom(oldRoomPojo: RoomPojo, roomPojo: RoomPojo): RoomPojo {
        val roomRecord = dsl.selectFrom(tableRoom)
                .where(tableRoom.ID.eq(oldRoomPojo.id))
                .fetchOne() ?: throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "Room not found")
        updateRoomRecordFieldsFromPojo(roomRecord, roomPojo)
        roomRecord.store()
        return roomRecord.into(RoomPojo::class.java)
    }

    fun roomsByUserId(userId: Int): MutableList<RoomPojo> {
        return dsl.selectFrom(tableRoom)
                .where(tableRoom.OWNER_ID.eq(userId))
                .fetch()
                .into(RoomPojo::class.java)

    }

    fun removeRoomByInvOrToken(inviteOrToken: String, ownerId: Int)
            : MutableList<RoomPojo> {
        return dsl.deleteFrom(tableRoom)
                .where(
                        tableRoom.OWNER_ID.eq(ownerId)
                                .and(tableRoom.INVITE.eq(inviteOrToken)
                                        .or(tableRoom.TOKEN.eq(inviteOrToken))
                                )
                )
                .returning()
                .fetch()
                .into(RoomPojo::class.java)
    }

    fun removePlaylistsByRoom(roomId: Int, playlistTitle: String?)
            : MutableList<PlaylistPojo> {
        var whereCondition = tablePlaylist.ROOM_ID.eq(roomId)
        if (playlistTitle != null)
            whereCondition = whereCondition.and(tablePlaylist.TITLE.eq(playlistTitle))
        return dsl.deleteFrom(tablePlaylist)
                .where(whereCondition)
                .returning()
                .fetch()
                .into(PlaylistPojo::class.java)
    }

    fun getAllPlaylistsInRoom(roomId: Int)
            : MutableList<PlaylistPojo> {
        return dsl.selectFrom(tablePlaylist)
                .where(tablePlaylist.ROOM_ID.eq(roomId))
                .fetch()
                .into(PlaylistPojo::class.java)
    }

    fun getPlaylistInRoom(roomId: Int, playlistTitle: String)
            : PlaylistPojo? {

        return dsl.selectFrom(tablePlaylist)
                .where(tablePlaylist.ROOM_ID.eq(roomId).and(tablePlaylist.TITLE.eq(playlistTitle)))
                .fetchOne()
                ?.into(PlaylistPojo::class.java)
    }

    private fun updatePlaylistRecordFieldsFromPojo(
            playlistRecord: PlaylistRecord, playlistPojo: PlaylistPojo) {
        playlistPojo.title?.let(playlistRecord::setTitle)
        playlistPojo.description?.let(playlistRecord::setDescription)
        playlistPojo.currentSongId?.let(playlistRecord::setCurrentSongId)
        playlistPojo.listenerId?.let(playlistRecord::setListenerId)
        playlistPojo.roomId?.let(playlistRecord::setRoomId)
    }

    private fun updateRoomRecordFieldsFromPojo(
            roomRecord: RoomRecord, roomPojo: RoomPojo
    ) {
        roomPojo.title?.let(roomRecord::setTitle)
        roomPojo.invite?.let(roomRecord::setInvite)
        roomPojo.token?.let(roomRecord::setToken)
        roomPojo.ownerId?.let(roomRecord::setOwnerId)
        roomPojo.rsAllowVotes?.let(roomRecord::setRsAllowVotes)
        roomPojo.rsPriorityRarelyOrderingUsers?.let(roomRecord::setRsPriorityRarelyOrderingUsers)
        roomPojo.rsSongOwnersVisible?.let(roomRecord::setRsSongOwnersVisible)
    }
}