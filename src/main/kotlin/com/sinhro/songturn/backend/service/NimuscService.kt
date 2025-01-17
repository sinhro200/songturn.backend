package com.sinhro.songturn.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import okhttp3.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration

data class AudioItem(
        val artist: String,
        val title: String,
        val url: String,
        val durationSeconds: Int,
        //через сколько
        val expiresIn: Duration = Duration.ofHours(1)
)

@Component
class NimuscService @Autowired constructor(
        @Qualifier("NimuscObjectMapper")
        private val om : ObjectMapper
) {

    private val logger = LoggerFactory.getLogger(NimuscService::class.java)

    @Value("\${nimusc.server.address}")
    private lateinit var serverAddress: String

    @Value("\${nimusc.server.port}")
    private var serverPort: Int = 0

    fun getAudio(songLink: String, authInfo: String?): AudioItem {
        val client = OkHttpClient()

        val httpUrlBuilder = HttpUrl.Builder()
                .scheme("http")
                .addEncodedPathSegment("get-audio")
                .host(serverAddress)
                .port(serverPort)
                .addQueryParameter("url", songLink)

        if (!authInfo.isNullOrBlank())
            httpUrlBuilder.addQueryParameter("auth", authInfo)


        val request = Request.Builder().url(httpUrlBuilder.build()).build()

        val call: Call = client.newCall(request)
        val response: Response = call.execute()

//        om.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        response.body()?.let { responseBody ->
            if (response.isSuccessful) {
//                val om = jackson2ObjectMapperBuilder.build<ObjectMapper>()
//                om.propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE

                val audioItem =
                        om.readValue<AudioItem>(responseBody.string())
                return audioItem

//                val node = om.readTree(responseBody.string())

                /*val expiresAtNullableNode = node.get("expiresAt")
                val expiresAt: LocalDateTime?
                if (expiresAtNullableNode == null
                        || expiresAtNullableNode.isNull
                        || expiresAtNullableNode.isEmpty)
                    expiresAt = null
                else
                    expiresAt = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(
                                    node.get("expiresAt").asLong()
                            ),
                            ZoneOffset.UTC
                    )

                return SongInfo(
                        title = node.get("title").asText(),
                        artist = node.get("artist").asText(),
                        duration = node.get("durationSeconds").asInt(),
                        link = node.get("url").asText(),

                )*/
            } else {
                val request = response.request()
                logger.error("For [response.request] to ${request.url()}, " +
                        "method ${request.method()}, " +
                        "with body ${request.body().toString()}, " +
                        "headers ${request.headers()}")
                logger.error("[Response] is - Body : ${response.body()?.string()}. " +
                        "Code :${response.code()}. " +
                        "Message ${response.message()}.")
            }
        }

        throw CommonException(CommonError(ErrorCodes.NIMUSC_SERVER_EXC))
    }
}