package com.xyrel.app.repository

import com.xyrel.app.domain.entity.Rating
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RatingRepository : JpaRepository<Rating, UUID> {

  fun findByRideId(rideId: UUID): List<Rating>

  fun findByRatedId(ratedId: UUID): List<Rating>

  @Query("SELECT AVG(r.score) FROM Rating r WHERE r.ratedId = :userId")
  fun getAverageRating(userId: UUID): Double?
}
