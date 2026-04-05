package com.xyrel.app.security

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import org.owasp.encoder.Encode
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

// Sanitizes user-supplied input against XSS attacks. Wraps all request parameters and headers so
@Component
@Order(2)
class XssProtectionFilter : Filter {

  override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
    chain.doFilter(XssSafeRequestWrapper(request as HttpServletRequest), response)
  }

  private class XssSafeRequestWrapper(request: HttpServletRequest) :
      HttpServletRequestWrapper(request) {

    override fun getParameter(name: String): String? =
        super.getParameter(name)?.let { sanitize(it) }

    override fun getParameterValues(name: String): Array<String>? =
        super.getParameterValues(name)?.map { sanitize(it) }?.toTypedArray()

    override fun getHeader(name: String): String? =
        super.getHeader(name)?.let {
          // Never sanitize the Authorization header (breaks JWT)
          if (name.equals("Authorization", ignoreCase = true)) it else sanitize(it)
        }

    private fun sanitize(value: String): String = Encode.forHtml(value)
  }
}
