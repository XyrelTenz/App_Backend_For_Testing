package com.xyrel.app.service

import com.xyrel.app.common.NotFoundException
import com.xyrel.app.domain.entity.SavedPlace
import com.xyrel.app.dto.request.AddSavedPlaceRequest
import com.xyrel.app.dto.response.SavedPlaceResponse
import com.xyrel.app.repository.SavedPlaceRepository
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PassengerService(
    private val savedPlaceRepository: SavedPlaceRepository,
    private val fareCalculator: FareCalculatorService,
) {

  @Transactional
  fun addSavedPlace(userId: UUID, request: AddSavedPlaceRequest): SavedPlaceResponse {
    val place =
        SavedPlace(
            userId = userId,
            label = request.label,
            address = request.address,
            location = fareCalculator.toWkt(request.lat, request.lng),
        )
    val saved = savedPlaceRepository.save(place)
    return saved.toResponse(fareCalculator)
  }

  @Transactional(readOnly = true)
  fun getSavedPlaces(userId: UUID): List<SavedPlaceResponse> =
      savedPlaceRepository.findByUserId(userId).map { it.toResponse(fareCalculator) }

  @Transactional
  fun deleteSavedPlace(userId: UUID, placeId: UUID) {
    val deleted = savedPlaceRepository.deleteByIdAndUserId(placeId, userId)
    if (deleted == 0) throw NotFoundException("Saved place not found")
  }
}

private fun SavedPlace.toResponse(fareCalculator: FareCalculatorService) =
    SavedPlaceResponse(
        id = id,
        label = label,
        address = address,
        lat = fareCalculator.latFromWkt(location),
        lng = fareCalculator.lngFromWkt(location),
    )
