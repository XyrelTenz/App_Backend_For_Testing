package com.xyrel.app.websocket

import com.xyrel.app.dto.response.RideResponse
import com.xyrel.app.service.RideService
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class RideWebSocketController(private val rideService: RideService) {
  private val log = LoggerFactory.getLogger(RideWebSocketController::class.java)

  /**
   * Broadcasts ride status updates to subscribers of /topic/ride/{rideId}
   * This is used for real-time tracking of a specific ride.
   */
  @MessageMapping("/ride/{rideId}")
  @SendTo("/topic/ride/{rideId}")
  fun broadcastRideUpdate(@DestinationVariable rideId: UUID): RideResponse {
    log.info("Broadcasting update for ride: $rideId")
    return rideService.getRide(rideId)
  }

  /**
   * Broadcasts driver location updates to subscribers of /topic/ride/{rideId}/location
   */
  @MessageMapping("/ride/{rideId}/location")
  @SendTo("/topic/ride/{rideId}/location")
  fun broadcastDriverLocation(
      @DestinationVariable rideId: UUID,
      location: Map<String, Double>
  ): Map<String, Double> {
    log.debug("Broadcasting location for ride $rideId: $location")
    return location
  }
}
