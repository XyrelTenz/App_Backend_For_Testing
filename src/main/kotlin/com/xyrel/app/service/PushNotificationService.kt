package com.xyrel.app.service

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class PushNotificationService {

  private val log = LoggerFactory.getLogger(PushNotificationService::class.java)

  //  Sends a push notification to a specific FCM token.
  fun sendPushNotificationToToken(
      fcmToken: String,
      title: String,
      body: String,
      data: Map<String, String> = emptyMap(),
  ) {
    try {
      val messageBuilder =
          Message.builder()
              .setToken(fcmToken)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build())

      // Add custom data payload
      if (data.isNotEmpty()) {
        messageBuilder.putAllData(data)
      }

      val message = messageBuilder.build()
      val response = FirebaseMessaging.getInstance().sendAsync(message).get()
      log.info("Successfully sent message to token: {}. Response: {}", fcmToken, response)
    } catch (e: Exception) {
      log.error("Failed to send push notification to token {}: {}", fcmToken, e.message, e)
    }
  }

  // Sends a push notification to a topic. Useful for broadcasting to all drivers, etc.
  fun sendPushNotificationToTopic(
      topic: String,
      title: String,
      body: String,
      data: Map<String, String> = emptyMap(),
  ) {
    try {
      val messageBuilder =
          Message.builder()
              .setTopic(topic)
              .setNotification(Notification.builder().setTitle(title).setBody(body).build())

      if (data.isNotEmpty()) {
        messageBuilder.putAllData(data)
      }

      val message = messageBuilder.build()
      val response = FirebaseMessaging.getInstance().sendAsync(message).get()
      log.info("Successfully sent message to topic: {}. Response: {}", topic, response)
    } catch (e: Exception) {
      log.error("Failed to send push notification to topic {}: {}", topic, e.message, e)
    }
  }
}
