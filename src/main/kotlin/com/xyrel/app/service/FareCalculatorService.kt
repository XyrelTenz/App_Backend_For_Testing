package com.xyrel.app.service

import kotlin.math.*
import org.springframework.stereotype.Service

@Service
class FareCalculatorService {
  private val BASE_FARE = 20.0
  private val PER_KM_RATE = 15.0 // Price per kilometer
  private val PER_MIN_RATE = 2.0 // Price per minute

  // Calculates the estimated fare based on distance in kilometers.
  fun calculateFare(distanceKm: Double): Double {
    val distanceFare = distanceKm * PER_KM_RATE
    val estimatedTimeMins = estimateDurationMins(distanceKm)
    val timeFare = estimatedTimeMins * PER_MIN_RATE
    return BASE_FARE + distanceFare + timeFare
  }

  fun estimateDurationMins(distanceKm: Double): Int {
    // Rough estimate: 2.5 minutes per km in typical traffic
    return (distanceKm * 2.5).toInt()
  }

  // Haversine formula to calculate distance between two coordinates in kilometers.
  fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6371.0 // Radius of the earth in km
    val dLat = deg2rad(lat2 - lat1)
    val dLon = deg2rad(lon2 - lon1)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(deg2rad(lat1)) * cos(deg2rad(lat2)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return R * c
  }

  private fun deg2rad(deg: Double): Double = deg * (Math.PI / 180.0)

  fun toWkt(lat: Double, lng: Double): String = "POINT($lng $lat)"

  fun latFromWkt(wkt: String): Double {
    return wkt.replace("POINT(", "").replace(")", "").split(" ")[1].toDouble()
  }

  fun lngFromWkt(wkt: String): Double {
    return wkt.replace("POINT(", "").replace(")", "").split(" ")[0].toDouble()
  }
}
