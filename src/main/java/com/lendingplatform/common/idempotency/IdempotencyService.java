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
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyService {

    private static final String KEY_PREFIX = "idempotency:";
    private static final Duration TTL = Duration.ofHours(24);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> ResponseEntity<T> execute(String idempotencyKey, Object requestBody, HttpStatus successStatus,
                                          Class<T> responseType, Supplier<T> action) {
        String redisKey = KEY_PREFIX + idempotencyKey;
        String requestHash = hash(requestBody);

        Object cached = redisTemplate.opsForValue().get(redisKey);
        IdempotencyRecord existing = cached != null ? objectMapper.convertValue(cached, IdempotencyRecord.class) : null;
        if (existing != null) {
            if (!existing.requestHash().equals(requestHash)) {
                throw new DuplicateRequestException(
                        "Idempotency-Key '" + idempotencyKey + "' was already used with a different request body");
            }
            log.info("Replaying cached response for idempotency key {}", idempotencyKey);
            T cachedBody = objectMapper.convertValue(existing.responseBody(), responseType);
            return ResponseEntity.status(existing.statusCode()).body(cachedBody);
        }

        T result = action.get();
        redisTemplate.opsForValue().set(redisKey, new IdempotencyRecord(requestHash, result, successStatus.value()), TTL);
        return ResponseEntity.status(successStatus).body(result);
    }

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
