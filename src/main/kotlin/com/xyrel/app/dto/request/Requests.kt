package com.xyrel.app.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class FirebaseLoginRequest(
    @field:NotBlank(message = "Firebase ID token is required") val idToken: String
)

data class NativeLoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Must be a valid email format")
    val email: String,
    @field:NotBlank(message = "Password is required") val password: String,
)

data class FirebaseSignupRequest(
    @field:NotBlank(message = "Firebase ID token is required") val idToken: String,
    @field:NotBlank(message = "Full name is required") val fullName: String,
    val phone: String? = null,

    // Passenger or Driver
    val role: String = "passenger",

    // Driver Only
    val plateNumber: String? = null,
    val vehicleType: String? = null,
    val vehicleColor: String? = null,
    val licenseNumber: String? = null,
)

data class NativeSignupRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Must be a valid email format")
    val email: String,
    @field:NotBlank(message = "Password is required") val password: String,
    @field:NotBlank(message = "Full name is required") val fullName: String,
    val phone: String? = null,

    // Passenger or Driver
    val role: String = "passenger",

    // Driver Only
    val plateNumber: String? = null,
    val vehicleType: String? = null,
    val vehicleColor: String? = null,
    val licenseNumber: String? = null,
)

data class RequestRideRequest(
    @field:NotBlank(message = "Pickup address is required") val pickupAddress: String,
    val pickupLat: Double,
    val pickupLng: Double,
    @field:NotBlank(message = "Dropoff address is required") val dropoffAddress: String,
    val dropoffLat: Double,
    val dropoffLng: Double,
    val vehicleType: String = "baobao_etrike",
    val paymentMethod: String = "cash",
    val scheduledAt: String? = null,
)

data class UpdateRideStatusRequest(
    @field:NotBlank(message = "Status is required") val status: String,
    val cancelReason: String? = null,
)

data class UpdateLocationRequest(val lat: Double, val lng: Double)

data class SendMessageRequest(
    @field:NotBlank(message = "Message text is required") val messageText: String
)

data class AddSavedPlaceRequest(
    @field:NotBlank(message = "Label is required") val label: String,
    @field:NotBlank(message = "Address is required") val address: String,
    val lat: Double,
    val lng: Double,
)

data class RateRideRequest(val ratedUserId: String, val score: Int, val comment: String? = null)
