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

  @PostMapping("/signup")
  fun signup(@Valid @RequestBody request: FirebaseSignupRequest): ResponseEntity<ApiResponse<*>> {
    val response = authService.signup(request)
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
  }

  @PostMapping("/login")
  fun login(@Valid @RequestBody request: FirebaseLoginRequest): ResponseEntity<ApiResponse<*>> {
    val response = authService.login(request)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @PostMapping("/native/signup")
  fun signupNative(
      @Valid @RequestBody request: NativeSignupRequest
  ): ResponseEntity<ApiResponse<*>> {
    val response = authService.signupNative(request)
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response))
  }

  @PostMapping("/native/login")
  fun loginNative(@Valid @RequestBody request: NativeLoginRequest): ResponseEntity<ApiResponse<*>> {
    val response = authService.loginNative(request)
    return ResponseEntity.ok(ApiResponse.success(response))
  }

  @GetMapping("/ping")
  fun ping() = ResponseEntity.ok(ApiResponse.success(mapOf("message" to "pong")))
}
