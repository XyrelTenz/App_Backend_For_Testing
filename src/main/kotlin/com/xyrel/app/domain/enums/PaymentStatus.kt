package com.xyrel.app.domain.enums

enum class PaymentStatus(val value: String) {
    PENDING("pending"),
    COLLECTED("collected");

    companion object {
        fun fromValue(value: String): PaymentStatus =
            entries.firstOrNull { it.value.equals(value, ignoreCase = true) } ?: PENDING
    }
}
