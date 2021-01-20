package com.sinhro.songturn.backend.extentions

import com.sinhro.songturn.backend.tables.pojos.Playlist as PlaylistPojo
import com.sinhro.songturn.backend.tables.pojos.Room as RoomPojo
import com.sinhro.songturn.backend.tables.pojos.Users as UserPojo
import com.sinhro.songturn.backend.tables.pojos.Song as SongPojo
import com.sinhro.songturn.rest.model.*
import org.springframework.security.crypto.password.PasswordEncoder


fun UserPojo.toFullUserInfo(): FullUserInfo {
    return FullUserInfo(
            id, login, email, firstName, lastName, nickname
    )
}

fun UserPojo.toPublicUserInfo(): PublicUserInfo {
    return PublicUserInfo(
            id, firstName, lastName, nickname
    )
}

fun RegisterUserInfo.toUserPojo(
        passwordEncoder: PasswordEncoder,
        roleId: Int? = null,
        isVerified: Boolean? = null,
        roomId: Int? = null
): UserPojo {
    fun emptyToNull(value: String, valueToReturn: String = value): String? {
        return if (value.isBlank())
            null
        else
            valueToReturn
    }
    return UserPojo(
            null,
            emptyToNull(nickname),
            emptyToNull(email),
            emptyToNull(login),
            emptyToNull(rawPassword, passwordEncoder.encode(rawPassword)),
            roleId,
            emptyToNull(firstName),
            emptyToNull(lastName),
            isVerified,
            roomId
    )
}

fun FullUserInfo.toPublicUserInfo(): PublicUserInfo {
    return PublicUserInfo(
            id, firstName, lastName, nickname
    )
}

fun PlaylistPojo.toPlaylistInfo(): PlaylistInfo {
    return PlaylistInfo(
            id, title, description, roomId, currentSongId, listenerId
    )
}

fun RoomPojo.toRoomInfo(): RoomInfo {
    return RoomInfo(
            title, invite, token, ownerId,
            RoomSettings(
                    rsPriorityRarelyOrderingUsers,
                    rsAllowVotes,
                    rsSongOwnersVisible,
                    rsAnyCanListen
            )
    )
}

fun SongPojo.toFullSongInfo(): SongInfo {
    return SongInfo(
            id, title, artist, duration, link, expiresAt, userId
    )
}

fun SongPojo.toPublicSongInfo(): SongInfo {
    return SongInfo(
            id, title, artist, duration
    )
}

fun SongInfo.toPublicSongInfo(): SongInfo {
    return SongInfo(
            id, title, artist, durationSeconds
    )
}

