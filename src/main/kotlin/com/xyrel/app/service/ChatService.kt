package com.xyrel.app.service

import com.xyrel.app.domain.entity.ChatMessage
import com.xyrel.app.dto.request.SendMessageRequest
import com.xyrel.app.dto.response.ChatMessageResponse
import com.xyrel.app.repository.ChatMessageRepository
import com.xyrel.app.repository.RideRepository
import com.xyrel.app.repository.UserFcmTokenRepository
import java.util.UUID
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ChatService(
    private val chatMessageRepository: ChatMessageRepository,
    private val rideRepository: RideRepository,
    private val fcmTokenRepository: UserFcmTokenRepository,
    private val pushNotificationService: PushNotificationService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

  @Transactional
  fun sendMessage(rideId: UUID, senderId: UUID, request: SendMessageRequest): ChatMessageResponse {
    val msg = ChatMessage(rideId = rideId, senderId = senderId, messageText = request.messageText)
    val saved = chatMessageRepository.save(msg)
    val response = saved.toResponse()

    // Broadcast via STOMP WebSocket topic
    messagingTemplate.convertAndSend("/topic/chat/$rideId", response)

    // Find ride to determine recipient for push notification
    val ride = rideRepository.findByRideId(rideId)
    if (ride != null) {
      val recipientId =
          if (senderId == ride.passengerId) {
            // If passenger sent it, notify driver
            ride.driverId
          } else {
            // If driver sent it, notify passenger
            ride.passengerId
          }

      if (recipientId != null) {
        val tokens = fcmTokenRepository.findByUserId(recipientId)
        for (token in tokens) {
          pushNotificationService.sendPushNotificationToToken(
              fcmToken = token.fcmToken,
              title = "New Message",
              body = request.messageText,
              data = mapOf("rideId" to rideId.toString(), "type" to "chat"),
          )
        }
      }
    }

    return response
  }

  @Transactional(readOnly = true)
  fun getChatHistory(rideId: UUID): List<ChatMessageResponse> =
      chatMessageRepository.findByRideIdOrderByCreatedAtAsc(rideId).map { it.toResponse() }
}

private fun ChatMessage.toResponse() =
    ChatMessageResponse(
        id = id,
        rideId = rideId,
        senderId = senderId,
        messageText = messageText,
        createdAt = createdAt,
    )
