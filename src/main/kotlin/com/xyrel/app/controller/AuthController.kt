package com.xyrel.app.controller

import com.xyrel.app.common.ApiResponse
import com.xyrel.app.dto.request.FirebaseLoginRequest
import com.xyrel.app.dto.request.FirebaseSignupRequest
import com.xyrel.app.dto.request.NativeLoginRequest
import com.xyrel.app.dto.request.NativeSignupRequest
import com.xyrel.app.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService) {

    /**
     * POST /auth/signup
     * Register a new user (passenger or driver) using a Firebase ID token.
     */
    @PostMapping("/signup")
    fun signup(@Valid @RequestBody request: FirebaseSignupRequest): ResponseEntity<ApiResponse<*>> {
        val response = authService.signup(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
    }

    /**
     * POST /auth/login
     * Login with a Firebase ID token — returns internal JWT.
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: FirebaseLoginRequest): ResponseEntity<ApiResponse<*>> {
        val response = authService.login(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * POST /auth/native/signup
     * Register a new user with email and password.
     */
    @PostMapping("/native/signup")
    fun signupNative(@Valid @RequestBody request: NativeSignupRequest): ResponseEntity<ApiResponse<*>> {
        val response = authService.signupNative(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
    }

    /**
     * POST /auth/native/login
     * Login with email and password.
     */
    @PostMapping("/native/login")
    fun loginNative(@Valid @RequestBody request: NativeLoginRequest): ResponseEntity<ApiResponse<*>> {
        val response = authService.loginNative(request)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    /**
     * GET /auth/ping — health check (public)
     */
    @GetMapping("/ping")
    fun ping() = ResponseEntity.ok(ApiResponse.success(mapOf("message" to "pong")))
}
