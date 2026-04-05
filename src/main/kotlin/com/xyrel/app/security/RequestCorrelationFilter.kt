package com.xyrel.app.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.util.UUID
import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Assigns a unique correlation ID (X-Request-Id) to every incoming request. Logged alongside all
 * request events for distributed tracing.
 */
@Component
@Order(1)
class RequestCorrelationFilter : Filter {

  private val log = LoggerFactory.getLogger(RequestCorrelationFilter::class.java)

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    val httpRequest = request as HttpServletRequest
    val existingId = httpRequest.getHeader("X-Request-Id")
    val correlationId = if (existingId.isNullOrBlank()) UUID.randomUUID().toString() else existingId

    val wrapped = RequestIdWrapper(httpRequest, correlationId)
    log.debug("→ [{} {}] requestId={}", httpRequest.method, httpRequest.requestURI, correlationId)
    chain.doFilter(wrapped, response)
  }

  private class RequestIdWrapper(request: HttpServletRequest, private val correlationId: String) :
      HttpServletRequestWrapper(request) {
    override fun getHeader(name: String): String? =
        if (name.equals("X-Request-Id", ignoreCase = true)) correlationId else super.getHeader(name)
  }
}
