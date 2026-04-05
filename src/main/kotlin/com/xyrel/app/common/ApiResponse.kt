package com.xyrel.app.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errors: Any? = null,
) {
  companion object {
    fun <T> success(data: T, message: String = "Success") =
        ApiResponse(success = true, message = message, data = data)

    fun <T> created(data: T, message: String = "Created") =
        ApiResponse(success = true, message = message, data = data)

    fun error(message: String, errors: Any? = null) =
        ApiResponse<Nothing>(success = false, message = message, errors = errors)

    fun unauthorized(message: String = "Unauthorized") =
        ApiResponse<Nothing>(success = false, message = message)

    fun validationError(errors: Any?) =
        ApiResponse<Nothing>(success = false, message = "Validation failed", errors = errors)
  }
}
