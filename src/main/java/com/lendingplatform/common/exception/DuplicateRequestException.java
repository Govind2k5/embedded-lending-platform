package com.lendingplatform.common.exception;

/**
 * Thrown by IdempotencyService.execute() when a client reuses an
 * Idempotency-Key header value but sends a DIFFERENT request body than the
 * one originally recorded for that key. Mapped to HTTP 409 CONFLICT.
 *
 * This is deliberately NOT the same thing as "same key, same body" (which
 * is a normal, successful retry that just replays the cached response) -
 * this exception only fires when the key is being reused incorrectly,
 * which is treated as a client bug rather than something safe to guess at.
 */
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(String message) {
        super(message);
    }
}
