package com.sinhro.songturn.backend

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.sinhro.songturn.rest.model.RegisterUserInfo
import com.sinhro.songturn.rest.request_response.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import kotlin.jvm.Throws

@RunWith(SpringJUnit4ClassRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
class AllUserActionsTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    private lateinit var objectMapper: ObjectMapper

    @Before
    @Throws(Exception::class)
    fun setup() {
        objectMapper = ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(JavaTimeModule())
    }

    @Test
    fun allActionsTest() {
        val USER_DATA = RegisterUserInfo(
                "AUAT_login",
                "AUAT_email@AUAT_email.AUAT_email",
                "AUAT_firstname",
                "AUAT_lastname",
                "AUAT_nickname",
                "AUAT_rawpassword"
        )
        var ACCESS_TOKEN = ""

        var ROOM_TOKEN = ""

        var PLAYLIST_TITLE = ""

        val req = RegisterUserInfo(
                USER_DATA.login,
                USER_DATA.email,
                USER_DATA.firstName,
                USER_DATA.lastName,
                USER_DATA.nickname,
                USER_DATA.rawPassword
        )

        val mvcResult: MvcResult =
                mockMvc.perform(
                        MockMvcRequestBuilders.post("/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8")
                                .content(
                                        objectMapper.writeValueAsString(req)
                                )
                )
                        .andDo(::print)
                        .andExpect(MockMvcResultMatchers.status().isOk)
                        .andExpect(
                                MockMvcResultMatchers.jsonPath("$.body.message")
                                        .value("Registered successfully"))
                        .andReturn()


        val authReq = AuthReqData(
                USER_DATA.login,
                USER_DATA.rawPassword
        )

        val result = mockMvc.perform(
                MockMvcRequestBuilders.post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .content(
                                objectMapper.writeValueAsString(authReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body.access_token").exists())
                .andReturn()

        println("____________________response = ${result.response.contentAsString}")
        val resp = objectMapper.readValue<AuthRespBody>(result.response.contentAsString)
        println("____________________AuthRespBody = ${resp}")
        ACCESS_TOKEN = resp.accessToken
        println("____________________ACCESS TOKEN = $ACCESS_TOKEN")
        //  ### CREATE ROOM
        val createRoomReq = CreateRoomReqData(
                "AUAT_roomTitle"
        )

        val createRoomResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/room/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer $ACCESS_TOKEN")
                        .content(
                                objectMapper.writeValueAsString(createRoomReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body.room_info").exists())
                .andReturn()

        val createRoomResp = objectMapper.readValue<CreateRoomRespBody>(
                createRoomResult.response.contentAsString)
        ROOM_TOKEN = createRoomResp.roomInfo.roomToken

        //  ### ROOM PLAYLISTS
        val roomPlaylistsReq = GetPlaylistsReqData(
                ROOM_TOKEN
        )

        val roomPlaylistsResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/room/playlists")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer $ACCESS_TOKEN")
                        .content(
                                objectMapper.writeValueAsString(roomPlaylistsReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body").exists())
                .andReturn()

        val roomPlaylistsResp = objectMapper.readValue<
                GetPlaylistsRespBody>(
                roomPlaylistsResult.response.contentAsString)
        val playlist = roomPlaylistsResp.playlists[0]
        PLAYLIST_TITLE = playlist.title

        //  ### ORDER SONG

        val orderSongReq = OrderSongReqData(
                ROOM_TOKEN, PLAYLIST_TITLE,
                "https://vk.com/audio123622163_456240417_1f70e95f58df403368"
        )

        val orderSongResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/playlist/ordersong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer $ACCESS_TOKEN")
                        .content(
                                objectMapper.writeValueAsString(orderSongReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body.song_info").exists())
                .andReturn()

        val orderSongResp = objectMapper.readValue<OrderSongRespBody>(
                orderSongResult.response.contentAsString)
        val songInfo = orderSongResp.songInfo
        print("Ordered song : $songInfo")

        // ### songs in playlist
        val songsInPlaylistReq = GetSongsReqData(
                ROOM_TOKEN, PLAYLIST_TITLE
        )

        val songsInPlaylistResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/playlist/getsongs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer $ACCESS_TOKEN")
                        .content(
                                objectMapper.writeValueAsString(songsInPlaylistReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body").exists())
                .andReturn()

        val songsInPlaylistResp = objectMapper.readValue<GetSongsRespBody>(
                songsInPlaylistResult.response.contentAsString)
        val songsInPlaylist = songsInPlaylistResp.songs
        print("Songs in playlist : $songsInPlaylist")

        //  ###     Vote for song

        val voteForSongReq = VoteForSongReqData(
                ROOM_TOKEN, PLAYLIST_TITLE, songsInPlaylist[0].id, 1
        )

        val voteForSongResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/playlist/voteforsong")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8")
                        .header("Authorization", "Bearer $ACCESS_TOKEN")
                        .content(
                                objectMapper.writeValueAsString(voteForSongReq)
                        ))
                .andDo(::print)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(
                        MockMvcResultMatchers.jsonPath(
                                "$.body").exists())
                .andReturn()

        val voteForSongResp = objectMapper.readValue<VoteForSongRespBody>(
                voteForSongResult.response.contentAsString)
        val votedSong = voteForSongResp.songInfo
        print("Song after vote : $votedSong")
    }

}