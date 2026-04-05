package com.xyrel.app.common

import org.springframework.http.HttpStatus

// Sealed exception hierarchy for structured error handling.
sealed class AppException(
    message: String,
    val httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
) : RuntimeException(message)

class NotFoundException(message: String) : AppException(message, HttpStatus.NOT_FOUND)

class ConflictException(message: String) : AppException(message, HttpStatus.CONFLICT)

class UnauthorizedException(message: String = "Unauthorized") :
    AppException(message, HttpStatus.UNAUTHORIZED)

class ForbiddenException(message: String = "Forbidden") :
    AppException(message, HttpStatus.FORBIDDEN)

class BadRequestException(message: String) : AppException(message, HttpStatus.BAD_REQUEST)

class InternalException(message: String) : AppException(message, HttpStatus.INTERNAL_SERVER_ERROR)

class FirebaseAuthException(message: String) : AppException(message, HttpStatus.UNAUTHORIZED)
