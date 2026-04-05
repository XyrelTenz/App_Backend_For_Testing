package com.xyrel.app.security

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class CorsConfig(
    @param:Value("\${" + "spring.websocket.allowed-origins}") private val allowedOrigins: String
) {

  @Bean
  fun corsConfigurationSource(): CorsConfigurationSource {
    val config = CorsConfiguration()
    config.allowedOriginPatterns = listOf(allowedOrigins)
    config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
    config.allowedHeaders =
        listOf(
            "Content-Type",
            "Content-Length",
            "Accept-Encoding",
            "X-CSRF-Token",
            "Authorization",
            "accept",
            "origin",
            "Cache-Control",
            "X-Requested-With",
        )
    config.exposedHeaders = listOf("Content-Length")
    config.allowCredentials = true
    config.maxAge = 3600L

    val source = UrlBasedCorsConfigurationSource()
    source.registerCorsConfiguration("/**", config)
    return source
  }
}
