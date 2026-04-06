package com.xyrel.app.controller

import com.xyrel.app.common.ApiResponse
import com.xyrel.app.dto.request.UpdateLocationRequest
import com.xyrel.app.dto.request.UpdateRideStatusRequest
import com.xyrel.app.service.DriverService
import com.xyrel.app.service.RideService
import jakarta.validation.Valid
import java.security.Principal
import java.util.UUID
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/driver")
@PreAuthorize("hasRole('DRIVER')")
class DriverController(
    private val rideService: RideService,
    private val driverService: DriverService,
) {

  @PostMapping("/ride/{id}/accept")
  fun acceptRide(principal: Principal, @PathVariable id: UUID): ResponseEntity<ApiResponse<*>> {
    rideService.acceptRide(id, UUID.fromString(principal.name))
    return ResponseEntity.ok(ApiResponse.success(mapOf("message" to "Ride accepted successfully")))
  }

  // Update ride status (en_route, arrived, in_progress, completed, cancelled).
  @PatchMapping("/ride/{id}/status")
  fun updateRideStatus(
      principal: Principal,
      @PathVariable id: UUID,
      @Valid @RequestBody request: UpdateRideStatusRequest,
  ): ResponseEntity<ApiResponse<*>> {
    rideService.updateRideStatus(
        id,
        UUID.fromString(principal.name),
        request.status,
        request.cancelReason,
    )
    return ResponseEntity.ok(
        ApiResponse.success(mapOf("message" to "Ride status updated to ${request.status}"))
    )
  }

  // Update driver's current GPS location.
  @PostMapping("/location")
  fun updateLocation(
      principal: Principal,
      @RequestBody request: UpdateLocationRequest,
  ): ResponseEntity<ApiResponse<*>> {
    driverService.updateLocation(UUID.fromString(principal.name), request)
    return ResponseEntity.ok(ApiResponse.success(mapOf("message" to "Location updated")))
  }

  // Get available (searching) rides near the driver.
  @GetMapping("/rides/nearby")
  fun getNearbyRides(
      principal: Principal,
      @RequestParam lat: Double,
      @RequestParam lng: Double,
      @RequestParam(defaultValue = "5000.0") radius: Double,
  ): ResponseEntity<ApiResponse<*>> {
    val rides = rideService.getNearbyRides(lat, lng, radius)
    return ResponseEntity.ok(ApiResponse.success(rides))
  }

  // Get driver profile & online status
  @GetMapping("/status")
  fun getStatus(principal: Principal): ResponseEntity<ApiResponse<*>> {
    val driver = driverService.getDriverProfile(UUID.fromString(principal.name))
    return ResponseEntity.ok(ApiResponse.success(driver))
  }

  // Toggle driver online/offline
  @PostMapping("/online")
  fun setOnline(
      principal: Principal,
      @RequestParam isOnline: Boolean,
  ): ResponseEntity<ApiResponse<*>> {
    driverService.setOnlineStatus(UUID.fromString(principal.name), isOnline)
    return ResponseEntity.ok(
        ApiResponse.success(
            mapOf("message" to if (isOnline) "You are now online" else "You are now offline")
        )
    )
  }
}
