package com.xyrel.app.domain.entity

import com.xyrel.app.domain.enums.PaymentStatus
import com.xyrel.app.domain.enums.RideStatus
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Ride entity mapped to the partitioned `rides` table. The composite PK (id, created_at) is
 * reflected via @EmbeddedId. Spatial columns (pickup_location, dropoff_location) are stored as WKT
 * strings and managed using native queries.
 */
@Entity
@Table(name = "rides")
class Ride(
    @EmbeddedId val rideId: RideId = RideId(),
    @Column(name = "passenger_id", nullable = false, columnDefinition = "uuid")
    var passengerId: UUID = UUID.randomUUID(),
    @Column(name = "driver_id", columnDefinition = "uuid") var driverId: UUID? = null,
    @Column(name = "pickup_address", nullable = false) var pickupAddress: String = "",

    /** WKT e.g. "POINT(123.4344 7.8285)" — longitude first per GeoJSON convention */
    @Column(name = "pickup_location", columnDefinition = "geography(Point,4326)", nullable = false)
    var pickupLocation: String = "",
    @Column(name = "dropoff_address", nullable = false) var dropoffAddress: String = "",
    @Column(name = "dropoff_location", columnDefinition = "geography(Point,4326)", nullable = false)
    var dropoffLocation: String = "",
    @Column(name = "distance_km", precision = 6, scale = 2) var distanceKm: BigDecimal? = null,
    @Column(name = "estimated_duration_mins") var estimatedDurationMins: Int? = null,
    @Column(name = "estimated_fare_amount", precision = 8, scale = 2)
    var estimatedFareAmount: BigDecimal? = null,
    @Column(name = "final_fare_amount", precision = 8, scale = 2)
    var finalFareAmount: BigDecimal? = null,
    @Column(name = "payment_method", nullable = false) var paymentMethod: String = "cash",
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", columnDefinition = "payment_status")
    var paymentStatus: PaymentStatus = PaymentStatus.PENDING,
    @Enumerated(EnumType.STRING)
    @Column(name = "status", columnDefinition = "ride_status")
    var status: RideStatus = RideStatus.SEARCHING,
    @Column(name = "scheduled_at") var scheduledAt: Instant? = null,
    @Column(name = "started_at") var startedAt: Instant? = null,
    @Column(name = "completed_at") var completedAt: Instant? = null,
    @Column(name = "cancelled_at") var cancelledAt: Instant? = null,
    @Column(name = "cancel_reason") var cancelReason: String? = null,
    @Column(name = "updated_at") var updatedAt: Instant = Instant.now(),
)

@Embeddable
class RideId(
    @Column(name = "id", columnDefinition = "uuid") val id: UUID = UUID.randomUUID(),
    @Column(name = "created_at") val createdAt: Instant = Instant.now(),
) : java.io.Serializable
