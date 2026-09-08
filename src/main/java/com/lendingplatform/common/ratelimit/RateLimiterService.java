package com.lendingplatform.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Fixed-window rate limiter backed by Redis INCR. Each caller gets a counter
 * key scoped to the current one-minute window; the key expires on its own so
 * nothing needs a cleanup job.
 *
 * Used by two independent limiters in this project (see RateLimitInterceptor
 * for the general per-IP one, and LoanApplicationController for the
 * per-borrower one on application creation) - both just call isAllowed()
 * with their own key/limit/window.
 *
 * Note: this is a plain StringRedisTemplate, deliberately separate from the
 * RedisTemplate<String,Object> used elsewhere (LenderCacheService,
 * IdempotencyService) for JSON caching. Redis's INCR command needs the
 * stored value to be a raw string of digits, not a JSON-wrapped object, so
 * a different (simpler) template is used here on purpose.
 */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Atomically increments the counter for `key` and reports whether it's
     * still within `limit`. The very first increment (count == 1) also sets
     * the key's expiry, so the counter resets itself once `window` elapses
     * without needing any background job.
     */
    public boolean isAllowed(String key, int limit, Duration window) {
        Long count = stringRedisTemplate.opsForValue().increment(key); // Redis INCR - atomic even under concurrent requests
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window);
        }
        return count != null && count <= limit;
    }

    /**
     * Builds a Redis key that changes every `windowSeconds` seconds, e.g.
     * "ratelimit:ip:1.2.3.4:29783197". Dividing the current epoch millis by
     * the window size (in millis) buckets time into fixed windows - this is
     * what makes the limiter "fixed-window" rather than a sliding log: two
     * requests 1 second apart can land in different windows (one at :59,
     * the next at :00) and both be allowed, which is the known tradeoff of
     * this simple approach vs. a sliding-window/token-bucket limiter.
     */
    public String windowedKey(String prefix, long windowSeconds) {
        long currentWindow = System.currentTimeMillis() / (windowSeconds * 1000);
        return prefix + ":" + currentWindow;
    }
}
