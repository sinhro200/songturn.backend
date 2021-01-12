package com.sinhro.songturn.backend.utils

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import java.util.*

@Component
class RandomStringGenerator {
    @Bean
    fun random(): Random {
        return Random()
    }

    @Autowired
    lateinit var random: Random

    fun generateString(characters: CharSequence, length: Int): String {
        return generateString(random, characters, length)
    }

    fun generateString(rng: Random, characters: CharSequence, length: Int): String {
        val text = CharArray(length)
        for (i in 0 until length) {
            text[i] = characters[rng.nextInt(characters.length)]
        }
        return String(text)
    }
}