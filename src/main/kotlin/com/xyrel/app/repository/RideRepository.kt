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

  fun findByPassengerIdAndStatusIn(passengerId: UUID, statuses: List<RideStatus>): List<Ride>

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

  @Modifying
  @Query(
      value =
          """
            INSERT INTO rides (
                id, created_at, passenger_id, pickup_address, pickup_location, 
                dropoff_address, dropoff_location, distance_km, estimated_duration_mins, 
                estimated_fare_amount, payment_method, status, updated_at
            ) VALUES (
                :id, :createdAt, :passengerId, :pickupAddress, ST_GeogFromText(:pickupLocation), 
                :dropoffAddress, ST_GeogFromText(:dropoffLocation), :distanceKm, :estimatedDurationMins, 
                :estimatedFareAmount, :paymentMethod, :status, CURRENT_TIMESTAMP
            )
        """,
      nativeQuery = true,
  )
  fun nativeInsert(
      id: UUID,
      createdAt: java.time.Instant,
      passengerId: UUID,
      pickupAddress: String,
      pickupLocation: String,
      dropoffAddress: String,
      dropoffLocation: String,
      distanceKm: java.math.BigDecimal,
      estimatedDurationMins: Int,
      estimatedFareAmount: java.math.BigDecimal,
      paymentMethod: String,
      status: String,
  )

  // Find searching rides near a given point within radiusMeters.
  @Query(
      value =
          """
            SELECT r.* FROM rides r
            WHERE r.status = 'SEARCHING'
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
