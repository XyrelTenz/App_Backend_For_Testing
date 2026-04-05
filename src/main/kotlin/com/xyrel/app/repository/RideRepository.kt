package com.xyrel.app.repository

import com.xyrel.app.domain.entity.Ride
import com.xyrel.app.domain.entity.RideId
import com.xyrel.app.domain.enums.RideStatus
import java.util.UUID
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface RideRepository : JpaRepository<Ride, RideId> {

  fun findByPassengerId(passengerId: UUID): List<Ride>

  fun findByDriverId(driverId: UUID): List<Ride>

  fun findByStatus(status: RideStatus): List<Ride>

  @Query("SELECT r FROM Ride r WHERE r.passengerId = :passengerId ORDER BY r.rideId.createdAt DESC")
  fun findPassengerHistory(passengerId: UUID): List<Ride>

  @Query("SELECT r FROM Ride r WHERE r.driverId = :driverId ORDER BY r.rideId.createdAt DESC")
  fun findDriverHistory(driverId: UUID): List<Ride>

  @Query("SELECT r FROM Ride r WHERE r.rideId.id = :id") fun findByRideId(id: UUID): Ride?

  @Modifying
  @Query(
      "UPDATE Ride r SET r.status = :status, r.updatedAt = CURRENT_TIMESTAMP WHERE r.rideId.id = :id"
  )
  fun updateStatus(id: UUID, status: RideStatus)

  @Modifying
  @Query(
      """
        UPDATE Ride r
        SET r.driverId = :driverId,
            r.status = :status,
            r.updatedAt = CURRENT_TIMESTAMP
        WHERE r.rideId.id = :id
          AND r.driverId IS NULL
    """
  )
  fun acceptRide(id: UUID, driverId: UUID, status: RideStatus): Int

  // Find searching rides near a given point within radiusMeters.
  @Query(
      value =
          """
            SELECT r.* FROM rides r
            WHERE r.status = 'searching'
              AND ST_DWithin(
                    r.pickup_location,
                    ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography,
                    :radiusMeters
                  )
            ORDER BY ST_Distance(r.pickup_location, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)::geography)
            LIMIT 50
        """,
      nativeQuery = true,
  )
  fun findNearbySearchingRides(lat: Double, lng: Double, radiusMeters: Double): List<Ride>
}
