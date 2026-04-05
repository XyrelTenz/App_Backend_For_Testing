package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "user_fcm_tokens")
class UserFcmToken(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid") val userId: UUID,
    @Column(name = "fcm_token", nullable = false, unique = true) var fcmToken: String,
    @Column(name = "device_type") var deviceType: String? = null,
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)
