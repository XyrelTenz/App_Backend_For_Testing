package com.xyrel.app.dto.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class AuthResponse(
    val token: String,
    val userId: String,
    val role: String,
    val fullName: String,
    val email: String,
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val phone: String?,
    // TODO: Remove JsonProperty and use camelCase consistently across the app
    @param:JsonProperty("full_name") val fullName: String,
    val role: String,
    val isActive: Boolean,
    val profileImageUrl: String?,
    val country: String?,
    val averageRating: BigDecimal?,
    val totalTripsCompleted: Int?,
    val createdAt: Instant,
)

data class RideResponse(
    val id: UUID,
    val status: String,
    val passengerId: UUID,
    val driverId: UUID?,
    val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val distanceKm: BigDecimal?,
    val estimatedDurationMins: Int?,
    val estimatedFareAmount: BigDecimal?,
    val finalFareAmount: BigDecimal?,
    val paymentMethod: String,
    val paymentStatus: String,
    val scheduledAt: Instant?,
    val startedAt: Instant?,
    val completedAt: Instant?,
    val cancelledAt: Instant?,
    val cancelReason: String?,
    val createdAt: Instant,
)

data class NearbyRideResponse(
    val id: UUID,
    val pickupAddress: String,
    val dropoffAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    val distanceKm: BigDecimal?,
    val estimatedFareAmount: BigDecimal?,
    val status: String,
)

data class DriverResponse(
    val userId: UUID,
    val plateNumber: String,
    val vehicleType: String,
    val vehicleColor: String?,
    val licenseNumber: String,
    val isVerified: Boolean,
    val isOnline: Boolean,
)

data class SavedPlaceResponse(
    val id: UUID,
    val label: String,
    val address: String,
    val lat: Double,
    val lng: Double,
)

data class ChatMessageResponse(
    val id: UUID,
    val rideId: UUID,
    val senderId: UUID,
    val messageText: String,
    val createdAt: Instant,
)

data class NotificationResponse(
    val id: UUID,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean,
    val createdAt: Instant,
)
