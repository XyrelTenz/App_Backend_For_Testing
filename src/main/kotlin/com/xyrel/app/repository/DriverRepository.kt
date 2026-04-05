package com.xyrel.app.repository

import com.xyrel.app.domain.entity.Driver
import java.util.Optional
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface DriverRepository : JpaRepository<Driver, UUID> {

  fun findByUserId(userId: UUID): Optional<Driver>

  fun findByIsOnlineTrue(): List<Driver>

  fun findByIsOnlineTrueAndIsVerifiedTrue(): List<Driver>

  // Update driver location using PostGIS ST_Point. longitude first, then latitude per PostGIS
  @Modifying
  @Query(
      value =
          """
            UPDATE drivers 
            SET last_known_location = ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                updated_at = now()
            WHERE user_id = :userId
        """,
      nativeQuery = true,
  )
  fun updateLocation(userId: UUID, lat: Double, lng: Double)

  // Find online+verified drivers within [radiusMeters] of the given point. Returns user_id strings
  // ordered by proximity.
  @Query(
      value =
          """
            SELECT d.* FROM drivers d
            WHERE d.is_online = true
              AND d.is_verified = true
              AND d.last_known_location IS NOT NULL
              AND ST_DWithin(
                    d.last_known_location,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radiusMeters
                  )
            ORDER BY ST_Distance(d.last_known_location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
        """,
      nativeQuery = true,
  )
  fun findNearbyOnlineDrivers(lat: Double, lng: Double, radiusMeters: Double): List<Driver>
}
