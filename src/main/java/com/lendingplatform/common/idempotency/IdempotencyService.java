package com.lendingplatform.common.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lendingplatform.common.exception.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.function.Supplier;

/**
 * Makes a write endpoint safe to retry. A client sends an Idempotency-Key
 * header; the first request executes normally and its response is cached in
 * Redis. Any repeat with the same key returns the cached response instead of
 * repeating the side effect. If the same key shows up with a different
 * request body, that's treated as a client error rather than silently
 * reusing the old response.
 *
 * Currently wired up on POST /api/v1/loan-applications only (see
 * LoanApplicationController.create) - the mechanism here is generic enough
 * that approve/repayment could reuse it identically if needed later.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24); // long enough to cover realistic client retry windows, short enough not to grow Redis forever

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Wraps a controller action with idempotency semantics.
     *
     * @param idempotencyKey the client-supplied Idempotency-Key header value
     * @param requestBody    the parsed request DTO, hashed to detect key reuse with a different body
     * @param successStatus  the HTTP status to use/replay on success (e.g. 201 CREATED)
     * @param responseType   the DTO class to deserialize a cached response back into
     * @param action         the real work to run on a first-time request (only invoked on a cache miss)
     */
    public <T> ResponseEntity<T> execute(String idempotencyKey, Object requestBody, HttpStatus successStatus,
                                          Class<T> responseType, Supplier<T> action) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        String requestHash = hash(requestBody);

        // Read as Object, not directly as IdempotencyRecord: GenericJackson2JsonRedisSerializer
        // built with a custom ObjectMapper doesn't preserve enough type
        // metadata for a raw cast to always work, so we deserialize
        // defensively via convertValue instead (see also LenderCacheService,
        // which hit the exact same issue).
        Object cached = redisTemplate.opsForValue().get(redisKey);
        IdempotencyRecord existing = cached != null ? objectMapper.convertValue(cached, IdempotencyRecord.class) : null;

        if (existing != null) {
            if (!existing.requestHash().equals(requestHash)) {
                // Same key, different body - refuse to guess which one the client "really" meant.
                throw new DuplicateRequestException(
                        "Idempotency-Key '" + idempotencyKey + "' was already used with a different request body");
            }
            // Same key, same body: this is a genuine retry (e.g. the client's
            // first response timed out on the network even though the server
            // completed successfully). Replay the original response instead
            // of running create() again and making a second application.
            log.info("Replaying cached response for idempotency key {}", idempotencyKey);
            T cachedBody = objectMapper.convertValue(existing.responseBody(), responseType);
            return ResponseEntity.status(existing.statusCode()).body(cachedBody);
        }

        // First time we've seen this key: do the real work, then remember
        // the result so a retry can be replayed instead of repeated.
        T result = action.get();
        redisTemplate.opsForValue().set(redisKey, new IdempotencyRecord(requestHash, result, successStatus.value()), TTL);
        return ResponseEntity.status(successStatus).body(result);
    }

    /** SHA-256 over the request body's JSON representation - a compact, order-independent fingerprint of "what was asked for". */
    private String hash(Object requestBody) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(requestBody);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(json);
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to hash request body for idempotency check", e);
        }
    }
}
