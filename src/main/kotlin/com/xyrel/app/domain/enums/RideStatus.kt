package com.xyrel.app.domain.enums

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class RideStatus(@JsonValue val value: String) {
  SEARCHING("SEARCHING"),
  DRIVER_ASSIGNED("DRIVER_ASSIGNED"),
  DRIVER_EN_ROUTE("DRIVER_EN_ROUTE"),
  DRIVER_ARRIVED("DRIVER_ARRIVED"),
  IN_PROGRESS("IN_PROGRESS"),
  COMPLETED("COMPLETED"),
  CANCELLED("CANCELLED");

  companion object {
    @JvmStatic
    @JsonCreator
    fun fromValue(value: String): RideStatus =
        entries.firstOrNull { it.value.equals(value, ignoreCase = true) }
            ?: throw IllegalArgumentException("Unknown ride status: $value")
  }
}
