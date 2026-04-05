package com.xyrel.app.controller.auth

import com.xyrel.app.controller.AuthController
import com.xyrel.app.controller.BaseControllerTest

import com.xyrel.app.dto.request.FirebaseLoginRequest
import com.xyrel.app.dto.request.FirebaseSignupRequest
import com.xyrel.app.dto.response.AuthResponse
import com.xyrel.app.service.AuthService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import com.xyrel.app.dto.request.NativeSignupRequest
import com.xyrel.app.dto.request.NativeLoginRequest

@WebMvcTest(AuthController::class)
class SignupAndLoginTest : BaseControllerTest() {

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `signup should return 201 created when valid request`() {
        val request = FirebaseSignupRequest(idToken = "valid-token", fullName = "Test User")
        val response = AuthResponse("jwt-token", "123", "PASSENGER", "Test User", "test@test.com")

        `when`(authService.signup(any())).thenReturn(response)

        mockMvc.perform(
            post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
    }

    @Test
    fun `login should return 200 ok when valid request`() {
        val request = FirebaseLoginRequest(idToken = "valid-token")
        val response = AuthResponse("jwt-token", "123", "PASSENGER", "Test User", "test@test.com")

        `when`(authService.login(any())).thenReturn(response)

        mockMvc.perform(
            post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
    }

    @Test
    fun `native signup should return 201 created when valid request`() {
        val request = NativeSignupRequest(
            email = "native@test.com",
            password = "password123",
            fullName = "Native User"
        )
        val response = AuthResponse("jwt-token", "124", "PASSENGER", "Native User", "native@test.com")

        `when`(authService.signupNative(any())).thenReturn(response)

        mockMvc.perform(
            post("/auth/native/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
    }

    @Test
    fun `native login should return 200 ok when valid request`() {
        val request = NativeLoginRequest(
            email = "native@test.com",
            password = "password123"
        )
        val response = AuthResponse("jwt-token", "124", "PASSENGER", "Native User", "native@test.com")

        `when`(authService.loginNative(any())).thenReturn(response)

        mockMvc.perform(
            post("/auth/native/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").value("jwt-token"))
    }
}
