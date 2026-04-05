package com.xyrel.app.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.xyrel.app.security.JwtAuthFilter
import com.xyrel.app.security.RateLimitFilter
import com.xyrel.app.security.SecurityConfig
import com.xyrel.app.service.JwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.web.servlet.MockMvc

@TestConfiguration
class MockSecurityConfig {
    @Bean
    @Primary
    fun jwtAuthFilterMock(): JwtAuthFilter = object : JwtAuthFilter(jwtServiceMock()) {
        override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
            filterChain.doFilter(request, response)
        }
    }

    @Bean
    @Primary
    fun rateLimitFilterMock(objectMapper: ObjectMapper): RateLimitFilter = object : RateLimitFilter(100, 60, objectMapper) {
        override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
            chain.doFilter(request, response)
        }
    }

    @Bean
    fun jwtServiceMock(): JwtService = org.mockito.Mockito.mock(JwtService::class.java)
}

@Import(SecurityConfig::class, MockSecurityConfig::class)
abstract class BaseControllerTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper
}
