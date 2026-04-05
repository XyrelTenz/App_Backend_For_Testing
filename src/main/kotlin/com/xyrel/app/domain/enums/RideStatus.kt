package com.xyrel.app.domain.enums

enum class RideStatus(val value: String) {
  SEARCHING("searching"),
  DRIVER_ASSIGNED("driver_assigned"),
  DRIVER_EN_ROUTE("driver_en_route"),
  DRIVER_ARRIVED("driver_arrived"),
  IN_PROGRESS("in_progress"),
  COMPLETED("completed"),
  CANCELLED("cancelled");

  companion object {
    fun fromValue(value: String): RideStatus =
        entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown ride status: $value")
  }
}
