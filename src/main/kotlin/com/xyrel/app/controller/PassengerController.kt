package com.xyrel.app.controller

import com.xyrel.app.common.ApiResponse
import com.xyrel.app.dto.request.AddSavedPlaceRequest
import com.xyrel.app.dto.request.RequestRideRequest
import com.xyrel.app.service.PassengerService
import com.xyrel.app.service.RideService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.security.Principal
import java.util.UUID

@RestController
@RequestMapping("/api/passenger")
@PreAuthorize("hasAnyRole('PASSENGER', 'DRIVER')")  // both roles can view rides
class PassengerController(
    private val rideService: RideService,
    private val passengerService: PassengerService
) {

    /**
     * POST /api/passenger/ride/request
     * Passenger requests a new ride.
     */
    @PostMapping("/ride/request")
    fun requestRide(
        principal: Principal,
        @Valid @RequestBody request: RequestRideRequest
    ): ResponseEntity<ApiResponse<*>> {
        val ride = rideService.requestRide(UUID.fromString(principal.name), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(ride))
    }

    /**
     * GET /api/passenger/ride/{id}
     * Get a single ride by ID.
     */
    @GetMapping("/ride/{id}")
    fun getRide(
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<*>> {
        val ride = rideService.getRide(id)
        return ResponseEntity.ok(ApiResponse.success(ride))
    }

    /**
     * GET /api/passenger/history
     * Get all rides for the authenticated passenger.
     */
    @GetMapping("/history")
    fun getHistory(
        principal: Principal
    ): ResponseEntity<ApiResponse<*>> {
        val rides = rideService.getPassengerHistory(UUID.fromString(principal.name))
        return ResponseEntity.ok(ApiResponse.success(rides))
    }

    /**
     * POST /api/passenger/saved-places
     * Add a new saved place.
     */
    @PostMapping("/saved-places")
    fun addSavedPlace(
        principal: Principal,
        @Valid @RequestBody request: AddSavedPlaceRequest
    ): ResponseEntity<ApiResponse<*>> {
        val place = passengerService.addSavedPlace(UUID.fromString(principal.name), request)
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(place))
    }

    /**
     * GET /api/passenger/saved-places
     * Get all saved places for the authenticated passenger.
     */
    @GetMapping("/saved-places")
    fun getSavedPlaces(
        principal: Principal
    ): ResponseEntity<ApiResponse<*>> {
        val places = passengerService.getSavedPlaces(UUID.fromString(principal.name))
        return ResponseEntity.ok(ApiResponse.success(places))
    }

    /**
     * DELETE /api/passenger/saved-places/{id}
     * Remove a saved place.
     */
    @DeleteMapping("/saved-places/{id}")
    fun deleteSavedPlace(
        principal: Principal,
        @PathVariable id: UUID
    ): ResponseEntity<ApiResponse<*>> {
        passengerService.deleteSavedPlace(UUID.fromString(principal.name), id)
        return ResponseEntity.ok(ApiResponse.success(mapOf("message" to "Place deleted")))
    }
}
