package com.xyrel.app.repository

import com.xyrel.app.domain.entity.User
import java.time.Instant
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, UUID> {

  fun findByEmail(email: String): Optional<User>

  fun findByFirebaseUid(firebaseUid: String): Optional<User>

  fun existsByEmail(email: String): Boolean

  fun existsByFirebaseUid(firebaseUid: String): Boolean

  @Modifying
  @Query("UPDATE User u SET u.lastSeenAt = :now WHERE u.id = :userId")
  fun updateLastSeen(userId: UUID, now: Instant)

  @Modifying
  @Query("UPDATE User u SET u.deletedAt = :now, u.isActive = false WHERE u.id = :userId")
  fun softDelete(userId: UUID, now: Instant)
}
