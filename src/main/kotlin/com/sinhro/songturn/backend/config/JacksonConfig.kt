package com.sinhro.songturn.backend.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
    @Bean
    @Primary
    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder {
        val builder = Jackson2ObjectMapperBuilder()
        builder.modulesToInstall(JavaTimeModule())
        builder.modulesToInstall(KotlinModule())
        builder.propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
//        builder.failOnUnknownProperties(false)
        builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        builder.featuresToDisable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)

        return builder
    }

    @Bean
    @Qualifier("NimuscObjectMapper")
    fun objectMapperForNimusc(
            @Autowired
            jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder
    ): ObjectMapper {
        val om = jackson2ObjectMapperBuilder.build<ObjectMapper>()
        om.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
        return om
    }

    @Bean
    @Primary
    fun objectMapperPrimary(
            @Autowired
            jackson2ObjectMapperBuilder: Jackson2ObjectMapperBuilder
    ): ObjectMapper {
        return jackson2ObjectMapperBuilder.build()
    }

}