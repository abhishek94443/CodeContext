package com.codecontext.server

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Thread-safe rate limiter using sliding window algorithm. Tracks requests per client (identified
 * by API key or IP) and enforces limits.
 */
class RateLimiter(
        private val maxRequestsPerMinute: Int = 60,
        private val maxRequestsPerHour: Int = 1000
) {
    private val minuteCounters = ConcurrentHashMap<String, TimedCounter>()
    private val hourCounters = ConcurrentHashMap<String, TimedCounter>()

    data class TimedCounter(
            val counter: AtomicInteger = AtomicInteger(0),
            val startTime: Long = System.currentTimeMillis()
    )

    /**
     * Check if a client has exceeded their rate limit.
     * @param clientId Unique identifier for the client (API key or IP address)
     * @return true if request is allowed, false if rate limit exceeded
     */
    fun checkLimit(clientId: String): Boolean {
        val now = System.currentTimeMillis()

        // Check minute limit
        val minuteKey = "$clientId:${now / 60_000}"
        val minuteCounter = minuteCounters.computeIfAbsent(minuteKey) { TimedCounter() }

        if (minuteCounter.counter.incrementAndGet() > maxRequestsPerMinute) {
            return false
        }

        // Check hour limit
        val hourKey = "$clientId:${now / 3_600_000}"
        val hourCounter = hourCounters.computeIfAbsent(hourKey) { TimedCounter() }

        if (hourCounter.counter.incrementAndGet() > maxRequestsPerHour) {
            return false
        }

        // Cleanup old entries periodically (1% chance)
        if (Math.random() < 0.01) {
            cleanup()
        }

        return true
    }

    /** Get remaining requests for a client in the current minute. */
    fun getRemainingMinute(clientId: String): Int {
        val now = System.currentTimeMillis()
        val minuteKey = "$clientId:${now / 60_000}"
        val counter = minuteCounters[minuteKey]
        return if (counter != null) {
            maxOf(0, maxRequestsPerMinute - counter.counter.get())
        } else {
            maxRequestsPerMinute
        }
    }

    /** Get remaining requests for a client in the current hour. */
    fun getRemainingHour(clientId: String): Int {
        val now = System.currentTimeMillis()
        val hourKey = "$clientId:${now / 3_600_000}"
        val counter = hourCounters[hourKey]
        return if (counter != null) {
            maxOf(0, maxRequestsPerHour - counter.counter.get())
        } else {
            maxRequestsPerHour
        }
    }

    /** Get seconds until the rate limit resets. */
    fun getSecondsUntilReset(clientId: String): Long {
        val now = System.currentTimeMillis()
        val minuteKey = "$clientId:${now / 60_000}"
        val counter = minuteCounters[minuteKey]

        return if (counter != null && counter.counter.get() > maxRequestsPerMinute) {
            // Return seconds until next minute
            60 - ((now / 1000) % 60)
        } else {
            0
        }
    }

    /** Remove expired entries to prevent memory leaks. */
    private fun cleanup() {
        val now = System.currentTimeMillis()

        // Remove minute counters older than 1 minute
        minuteCounters.entries.removeIf { (_, counter) -> now - counter.startTime > 60_000 }

        // Remove hour counters older than 1 hour
        hourCounters.entries.removeIf { (_, counter) -> now - counter.startTime > 3_600_000 }
    }

    /** Clear all rate limit data (useful for testing). */
    fun clear() {
        minuteCounters.clear()
        hourCounters.clear()
    }
}
