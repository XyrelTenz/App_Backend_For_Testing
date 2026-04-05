package com.xyrel.app.service

import com.xyrel.app.common.UnauthorizedException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import javax.crypto.SecretKey
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class JwtService(
    @Value("\${" + "jwt.secret}") secretKey: String,
    @param:Value("\${" + "jwt.expiration-ms}") private val expirationMs: Long,
) {
  private val signingKey: SecretKey = Keys.hmacShaKeyFor(secretKey.toByteArray())
  private val issuer = "driving-app-backend"

  fun generateToken(userId: String, role: String): String {
    val now = Date()
    val expiry = Date(now.time + expirationMs)

    return Jwts.builder()
        .issuer(issuer)
        .subject(userId)
        .claim("role", role)
        .issuedAt(now)
        .expiration(expiry)
        .signWith(signingKey)
        .compact()
  }

  fun validateToken(token: String): Claims {
    return try {
      Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).payload
    } catch (e: JwtException) {
      throw UnauthorizedException("Invalid or expired JWT token")
    } catch (e: IllegalArgumentException) {
      throw UnauthorizedException("JWT token is empty or malformed")
    }
  }

  fun extractUserId(token: String): String = validateToken(token).subject

  fun extractRole(token: String): String = validateToken(token)["role", String::class.java]
}
