package com.xyrel.app.service

import kotlin.math.*
import org.springframework.stereotype.Service

/** Fare calculation service — mirrors Go domain/ride.go logic. Base fare: ₱40, per KM rate: ₱20 */
@Service
class FareCalculatorService {
  companion object {
    const val FARE_PER_KM = 20.0
    const val MINIMUM_FARE = 40.0
    const val EARTH_RADIUS_KM = 6371.0
    const val SPEED_KM_PER_MIN = 1.0 / 3.0 // ~20 km/h → 3 mins per km
  }

  // Haversine distance in kilometers.
  fun calculateDistanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
        sin(dLat / 2).pow(2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return EARTH_RADIUS_KM * c
  }

  // Compute fare in PHP. Min ₱40, then ₱20 per km above 2 km.
  fun calculateFare(distanceKm: Double): Double = maxOf(MINIMUM_FARE, distanceKm * FARE_PER_KM)

  // Estimated travel duration in minutes (simple estimate).
  fun estimateDurationMins(distanceKm: Double): Int =
      (distanceKm / SPEED_KM_PER_MIN).toInt().coerceAtLeast(1)

  // Build PostGIS WKT point: longitude first per GeoJSON
  fun toWkt(lat: Double, lng: Double): String = "SRID=4326;POINT($lng $lat)"

  // Parse lat from WKT "SRID=4326;POINT(lng lat)"
  fun latFromWkt(wkt: String): Double {
    val coords = wkt.substringAfter("POINT(").substringBefore(")").split(" ")
    return coords[1].toDouble()
  }

  // Parse lng from WKT
  fun lngFromWkt(wkt: String): Double {
    val coords = wkt.substringAfter("POINT(").substringBefore(")").split(" ")
    return coords[0].toDouble()
  }
}
