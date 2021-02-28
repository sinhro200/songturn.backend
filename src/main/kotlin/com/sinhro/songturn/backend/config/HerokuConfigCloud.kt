package com.sinhro.songturn.backend.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.net.URI
import java.net.URISyntaxException
import javax.sql.DataSource
import kotlin.jvm.Throws


@Configuration
class HerokuConfigCloud {

    private val logger: Logger = LoggerFactory.getLogger(HerokuConfigCloud::class.java)

    @Value("\${DATABASE_DRIVER}")
    private var driverClass: String? = null

    @Value("\${DATABASE_USER}")
    private var username: String? = null

    @Value("\${DATABASE_PASSWORD}")
    private var password: String? = null

    @Value("\${DATABASE_URL}")
    private lateinit var url: String

    @Bean
    @Throws(URISyntaxException::class)
    @Profile("release_heroku")
    fun dataSource(): DataSource {
        logger.debug("Initializing datasource")

        if (isUri()){
            logger.trace("Got uri from env : $url")
            val dbUri = URI(url)
            username = dbUri.userInfo.split(":")[0]
            password = dbUri.userInfo.split(":")[1]
            url = "jdbc:postgresql://" + dbUri.host + ':' + dbUri.port + dbUri.path
            logger.trace("Converted uri to url : $url")
        }else{
            logger.trace("got url from env : $url")
        }

        logger.debug("Creating data source with url : $url")
        return DataSourceBuilder
                .create()
                .username(username)
                .password(password)
                .url(url)
                .driverClassName(driverClass)
                .build()
    }

    private fun isUri(): Boolean {
        return url.startsWith("postgres:")
    }


    /**
     * Heroku dont see port if it passed throw .properties or .yml
     */
    @Component
    class ServerPortCustomizer : WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
        override fun customize(factory: ConfigurableWebServerFactory) {
            factory.setPort(System.getenv("PORT").toInt())
        }
    }
}
