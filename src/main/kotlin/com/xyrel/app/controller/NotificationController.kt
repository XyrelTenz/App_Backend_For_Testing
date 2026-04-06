package com.xyrel.app.controller

import com.xyrel.app.common.ApiResponse
import com.xyrel.app.domain.entity.UserFcmToken
import com.xyrel.app.dto.request.RegisterFcmTokenRequest
import com.xyrel.app.repository.UserFcmTokenRepository
import jakarta.validation.Valid
import java.security.Principal
import java.time.Instant
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(private val fcmTokenRepository: UserFcmTokenRepository) {
  private val log = LoggerFactory.getLogger(NotificationController::class.java)

  // Register an FCM token for the authenticated user
  @PostMapping("/token")
  @Transactional
  fun registerFcmToken(
      principal: Principal,
      @Valid @RequestBody request: RegisterFcmTokenRequest,
  ): ResponseEntity<ApiResponse<*>> {
    val fcmToken = fcmTokenRepository.findByFcmToken(request.fcmToken)
    val userId = principal.name

    if (fcmToken == null) {
      val newToken =
          UserFcmToken(
              userId = UUID.fromString(userId),
              fcmToken = request.fcmToken,
              deviceType = request.deviceType,
          )
      fcmTokenRepository.save(newToken)
      log.info("Registered new FCM token for user $userId")
    } else {
      // Update token if it transferred to another user, or update timestamp
      if (fcmToken.userId.toString() != userId) {
        fcmTokenRepository.delete(fcmToken)
        val newToken =
            UserFcmToken(
                userId = UUID.fromString(userId),
                fcmToken = request.fcmToken,
                deviceType = request.deviceType,
            )
        fcmTokenRepository.save(newToken)
      } else {
        fcmToken.updatedAt = Instant.now()
        fcmTokenRepository.save(fcmToken)
      }
      log.info("Updated FCM token for user $userId")
    }

    return ResponseEntity.ok(
        ApiResponse.success(mapOf("message" to "Token registered successfully"))
    )
  }

  // Unregister an FCM token
  @DeleteMapping("/token")
  @Transactional
  fun unregisterFcmToken(@RequestParam token: String): ResponseEntity<ApiResponse<*>> {
    fcmTokenRepository.deleteByFcmToken(token)
    log.info("Unregistered FCM token")
    return ResponseEntity.ok(
        ApiResponse.success(mapOf("message" to "Token unregistered successfully"))
    )
  }
}
