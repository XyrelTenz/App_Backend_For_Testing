package com.xyrel.app.dto.request

import jakarta.validation.constraints.NotBlank

data class RegisterFcmTokenRequest(
    @field:NotBlank(message = "FCM token is required") val fcmToken: String,
    val deviceType: String? = null,
)
