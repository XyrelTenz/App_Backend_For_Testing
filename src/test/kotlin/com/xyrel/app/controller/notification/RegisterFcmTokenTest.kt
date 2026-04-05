package com.xyrel.app.controller.notification

import com.xyrel.app.controller.NotificationController
import com.xyrel.app.controller.BaseControllerTest

import com.xyrel.app.dto.request.RegisterFcmTokenRequest
import com.xyrel.app.domain.entity.UserFcmToken
import com.xyrel.app.repository.UserFcmTokenRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(NotificationController::class)
class NotificationControllerTest : BaseControllerTest() {

    @MockBean
    private lateinit var userFcmTokenRepository: UserFcmTokenRepository

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    fun `registerFcmToken should return 200 ok`() {
        val request = RegisterFcmTokenRequest(fcmToken = "test-fcm-token")

        `when`(userFcmTokenRepository.findByFcmToken(any())).thenReturn(null)
        `when`(userFcmTokenRepository.save(any())).thenAnswer { it.arguments[0] as UserFcmToken }

        mockMvc.perform(
            post("/api/notifications/token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }
}
