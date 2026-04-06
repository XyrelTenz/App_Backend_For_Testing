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
    // Check brute-force block on the raw token prefix
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

    if (userRepository.existsByFirebaseUid(firebaseUid)) {
      throw ConflictException("User already registered. Please use login instead.")
    }

    val role = UserRole.fromValue(request.role)

    // Create User
    val user =
        User(
            firebaseUid = firebaseUid,
            email = email,
            phone = request.phone,
            fullName = request.fullName,
            role = role,
        )
    val savedUser = userRepository.save(user)

    // Create Profile
    val profile = UserProfile(userId = savedUser.id, user = savedUser)
    userProfileRepository.save(profile)

    // Create Driver record if registering as driver
    if (role == UserRole.DRIVER) {
      validateDriverFields(request)
      val driver =
          Driver(
              userId = savedUser.id,
              user = savedUser,
              plateNumber = request.plateNumber!!,
              vehicleType = request.vehicleType ?: "baobao_etrike",
              vehicleColor = request.vehicleColor,
              licenseNumber = request.licenseNumber!!,
          )
      driverRepository.save(driver)
    }

    val token = jwtService.generateToken(savedUser.id.toString(), role.name)
    log.info("New user registered: ${savedUser.email} as ${role.name}")

    return AuthResponse(
        token = token,
        userId = savedUser.id.toString(),
        role = role.name,
        fullName = savedUser.fullName,
        email = savedUser.email,
    )
  }

  /**
   * Firebase Login:
   * 1. Verify Firebase ID token
   * 2. Find existing user by firebase_uid
   * 3. Return new JWT
   */
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

    val token = jwtService.generateToken(user.id.toString(), user.role.name)
    log.info("User logged in: ${user.email}")

    return AuthResponse(
        token = token,
        userId = user.id.toString(),
        role = user.role.name,
        fullName = user.fullName,
        email = user.email,
    )
  }

  /**
   * Native Signup:
   * 1. Check if user exists by email
   * 2. Hash password
   * 3. Create user + profile + driver record
   * 4. Return JWT
   */
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
    val savedUser = userRepository.save(user)

    // Create Profile
    val profile = UserProfile(userId = savedUser.id, user = savedUser)
    userProfileRepository.save(profile)

    // Create Driver record if registering as driver
    if (role == UserRole.DRIVER) {
      validateNativeDriverFields(request)
      val driver =
          Driver(
              userId = savedUser.id,
              user = savedUser,
              plateNumber = request.plateNumber!!,
              vehicleType = request.vehicleType ?: "baobao_etrike",
              vehicleColor = request.vehicleColor,
              licenseNumber = request.licenseNumber!!,
          )
      driverRepository.save(driver)
    }

    val token = jwtService.generateToken(savedUser.id.toString(), role.name)
    log.info("New native user registered: ${savedUser.email} as ${role.name}")

    return AuthResponse(
        token = token,
        userId = savedUser.id.toString(),
        role = role.name,
        fullName = savedUser.fullName,
        email = savedUser.email,
    )
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

    val token = jwtService.generateToken(user.id.toString(), user.role.name)
    log.info("Native user logged in: ${user.email}")

    return AuthResponse(
        token = token,
        userId = user.id.toString(),
        role = user.role.name,
        fullName = user.fullName,
        email = user.email,
    )
  }

  private fun validateDriverFields(request: FirebaseSignupRequest) {
    // Required Plate Number and License Number for drivers
    if (request.plateNumber.isNullOrBlank())
        throw com.xyrel.app.common.BadRequestException("Plate number is required for drivers")
    if (request.licenseNumber.isNullOrBlank())
        throw com.xyrel.app.common.BadRequestException("License number is required for drivers")
  }

  private fun validateNativeDriverFields(request: com.xyrel.app.dto.request.NativeSignupRequest) {
    // Required Plate Number and License Number for drivers
    if (request.plateNumber.isNullOrBlank())
        throw com.xyrel.app.common.BadRequestException("Plate number is required for drivers")
    if (request.licenseNumber.isNullOrBlank())
        throw com.xyrel.app.common.BadRequestException("License number is required for drivers")
  }
}
