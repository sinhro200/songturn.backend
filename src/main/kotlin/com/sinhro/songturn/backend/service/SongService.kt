package com.sinhro.songturn.backend.service

import com.sinhro.songturn.backend.pojos.PlaylistPojo
import com.sinhro.songturn.backend.pojos.SongPojo
import com.sinhro.songturn.backend.repository.SongRepository
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class SongService @Autowired constructor(
        private val songRepository: SongRepository
) {

    fun getSongsInPlaylist(playlistPojo: PlaylistPojo): MutableList<SongPojo> {
        playlistPojo.id?.let {
            return songRepository.getSongsInPlaylist(it)
        }
        throw CommonException(CommonError(ErrorCodes.INTERNAL_SERVER_EXC), "Playlist dont has id")
    }
}