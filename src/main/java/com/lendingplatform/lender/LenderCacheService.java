package com.lendingplatform.lender;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * Cache-aside wrapper around lender eligibility rules - the main reason
 * Redis exists in this project besides idempotency and rate limiting.
 *
 * PostgreSQL is always the source of truth. Redis just saves a repeated
 * round-trip to Postgres for data that's read on EVERY loan application's
 * eligibility check but changes rarely (an admin editing a lender's limits).
 * If Redis were wiped right now, every call below would simply become a
 * cache miss and fall through to Postgres correctly - just a little slower
 * until the cache warms back up.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LenderCacheService {

    private static final String KEY_PREFIX = "lender:";
    private static final String KEY_SUFFIX = ":rules";
    private static final Duration TTL = Duration.ofMinutes(10); // outer bound on staleness if an eviction were ever missed

    private final LenderRepository lenderRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Used by EligibilityService: get the rules for every currently-active
     * lender, going through the per-lender cache for each one.
     */
    public List<LenderRules> getActiveLenderRules() {
        List<Long> activeLenderIds = lenderRepository.findByActiveTrue().stream()
                .map(Lender::getId)
                .toList();
        return activeLenderIds.stream().map(this::getRules).toList();
    }

    /**
     * The actual cache-aside pattern for a single lender:
     * 1. try Redis first (GET lender:{id}:rules)
     * 2. HIT  -> return the cached value, no Postgres query at all
     * 3. MISS -> load from Postgres, build the LenderRules projection,
     *            write it back to Redis with a TTL, then return it
     */
    public LenderRules getRules(Long lenderId) {
        String key = cacheKey(lenderId);

        // We read as Object and convert via Jackson instead of casting
        // directly to LenderRules. GenericJackson2JsonRedisSerializer,
        // when built with a custom (injected) ObjectMapper, does not embed
        // the same "@class" type metadata its no-arg constructor would, so
        // a value read back from Redis can come back as a plain
        // LinkedHashMap rather than a LenderRules instance. A direct
        // `(LenderRules) redisTemplate.opsForValue().get(key)` cast blows
        // up with a ClassCastException in that case - objectMapper.convertValue
        // handles both shapes correctly.
        Object raw = redisTemplate.opsForValue().get(key);
        LenderRules cached = raw != null ? objectMapper.convertValue(raw, LenderRules.class) : null;
        if (cached != null) {
            log.debug("Lender rules cache hit for lender {}", lenderId);
            return cached;
        }

        log.debug("Lender rules cache miss for lender {}, loading from database", lenderId);
        Lender lender = lenderRepository.findById(lenderId)
                .orElseThrow(() -> new ResourceNotFoundException("Lender not found: " + lenderId));
        LenderRules rules = LenderRules.from(lender);
        redisTemplate.opsForValue().set(key, rules, TTL);
        return rules;
    }

    /** Called by LenderService.update() right after a Postgres write commits, so stale rules never linger. */
    public void evict(Long lenderId) {
        redisTemplate.delete(cacheKey(lenderId));
    }

    private String cacheKey(Long lenderId) {
        return KEY_PREFIX + lenderId + KEY_SUFFIX;
    }
}
