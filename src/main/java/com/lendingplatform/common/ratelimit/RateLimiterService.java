package com.lendingplatform.common.ratelimit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Fixed-window rate limiter backed by Redis INCR. Each caller gets a counter
 * key scoped to the current one-minute window; the key expires on its own so
 * nothing needs a cleanup job.
 */
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final StringRedisTemplate stringRedisTemplate;

    public boolean isAllowed(String key, int limit, Duration window) {
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            stringRedisTemplate.expire(key, window);
        }
        return count != null && count <= limit;
    }

    public String windowedKey(String prefix, long windowSeconds) {
        long currentWindow = System.currentTimeMillis() / (windowSeconds * 1000);
        return prefix + ":" + currentWindow;
    }
}
