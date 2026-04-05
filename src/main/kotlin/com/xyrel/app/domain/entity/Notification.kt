package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "notifications")
class Notification(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "user_id", nullable = false, columnDefinition = "uuid") val userId: UUID,
    @Column(name = "title", nullable = false) val title: String,
    @Column(name = "message", nullable = false) val message: String,
    @Column(name = "type", nullable = false) val type: String,
    @Column(name = "is_read") var isRead: Boolean = false,
    @Column(name = "payload", columnDefinition = "jsonb") var payload: String? = null,
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
)
