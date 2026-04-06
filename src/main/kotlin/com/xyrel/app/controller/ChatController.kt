package com.xyrel.app.controller

import com.xyrel.app.common.ApiResponse
import com.xyrel.app.dto.request.SendMessageRequest
import com.xyrel.app.service.ChatService
import jakarta.validation.Valid
import java.security.Principal
import java.util.UUID
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/chat")
class ChatController(private val chatService: ChatService) {

  @PostMapping("/{rideId}/send")
  fun sendMessage(
      principal: Principal,
      @PathVariable rideId: UUID,
      @Valid @RequestBody request: SendMessageRequest,
  ): ResponseEntity<ApiResponse<*>> {
    val message = chatService.sendMessage(rideId, UUID.fromString(principal.name), request)
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(message))
  }

  @GetMapping("/{rideId}/history")
  fun getChatHistory(
      principal: Principal,
      @PathVariable rideId: UUID,
  ): ResponseEntity<ApiResponse<*>> {
    val messages = chatService.getChatHistory(rideId)
    return ResponseEntity.ok(ApiResponse.success(messages))
  }
}
