package com.xyrel.app.service

import com.xyrel.app.common.ConflictException
import com.xyrel.app.common.UnauthorizedException
import com.xyrel.app.domain.entity.Driver
import com.xyrel.app.domain.entity.User
import com.xyrel.app.domain.entity.UserProfile
import com.xyrel.app.domain.enums.UserRole
import com.xyrel.app.dto.request.FirebaseLoginRequest
import com.xyrel.app.dto.request.FirebaseSignupRequest
import com.xyrel.app.dto.response.AuthResponse
import com.xyrel.app.repository.DriverRepository
import com.xyrel.app.repository.UserProfileRepository
import com.xyrel.app.repository.UserRepository
import com.xyrel.app.security.BruteForceProtectionService
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val userProfileRepository: UserProfileRepository,
    private val driverRepository: DriverRepository,
    private val firebaseTokenVerifier: FirebaseTokenVerifier,
    private val jwtService: JwtService,
    private val bruteForce: BruteForceProtectionService,
    private val passwordEncoder: PasswordEncoder,
) {
  private val log = LoggerFactory.getLogger(AuthService::class.java)

  // Firebase Sign up
  @Transactional
  fun signup(request: FirebaseSignupRequest): AuthResponse {
    val tokenKey = "signup:${request.idToken.take(20)}"
    bruteForce.checkBlocked(tokenKey)

    val firebaseToken =
        try {
          firebaseTokenVerifier.verifyIdToken(request.idToken)
        } catch (e: Exception) {
          bruteForce.recordFailure(tokenKey)
          throw e
        }

    val firebaseUid = firebaseToken.uid
    val email =
        firebaseToken.email ?: throw UnauthorizedException("Firebase token does not contain email")

    val user =
        handleExistingFirebaseUser(firebaseUid, email)
            ?: createNewFirebaseUser(request, firebaseUid, email)

    return generateAuthResponse(user)
  }

  private fun handleExistingFirebaseUser(firebaseUid: String, email: String): User? {
    val existingUser =
        userRepository
            .findByFirebaseUid(firebaseUid)
            .or { userRepository.findByEmail(email) }
            .orElse(null) ?: return null

    if (existingUser.firebaseUid != null && existingUser.firebaseUid != firebaseUid) {
      throw ConflictException("Email already associated with another account.")
    }

    if (existingUser.firebaseUid == null) {
      existingUser.firebaseUid = firebaseUid
      userRepository.save(existingUser)
      log.info("Linked Google account to existing user: ${existingUser.email}")
    }
    return existingUser
  }

  private fun createNewFirebaseUser(
      request: FirebaseSignupRequest,
      firebaseUid: String,
      email: String,
  ): User {
    val role = UserRole.fromValue(request.role)
    val user =
        User(
            firebaseUid = firebaseUid,
            email = email,
            phone = request.phone,
            fullName = request.fullName,
            role = role,
        )
    user.profile = UserProfile(userId = user.id, user = user)

    if (role == UserRole.DRIVER) {
      val plateNumber =
          request.plateNumber
              ?: throw com.xyrel.app.common.BadRequestException("Plate number required")
      val licenseNumber =
          request.licenseNumber
              ?: throw com.xyrel.app.common.BadRequestException("License number required")

      user.driver =
          Driver(
              userId = user.id,
              user = user,
              plateNumber = plateNumber,
              vehicleType = request.vehicleType ?: "baobao_etrike",
              vehicleColor = request.vehicleColor,
              licenseNumber = licenseNumber,
          )
    }

    val savedUser = userRepository.save(user)
    log.info("New user registered: ${savedUser.email} as ${role.name}")
    return savedUser
  }

  // Firebase Login
  @Transactional(readOnly = true)
  fun login(request: FirebaseLoginRequest): AuthResponse {
    // Rate-limit login attempts by token prefix
    val tokenKey = "login:${request.idToken.take(20)}"
    bruteForce.checkBlocked(tokenKey)

    val firebaseToken =
        try {
          firebaseTokenVerifier.verifyIdToken(request.idToken)
        } catch (e: Exception) {
          bruteForce.recordFailure(tokenKey)
          throw e
        }

    val user =
        try {
          userRepository.findByFirebaseUid(firebaseToken.uid).orElseThrow {
            UnauthorizedException("User not found. Please sign up first.")
          }
        } catch (e: UnauthorizedException) {
          bruteForce.recordFailure(tokenKey)
          throw e
        }

    if (!user.isActive) {
      bruteForce.recordFailure(tokenKey)
      throw UnauthorizedException("Account is deactivated. Contact support.")
    }

    // Successful login — clear any failure record
    bruteForce.recordSuccess(tokenKey)
    log.info("User logged in: ${user.email}")

    return generateAuthResponse(user)
  }

  // Native Sign up
  @Transactional
  fun signupNative(request: com.xyrel.app.dto.request.NativeSignupRequest): AuthResponse {
    val emailKey = "signup:${request.email}"
    bruteForce.checkBlocked(emailKey)

    if (userRepository.existsByEmail(request.email)) {
      throw ConflictException("Email already registered.")
    }

    val role = UserRole.fromValue(request.role)
    val user =
        User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            phone = request.phone,
            fullName = request.fullName,
            role = role,
        )
    user.profile = UserProfile(userId = user.id, user = user)

    if (role == UserRole.DRIVER) {
      val plateNumber =
          request.plateNumber
              ?: throw com.xyrel.app.common.BadRequestException("Plate number required")
      val licenseNumber =
          request.licenseNumber
              ?: throw com.xyrel.app.common.BadRequestException("License number required")

      user.driver =
          Driver(
              userId = user.id,
              user = user,
              plateNumber = plateNumber,
              vehicleType = request.vehicleType ?: "baobao_etrike",
              vehicleColor = request.vehicleColor,
              licenseNumber = licenseNumber,
          )
    }

    val savedUser = userRepository.save(user)
    log.info("New native user registered: ${savedUser.email} as ${role.name}")

    return generateAuthResponse(savedUser)
  }

  // Native Login
  @Transactional(readOnly = true)
  fun loginNative(request: com.xyrel.app.dto.request.NativeLoginRequest): AuthResponse {
    val emailKey = "login:${request.email}"
    bruteForce.checkBlocked(emailKey)

    val user =
        userRepository.findByEmail(request.email).orElseThrow {
          bruteForce.recordFailure(emailKey)
          UnauthorizedException("Invalid email or password.")
        }

    if (
        user.passwordHash == null || !passwordEncoder.matches(request.password, user.passwordHash)
    ) {
      bruteForce.recordFailure(emailKey)
      throw UnauthorizedException("Invalid email or password.")
    }

    if (!user.isActive) {
      throw UnauthorizedException("Account is deactivated.")
    }

    bruteForce.recordSuccess(emailKey)
    log.info("Native user logged in: ${user.email}")

    return generateAuthResponse(user)
  }

  private fun generateAuthResponse(user: User): AuthResponse {
    val token = jwtService.generateToken(user.id.toString(), user.role.name)
    return AuthResponse(
        token = token,
        userId = user.id.toString(),
        role = user.role.name,
        fullName = user.fullName,
        email = user.email,
    )
  }
}
