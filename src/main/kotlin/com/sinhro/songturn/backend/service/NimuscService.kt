package com.sinhro.songturn.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.sinhro.songturn.rest.ErrorCodes
import com.sinhro.songturn.rest.core.CommonError
import com.sinhro.songturn.rest.core.CommonException
import com.sinhro.songturn.rest.model.SongInfo
import okhttp3.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime

@Component
class NimuscService {

    @Value("\${nimusc.server.address}")
    private lateinit var serverAddress: String

    @Value("\${nimusc.server.port}")
    private var serverPort: Int = 0

    fun getAudio(songLink: String, authInfo: String?): SongInfo {
        val client = OkHttpClient()

        val httpUrlBuilder = HttpUrl.Builder()
                .scheme("http")
                .addEncodedPathSegment("get-audio")
                .host(serverAddress)
                .port(serverPort)
                .addQueryParameter("url", songLink)

        authInfo?.let {
            httpUrlBuilder.addQueryParameter("auth", it)
        }

        val request = Request.Builder().url(httpUrlBuilder.build()).build();

        val call: Call = client.newCall(request)
        val response: Response = call.execute()
        val om = ObjectMapper()
        response.body()?.let { responseBody ->
            if (response.isSuccessful) {
                val node = om.readTree(responseBody.string())
                return SongInfo(
                        node.get("title").asText(),
                        node.get("artist").asText(),
                        node.get("url").asText(),
                        node.get("durationSeconds").asInt(),
                        LocalDateTime.from(node.get("expiresAt").asLong()
                )
            } else {
//                logger.error("For [response.request] to ${response.request.url}, method ${response.request.method}, with body ${response.request.body.toString()}" +
//                        ", headers ${response.request.headers}")
//                logger.error("[Response] is - Body : ${it.string()}. Code :${response.code}. Message ${response.message}.")
            }
        }

        throw CommonException(CommonError(ErrorCodes.NIMUSC_SERVER_EXC))
    }
}