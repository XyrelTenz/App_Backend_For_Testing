package com.xyrel.app.websocket

import com.xyrel.app.dto.request.SendMessageRequest
import com.xyrel.app.service.ChatService
import java.util.UUID
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Controller

// STOMP message handler for real-time chat.
@Controller
class ChatWebSocketController(private val chatService: ChatService) {

  @MessageMapping("/chat/{rideId}/message")
  @SendTo("/topic/chat/{rideId}")
  fun handleChatMessage(
      @DestinationVariable rideId: String,
      request: SendMessageRequest,
      headerAccessor: SimpMessageHeaderAccessor,
  ): Any {
    // Extract user ID from STOMP session attributes (set during connect)
    val userId =
        headerAccessor.sessionAttributes?.get("userId") as? String
            ?: return mapOf("error" to "Unauthorized: missing user session")

    return chatService.sendMessage(UUID.fromString(rideId), UUID.fromString(userId), request)
  }
}
