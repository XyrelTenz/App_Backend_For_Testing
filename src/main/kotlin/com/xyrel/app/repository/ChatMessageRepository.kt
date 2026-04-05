package com.xyrel.app.repository

import com.xyrel.app.domain.entity.ChatMessage
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatMessageRepository : JpaRepository<ChatMessage, UUID> {
  fun findByRideIdOrderByCreatedAtAsc(rideId: UUID): List<ChatMessage>
}
