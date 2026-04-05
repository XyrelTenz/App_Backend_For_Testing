package com.xyrel.app.controller.driver

import com.xyrel.app.controller.DriverController
import com.xyrel.app.controller.BaseControllerTest

import com.xyrel.app.dto.request.UpdateRideStatusRequest
import com.xyrel.app.service.DriverService
import com.xyrel.app.service.RideService
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doNothing
import org.mockito.kotlin.any
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@WebMvcTest(DriverController::class)
class DriverControllerTest : BaseControllerTest() {

    @MockBean
    private lateinit var rideService: RideService

    @MockBean
    private lateinit var driverService: DriverService

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000002", roles = ["DRIVER"])
    fun `updateRideStatus should return 200 ok`() {
        val request = UpdateRideStatusRequest(status = "en_route")
        
        doNothing().`when`(rideService).updateRideStatus(any(), any(), any(), any())

        mockMvc.perform(
            patch("/api/driver/ride/${UUID.randomUUID()}/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
    }
}
