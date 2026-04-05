package com.xyrel.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.xyrel.app.common.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.AuthenticationException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
    private val objectMapper: ObjectMapper,
) {

  @Bean
  fun passwordEncoder(): PasswordEncoder {
    return BCryptPasswordEncoder()
  }

  @Bean
  fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
    http
        // Disable CSRF (stateless JWT API)
        .csrf { it.disable() }

        // Stateless sessions
        .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        // CORS (handled by CorsConfig bean)
        .cors {}

        // Security headers
        .headers { headers ->
          headers.frameOptions { it.deny() }
          headers.contentTypeOptions {}
          headers.xssProtection {}
          headers.httpStrictTransportSecurity { hsts ->
            hsts.includeSubDomains(true).maxAgeInSeconds(31536000)
          }
          headers.referrerPolicy {
            it.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
          }
          headers.contentSecurityPolicy { it.policyDirectives("default-src 'self'") }
        }

        // Authorization rules
        .authorizeHttpRequests { auth ->
          auth
              .requestMatchers(HttpMethod.OPTIONS, "/**")
              .permitAll()
              .requestMatchers("/auth/**")
              .permitAll()
              .requestMatchers("/actuator/health", "/actuator/info")
              .permitAll()
              .requestMatchers("/ws/**")
              .permitAll()
              .anyRequest()
              .authenticated()
        }

        // Custom unauthorized response
        .exceptionHandling { ex -> ex.authenticationEntryPoint(authenticationEntryPoint()) }

        // JWT filter
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

    return http.build()
  }

  @Bean
  fun authenticationEntryPoint(): AuthenticationEntryPoint =
      AuthenticationEntryPoint {
          _: HttpServletRequest,
          response: HttpServletResponse,
          _: AuthenticationException ->
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        objectMapper.writeValue(response.outputStream, ApiResponse.unauthorized())
      }
}
