package com.xyrel.app.common

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

  @ExceptionHandler(AppException::class)
  fun handleAppException(ex: AppException): ResponseEntity<ApiResponse<Nothing>> {
    return ResponseEntity.status(ex.httpStatus)
        .body(ApiResponse.error(ex.message ?: "An error occurred"))
  }

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleValidationError(
      ex: MethodArgumentNotValidException
  ): ResponseEntity<ApiResponse<Nothing>> {
    val errors =
        ex.bindingResult.fieldErrors.associate {
          it.field to (it.defaultMessage ?: "Invalid value")
        }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.validationError(errors))
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun handleConstraintViolation(
      ex: ConstraintViolationException
  ): ResponseEntity<ApiResponse<Nothing>> {
    val errors = ex.constraintViolations.associate { it.propertyPath.toString() to it.message }
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.validationError(errors))
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDenied(ex: AccessDeniedException): ResponseEntity<ApiResponse<Nothing>> =
      ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Access denied"))

  @ExceptionHandler(BadCredentialsException::class)
  fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<ApiResponse<Nothing>> =
      ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponse.unauthorized("Invalid credentials"))

  @ExceptionHandler(Exception::class)
  fun handleGenericException(ex: Exception): ResponseEntity<ApiResponse<Nothing>> {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("An unexpected error occurred: ${ex.message}"))
  }
}
