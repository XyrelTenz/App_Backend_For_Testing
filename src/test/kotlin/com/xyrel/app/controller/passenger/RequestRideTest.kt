package com.xyrel.app.controller.passenger

import com.xyrel.app.controller.PassengerController
import com.xyrel.app.controller.BaseControllerTest

import com.xyrel.app.dto.request.RequestRideRequest
import com.xyrel.app.dto.response.RideResponse
import com.xyrel.app.service.PassengerService
import com.xyrel.app.service.RideService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@WebMvcTest(PassengerController::class)
class PassengerControllerTest : BaseControllerTest() {

    @MockBean
    private lateinit var rideService: RideService

    @MockBean
    private lateinit var passengerService: PassengerService

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001", roles = ["PASSENGER"])
    fun `requestRide should return 201 created`() {
        val request = RequestRideRequest(
            pickupAddress = "Point A", pickupLat = 10.0, pickupLng = 10.0,
            dropoffAddress = "Point B", dropoffLat = 11.0, dropoffLng = 11.0
        )
        val rideId = UUID.randomUUID()
        val response = RideResponse(
            id = rideId, status = "searching", passengerId = UUID.fromString("00000000-0000-0000-0000-000000000001"),
            driverId = null, pickupAddress = "A", pickupLat = 10.0, pickupLng = 10.0,
            dropoffAddress = "B", dropoffLat = 11.0, dropoffLng = 11.0,
            distanceKm = BigDecimal("1.5"), estimatedDurationMins = 10, estimatedFareAmount = BigDecimal("50.0"), finalFareAmount = null,
            paymentMethod = "cash", paymentStatus = "pending", scheduledAt = null, startedAt = null, completedAt = null, cancelledAt = null,
            cancelReason = null, createdAt = Instant.now()
        )

        `when`(rideService.requestRide(any(), any())).thenReturn(response)

        mockMvc.perform(
            post("/api/passenger/ride/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.status").value("searching"))
    }
}
