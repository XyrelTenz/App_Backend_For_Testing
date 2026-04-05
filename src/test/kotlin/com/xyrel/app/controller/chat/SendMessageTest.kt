package com.xyrel.app.controller.chat

import com.xyrel.app.controller.ChatController
import com.xyrel.app.controller.BaseControllerTest

import com.xyrel.app.dto.request.SendMessageRequest
import com.xyrel.app.dto.response.ChatMessageResponse
import com.xyrel.app.service.ChatService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.Instant
import java.util.UUID

@WebMvcTest(ChatController::class)
class ChatControllerTest : BaseControllerTest() {

    @MockBean
    private lateinit var chatService: ChatService

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    fun `sendMessage should return 201 created`() {
        val rideId = UUID.randomUUID()
        val senderId = UUID.fromString("00000000-0000-0000-0000-000000000001")
        val request = SendMessageRequest(messageText = "Hello driver!")
        val response = ChatMessageResponse(
            id = UUID.randomUUID(), rideId = rideId, senderId = senderId,
            messageText = "Hello driver!", createdAt = Instant.now()
        )

        `when`(chatService.sendMessage(any(), any(), any())).thenReturn(response)

        mockMvc.perform(
            post("/api/chat/$rideId/send")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.message_text").value("Hello driver!"))
    }
}
