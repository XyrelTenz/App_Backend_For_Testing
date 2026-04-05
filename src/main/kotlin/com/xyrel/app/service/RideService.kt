package com.xyrel.app.service

import com.xyrel.app.common.BadRequestException
import com.xyrel.app.common.ForbiddenException
import com.xyrel.app.common.NotFoundException
import com.xyrel.app.domain.entity.Ride
import com.xyrel.app.domain.enums.RideStatus
import com.xyrel.app.dto.request.RequestRideRequest
import com.xyrel.app.dto.response.NearbyRideResponse
import com.xyrel.app.dto.response.RideResponse
import com.xyrel.app.repository.DriverRepository
import com.xyrel.app.repository.RideRepository
import com.xyrel.app.repository.UserFcmTokenRepository
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RideService(
    private val rideRepository: RideRepository,
    private val driverRepository: DriverRepository,
    private val fareCalculator: FareCalculatorService,
    private val pushNotificationService: PushNotificationService,
    private val fcmTokenRepository: UserFcmTokenRepository,
) {
  // Passenger requests a new ride.
  @Transactional
  fun requestRide(passengerId: UUID, request: RequestRideRequest): RideResponse {
    val distanceKm =
        fareCalculator.calculateDistanceKm(
            request.pickupLat,
            request.pickupLng,
            request.dropoffLat,
            request.dropoffLng,
        )
    val fareAmount = fareCalculator.calculateFare(distanceKm)
    val durationMins = fareCalculator.estimateDurationMins(distanceKm)

    val ride =
        Ride(
            passengerId = passengerId,
            pickupAddress = request.pickupAddress,
            pickupLocation = fareCalculator.toWkt(request.pickupLat, request.pickupLng),
            dropoffAddress = request.dropoffAddress,
            dropoffLocation = fareCalculator.toWkt(request.dropoffLat, request.dropoffLng),
            distanceKm = BigDecimal.valueOf(distanceKm),
            estimatedDurationMins = durationMins,
            estimatedFareAmount = BigDecimal.valueOf(fareAmount),
            paymentMethod = request.paymentMethod,
            scheduledAt = request.scheduledAt?.let { Instant.parse(it) },
        )
    val saved = rideRepository.save(ride)
    return saved.toResponse(fareCalculator)
  }

  @Transactional(readOnly = true)
  fun getRide(rideId: UUID): RideResponse {
    val ride =
        rideRepository.findByRideId(rideId) ?: throw NotFoundException("Ride not found: $rideId")
    return ride.toResponse(fareCalculator)
  }

  @Transactional(readOnly = true)
  fun getPassengerHistory(passengerId: UUID): List<RideResponse> =
      rideRepository.findPassengerHistory(passengerId).map { it.toResponse(fareCalculator) }

  @Transactional(readOnly = true)
  fun getDriverHistory(driverId: UUID): List<RideResponse> =
      rideRepository.findDriverHistory(driverId).map { it.toResponse(fareCalculator) }

  // Driver accepts a ride
  @Transactional
  fun acceptRide(rideId: UUID, driverUserId: UUID) {
    val updated = rideRepository.acceptRide(rideId, driverUserId, RideStatus.DRIVER_ASSIGNED)
    if (updated == 0)
        throw com.xyrel.app.common.ConflictException(
            "Ride already accepted by another driver or not found"
        )
    // Update driver status to on-trip
    driverRepository.findByUserId(driverUserId).ifPresent { driver ->
      driver.isOnline = true
      driverRepository.save(driver)
    }

    // Notify passenger
    val ride = rideRepository.findByRideId(rideId)
    if (ride != null) {
      notifyUser(ride.passengerId, "Driver Assigned", "A driver is on the way to pick you up!")
    }
  }

  // Driver updates ride status.
  @Transactional
  fun updateRideStatus(rideId: UUID, driverUserId: UUID, newStatus: String, cancelReason: String?) {
    val status =
        try {
          RideStatus.fromValue(newStatus)
        } catch (e: IllegalArgumentException) {
          throw BadRequestException("Invalid status: $newStatus")
        }

    val ride =
        rideRepository.findByRideId(rideId) ?: throw NotFoundException("Ride not found: $rideId")

    // Ensure only the assigned driver can update
    if (ride.driverId != driverUserId) {
      throw ForbiddenException("You are not the assigned driver for this ride")
    }

    ride.status = status
    ride.updatedAt = Instant.now()
    when (status) {
      RideStatus.IN_PROGRESS -> ride.startedAt = Instant.now()
      RideStatus.COMPLETED -> {
        ride.completedAt = Instant.now()
        ride.finalFareAmount = ride.estimatedFareAmount
      }
      RideStatus.CANCELLED -> {
        ride.cancelledAt = Instant.now()
        ride.cancelReason = cancelReason
      }
      else -> {}
    }
    rideRepository.save(ride)

    // Notify passenger of status change
    val title = "Ride Update"
    val body =
        when (status) {
          RideStatus.DRIVER_EN_ROUTE -> "Your driver is en route!"
          RideStatus.DRIVER_ARRIVED -> "Your driver has arrived!"
          RideStatus.IN_PROGRESS -> "Your ride has started."
          RideStatus.COMPLETED -> "You have reached your destination."
          RideStatus.CANCELLED -> "Your ride was cancelled."
          else -> "Your ride status was updated to ${status.value}"
        }
    notifyUser(ride.passengerId, title, body)
  }

  // Get nearby searching rides for a driver.
  @Transactional(readOnly = true)
  fun getNearbyRides(
      lat: Double,
      lng: Double,
      radiusMeters: Double = 5000.0,
  ): List<NearbyRideResponse> =
      rideRepository.findNearbySearchingRides(lat, lng, radiusMeters).map {
        NearbyRideResponse(
            id = it.rideId.id,
            pickupAddress = it.pickupAddress,
            dropoffAddress = it.dropoffAddress,
            pickupLat = fareCalculator.latFromWkt(it.pickupLocation),
            pickupLng = fareCalculator.lngFromWkt(it.pickupLocation),
            distanceKm = it.distanceKm,
            estimatedFareAmount = it.estimatedFareAmount,
            status = it.status.value,
        )
      }

  private fun notifyUser(userId: UUID, title: String, body: String) {
    val tokens = fcmTokenRepository.findByUserId(userId)
    for (token in tokens) {
      pushNotificationService.sendPushNotificationToToken(token.fcmToken, title, body)
    }
  }
}

// Extension to convert Ride entity → RideResponse DTO
private fun Ride.toResponse(fareCalculator: FareCalculatorService) =
    RideResponse(
        id = rideId.id,
        status = status.value,
        passengerId = passengerId,
        driverId = driverId,
        pickupAddress = pickupAddress,
        pickupLat = fareCalculator.latFromWkt(pickupLocation),
        pickupLng = fareCalculator.lngFromWkt(pickupLocation),
        dropoffAddress = dropoffAddress,
        dropoffLat = fareCalculator.latFromWkt(dropoffLocation),
        dropoffLng = fareCalculator.lngFromWkt(dropoffLocation),
        distanceKm = distanceKm,
        estimatedDurationMins = estimatedDurationMins,
        estimatedFareAmount = estimatedFareAmount,
        finalFareAmount = finalFareAmount,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus.value,
        scheduledAt = scheduledAt,
        startedAt = startedAt,
        completedAt = completedAt,
        cancelledAt = cancelledAt,
        cancelReason = cancelReason,
        createdAt = rideId.createdAt,
    )
