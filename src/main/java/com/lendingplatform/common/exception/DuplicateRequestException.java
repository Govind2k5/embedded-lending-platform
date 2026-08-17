package com.lendingplatform.common.exception;

/**
 * Thrown when an idempotency key is reused with a different request body
 * than the one it was originally recorded against.
 */
public class DuplicateRequestException extends RuntimeException {

    public DuplicateRequestException(String message) {
        super(message);
    }
}
