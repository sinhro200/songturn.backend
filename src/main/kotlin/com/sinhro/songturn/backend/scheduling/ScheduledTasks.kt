package com.sinhro.songturn.backend.scheduling

import com.sinhro.songturn.backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ScheduledTasks @Autowired constructor(
        val userRepository: UserRepository
) {

    private val log = LoggerFactory.getLogger(ScheduledTasks::class.java)

    //every 15 mins
    @Scheduled(fixedRate = 900000)
    fun reportCurrentTime() {
        val deletedUsers = userRepository.removeDemoUsersNotOnlineFrom(
                LocalDateTime.now().minusDays(1)
        )
        val deletedUsersString =
                if (deletedUsers.isEmpty()) "nobody"
                else "\n${deletedUsers.joinToString("\n") { it.toString() }}"
        log.info("Deleted users : $deletedUsersString")
    }
}