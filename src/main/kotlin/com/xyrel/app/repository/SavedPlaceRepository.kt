package com.xyrel.app.repository

import com.xyrel.app.domain.entity.SavedPlace
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SavedPlaceRepository : JpaRepository<SavedPlace, UUID> {
  fun findByUserId(userId: UUID): List<SavedPlace>

  fun deleteByIdAndUserId(id: UUID, userId: UUID): Int
}
