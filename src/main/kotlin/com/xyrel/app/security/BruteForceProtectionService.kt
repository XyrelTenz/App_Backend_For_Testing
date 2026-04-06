package com.xyrel.app.security

import com.xyrel.app.common.UnauthorizedException
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class BruteForceProtectionService(
    @param:Value("\${" + "security.brute-force.max-attempts:5}") private val maxAttempts: Int,
    @param:Value("\${" + "security.brute-force.block-duration-seconds:900}")
    private val blockDurationSeconds: Long,
    @param:Value("\${" + "security.brute-force.window-seconds:300}") private val windowSeconds: Long,
) {
  private val log = LoggerFactory.getLogger(BruteForceProtectionService::class.java)

  data class AttemptRecord(
      val count: AtomicInteger = AtomicInteger(0),
      var firstAttemptAt: Instant = Instant.now(),
      var blockedUntil: Instant? = null,
  )

  private val attempts = ConcurrentHashMap<String, AttemptRecord>()

  // Check if the given identifier is currently blocked.
  fun checkBlocked(identifier: String) {
    val record = attempts[identifier] ?: return
    val blockedUntil = record.blockedUntil ?: return
    if (Instant.now().isBefore(blockedUntil)) {
      val remaining = blockedUntil.epochSecond - Instant.now().epochSecond
      log.warn("Blocked login attempt for [{}] — {} seconds remaining", identifier, remaining)
      throw UnauthorizedException("Too many failed attempts. Try again in ${remaining}s.")
    } else {
      // Block expired — reset
      attempts.remove(identifier)
    }
  }

  // Record a successful authentication — clear failure counter.
  fun recordSuccess(identifier: String) {
    attempts.remove(identifier)
  }

  // Record a failed attempt. Block after [maxAttempts] failures.
  fun recordFailure(identifier: String) {
    val record = attempts.getOrPut(identifier) { AttemptRecord() }
    val now = Instant.now()

    // Reset window if expired
    if (now.epochSecond - record.firstAttemptAt.epochSecond > windowSeconds) {
      record.count.set(0)
      record.firstAttemptAt = now
    }

    val newCount = record.count.incrementAndGet()
    log.warn("Failed auth attempt #{} for [{}]", newCount, identifier)

    if (newCount >= maxAttempts) {
      record.blockedUntil = now.plusSeconds(blockDurationSeconds)
      log.warn(
          "Blocking [{}] for {} seconds after {} failed attempts",
          identifier,
          blockDurationSeconds,
          newCount,
      )
    }
  }
}
