package com.xyrel.app.security

import com.xyrel.app.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(private val jwtService: JwtService) : OncePerRequestFilter() {

  override fun doFilterInternal(
      request: HttpServletRequest,
      response: HttpServletResponse,
      filterChain: FilterChain,
  ) {
    val authHeader = request.getHeader("Authorization")

    if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
      filterChain.doFilter(request, response)
      return
    }

    val token = authHeader.removePrefix("Bearer ").trim()

    try {
      val claims = jwtService.validateToken(token)
      val userId = claims.subject
      val role = claims["role", String::class.java] ?: "NONE"

      if (SecurityContextHolder.getContext().authentication == null) {
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        val auth = UsernamePasswordAuthenticationToken(userId, null, authorities)
        auth.details = WebAuthenticationDetailsSource().buildDetails(request)
        SecurityContextHolder.getContext().authentication = auth
      }
    } catch (e: Exception) {
      // Invalid token — let request proceed without auth; SecurityConfig will reject protected
      // endpoints
      logger.debug("JWT validation failed: ${e.message}")
    }

    filterChain.doFilter(request, response)
  }
}
