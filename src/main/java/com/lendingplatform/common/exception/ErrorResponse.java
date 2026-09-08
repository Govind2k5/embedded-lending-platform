package com.lendingplatform.common.exception;

import java.time.Instant;

/**
 * The single, consistent JSON shape every error response returns from this
 * API, regardless of which exception caused it - built by
 * GlobalExceptionHandler.buildResponse(). A client only ever needs to know
 * this one shape to parse any 4xx/5xx from this service.
 *
 * Example body:
 * {
 *   "timestamp": "2026-01-01T12:00:00Z",
 *   "status": 404,
 *   "error": "NOT_FOUND",
 *   "message": "Borrower not found: 123",
 *   "path": "/api/v1/borrowers/123"
 * }
 */
public record ErrorResponse(
        Instant timestamp,
        int status,       // numeric HTTP status, e.g. 404
        String error,     // short machine-readable code, e.g. "NOT_FOUND" - stable for client-side switch statements
        String message,   // human-readable detail - for validation errors, lists every failing field
        String path        // the request URI that failed, useful when a client logs many calls at once
) {
}
