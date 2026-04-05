package com.xyrel.app.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Security audit logger — logs every incoming request with:
 * - HTTP method + path
 * - Client IP
 * - Response status
 * - Duration in ms
 * - X-Request-Id correlation ID
 *
 * Suspicious events (401, 403, 429) are logged at WARN level for alerting.
 */
@Component
@Order(3)
class SecurityAuditFilter : Filter {

  private val log = LoggerFactory.getLogger("SECURITY_AUDIT")

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpReq = request as HttpServletRequest
    val httpRes = response as HttpServletResponse
    val start = System.currentTimeMillis()
    val requestId = httpReq.getHeader("X-Request-Id") ?: "-"
    val ip = getClientIp(httpReq)

    try {
      chain.doFilter(request, response)
    } finally {
      val duration = System.currentTimeMillis() - start
      val status = httpRes.status
      val msg = "[{}] {} {} → {} ({}ms) ip={}"
      val args = arrayOf(requestId, httpReq.method, httpReq.requestURI, status, duration, ip)

      when {
        status == 401 || status == 403 -> log.warn("AUTH_FAILURE $msg", *args)
        status == 429 -> log.warn("RATE_LIMIT $msg", *args)
        status >= 500 -> log.error("SERVER_ERROR $msg", *args)
        else -> log.info(msg, *args)
      }
    }
  }

  private fun getClientIp(request: HttpServletRequest): String {
    return request.getHeader("X-Forwarded-For")?.split(",")?.firstOrNull()?.trim()
        ?: request.remoteAddr
  }
}
