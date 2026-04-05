package com.xyrel.app.domain.entity

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "chat_messages")
class ChatMessage(
    @Id @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "ride_id", nullable = false, columnDefinition = "uuid") val rideId: UUID,
    @Column(name = "sender_id", nullable = false, columnDefinition = "uuid") val senderId: UUID,
    @Column(name = "message_text", nullable = false) val messageText: String,
    @Column(name = "created_at", updatable = false) val createdAt: Instant = Instant.now(),
)
