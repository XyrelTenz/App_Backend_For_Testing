package com.xyrel.app.domain.enums

enum class UserRole(val value: String) {
  PASSENGER("passenger"),
  DRIVER("driver"),
  NONE("none");

  companion object {
    fun fromValue(value: String): UserRole =
        entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: NONE
  }
}
