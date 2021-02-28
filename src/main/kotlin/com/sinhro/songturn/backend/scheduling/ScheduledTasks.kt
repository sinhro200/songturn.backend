package com.sinhro.songturn.backend.scheduling

import com.sinhro.songturn.backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Component
class ScheduledTasks @Autowired constructor(
        val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)

    //every 15 mins
    @Scheduled(fixedRate = 900000)
    fun removeDemoUsers() {
        val deletedUsers = userRepository.removeDemoUsersNotOnlineFrom(
                OffsetDateTime.now(ZoneOffset.UTC).minusDays(1)
        )
        val deletedUsersString =
                if (deletedUsers.isEmpty()) "nobody"
                else "\n${deletedUsers.joinToString("\n") { it.toString() }}"
        log.info("Deleted users : $deletedUsersString")
    }

    //every 15 mins
    @Scheduled(fixedRate = 900000)
    fun leaveUsersFromRoomWhenNotOnline() {
        val deletedUsers = userRepository.leaveUsersFromRoomNotOnlineFrom(
                OffsetDateTime.now(ZoneOffset.UTC)
                        .minusHours(2)
        )
        val leftFromRoomUsersString =
                if (deletedUsers.isEmpty()) "nobody"
                else "\n${deletedUsers.joinToString("\n") { it.toString() }}"
        log.info("users to set not in room : $leftFromRoomUsersString")
    }
}