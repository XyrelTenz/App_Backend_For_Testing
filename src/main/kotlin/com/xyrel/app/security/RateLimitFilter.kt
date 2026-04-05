package com.xyrel.app.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.xyrel.app.common.ApiResponse
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component

@Component
class RateLimitFilter(
    @param:Value("\${" + "rate-limit.capacity}") private val capacity: Long,
    @param:Value("\${" + "rate-limit.refill-duration-seconds}")
    private val refillDurationSeconds: Long,
    private val objectMapper: ObjectMapper,
) : Filter {

  private val log = LoggerFactory.getLogger(RateLimitFilter::class.java)
  private val buckets = ConcurrentHashMap<String, Bucket>()

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest = request as HttpServletRequest
    val httpResponse = response as HttpServletResponse
    val clientIp = getClientIp(httpRequest)
    val bucket = buckets.computeIfAbsent(clientIp) { createBucket() }

    if (bucket.tryConsume(1)) {
      chain.doFilter(request, response)
    } else {
      log.warn("Rate limit exceeded for IP: $clientIp")
      httpResponse.status = HttpStatus.TOO_MANY_REQUESTS.value()
      httpResponse.contentType = MediaType.APPLICATION_JSON_VALUE
      objectMapper.writeValue(
          httpResponse.outputStream,
          ApiResponse.error("Too many requests. Please try again later."),
      )
    }
  }

  private fun createBucket(): Bucket {
    val bandwidth =
        Bandwidth.builder()
            .capacity(capacity)
            .refillGreedy(capacity, Duration.ofSeconds(refillDurationSeconds))
            .build()
    return Bucket.builder().addLimit(bandwidth).build()
  }

  private fun getClientIp(request: HttpServletRequest): String {
    val xForwardedFor = request.getHeader("X-Forwarded-For")
    return if (!xForwardedFor.isNullOrBlank()) {
      xForwardedFor.split(",").first().trim()
    } else {
      request.remoteAddr
    }
  }
}
