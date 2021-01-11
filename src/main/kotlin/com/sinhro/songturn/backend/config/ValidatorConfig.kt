package com.sinhro.songturn.backend.config

import com.sinhro.songturn.rest.validation.Validator
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ValidatorConfig {
    @Bean
    fun validator() : Validator{
        return Validator()
    }
}