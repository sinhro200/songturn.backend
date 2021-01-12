package com.sinhro.songturn.backend.repository

import com.sinhro.songturn.backend.pojos.PlaylistPojo
import com.sinhro.songturn.backend.pojos.RoomPojo
import com.sinhro.songturn.backend.pojos.UserPojo
import com.sinhro.songturn.backend.tables.Playlist
import com.sinhro.songturn.backend.tables.Room
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

    fun savePlaylist(playlistPojo: PlaylistPojo): PlaylistPojo {
        return dsl.insertInto(tablePlaylist)
                .columns(
                        tablePlaylist.TITLE,
                        tablePlaylist.DESCRIPTION,
                        tablePlaylist.ROOM_ID,
                        tablePlaylist.CURRENT_SONG_ID,
                        tablePlaylist.LISTENER_ID)
                .values(
                        playlistPojo.title,
                        playlistPojo.description,
                        playlistPojo.room_id,
                        playlistPojo.current_song_id,
                        playlistPojo.listener_id)
                .returning()
                .fetchOne()
                ?.into(PlaylistPojo::class.java) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "Cant insert playlist")
    }

    fun saveRoom(roomPojo: RoomPojo): RoomPojo {
        return dsl.insertInto(tableRoom)
                .columns(
                        tableRoom.INVITE,
                        tableRoom.TOKEN,
                        tableRoom.TITLE,
                        tableRoom.OWNER_ID,
                        tableRoom.RS_PRIORITY_RARELY_ORDERING_USERS,
                        tableRoom.RS_ALLOW_VOTES,
                        tableRoom.RS_SONG_OWNERS_VISIBLE)
                .values(
                        roomPojo.invite,
                        roomPojo.token,
                        roomPojo.title,
                        roomPojo.ownerId,
                        roomPojo.rs_priority_rarely_ordering_users,
                        roomPojo.rs_allow_votes,
                        roomPojo.rs_song_owners_visible
                )
                .returning()
                .fetchOne()
                ?.into(RoomPojo::class.java) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "Cant insert room")
    }

    fun findByInvite(invite: String): RoomPojo? {
        return dsl.selectFrom(tableRoom)
                .where(tableRoom.INVITE.eq(invite))
                .fetchOne()
                ?.into(RoomPojo::class.java)
    }

    fun findById(id: Int): RoomPojo? {
        return dsl.selectFrom(tableRoom)
                .where(tableRoom.ID.eq(id))
                .fetchOne()
                ?.into(RoomPojo::class.java)
    }

    fun updateRoom(roomPojo: RoomPojo): RoomPojo? {
        return dsl.update(tableRoom)
                .set(
                        row(
                                tableRoom.INVITE,
                                tableRoom.TOKEN,
                                tableRoom.TITLE,
                                tableRoom.OWNER_ID,
                                tableRoom.RS_PRIORITY_RARELY_ORDERING_USERS,
                                tableRoom.RS_ALLOW_VOTES,
                                tableRoom.RS_SONG_OWNERS_VISIBLE,
                        ),
                        row(
                                roomPojo.invite,
                                roomPojo.token,
                                roomPojo.title,
                                roomPojo.ownerId,
                                roomPojo.rs_priority_rarely_ordering_users,
                                roomPojo.rs_allow_votes,
                                roomPojo.rs_song_owners_visible
                        ))
                .where(tableRoom.ID.eq(roomPojo.id))
                .returning()
                .fetchOne()
                ?.into(RoomPojo::class.java) ?: throw CommonException(
                CommonError(ErrorCodes.INTERNAL_SERVER_EXC),
                "Cant update room info to $roomPojo ")
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
        if (playlistTitle!=null)
            whereCondition = whereCondition.and(tablePlaylist.TITLE.eq(playlistTitle))
        return dsl.deleteFrom(tablePlaylist)
                .where(whereCondition)
                .returning()
                .fetch()
                .into(PlaylistPojo::class.java)
    }
}