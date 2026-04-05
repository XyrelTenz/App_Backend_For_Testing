package com.xyrel.app.service

import com.xyrel.app.common.NotFoundException
import com.xyrel.app.dto.request.UpdateLocationRequest
import com.xyrel.app.dto.response.DriverResponse
import com.xyrel.app.repository.DriverRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DriverService(private val driverRepository: DriverRepository) {

  @Transactional(readOnly = true)
  fun getDriverProfile(userId: UUID): DriverResponse {
    val driver =
        driverRepository.findByUserId(userId).orElseThrow {
          NotFoundException("Driver profile not found for user: $userId")
        }
    return driver.toResponse()
  }

  @Transactional
  fun updateLocation(userId: UUID, request: UpdateLocationRequest) {
    driverRepository.findByUserId(userId).orElseThrow {
      NotFoundException("Driver not found: $userId")
    }
    driverRepository.updateLocation(userId, request.lat, request.lng)
  }

  @Transactional
  fun setOnlineStatus(userId: UUID, isOnline: Boolean) {
    val driver =
        driverRepository.findByUserId(userId).orElseThrow {
          NotFoundException("Driver not found: $userId")
        }
    driver.isOnline = isOnline
    driverRepository.save(driver)
  }
}

private fun com.xyrel.app.domain.entity.Driver.toResponse() =
    DriverResponse(
        userId = userId,
        plateNumber = plateNumber,
        vehicleType = vehicleType,
        vehicleColor = vehicleColor,
        licenseNumber = licenseNumber,
        isVerified = isVerified,
        isOnline = isOnline,
    )
