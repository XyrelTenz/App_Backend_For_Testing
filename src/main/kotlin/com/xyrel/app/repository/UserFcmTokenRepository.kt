package com.xyrel.app.repository

import com.xyrel.app.domain.entity.UserFcmToken
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserFcmTokenRepository : JpaRepository<UserFcmToken, UUID> {
  fun findByUserId(userId: UUID): List<UserFcmToken>

  fun findByFcmToken(fcmToken: String): UserFcmToken?

  fun deleteByFcmToken(fcmToken: String)
}
