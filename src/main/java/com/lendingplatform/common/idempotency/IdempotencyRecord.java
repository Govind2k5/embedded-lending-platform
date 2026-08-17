package com.lendingplatform.common.idempotency;

public record IdempotencyRecord(
        String requestHash,
        Object responseBody,
        int statusCode
) {
}
