package com.sinhro.songturn.backend.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import org.springframework.context.annotation.Bean

//@Configuration
class JacksonConfiguration {
    @Bean
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
        mapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true)
        return mapper
    }
}