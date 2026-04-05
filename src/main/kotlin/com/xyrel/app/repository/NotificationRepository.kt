package com.xyrel.app.repository

import com.xyrel.app.domain.entity.Notification
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, UUID> {

  fun findByUserIdOrderByCreatedAtDesc(userId: UUID): List<Notification>

  fun countByUserIdAndIsReadFalse(userId: UUID): Long

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId")
  fun markAllAsRead(userId: UUID)
}
