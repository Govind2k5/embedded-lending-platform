package com.lendingplatform.common.exception;

/**
 * Thrown by LoanApplicationController.enforceBorrowerCreationLimit() when a
 * single borrower creates more than 10 loan applications within a 60-second
 * window - mapped to HTTP 429 TOO_MANY_REQUESTS by GlobalExceptionHandler.
 *
 * Note this is the ENDPOINT-SPECIFIC limiter only. The general per-IP limit
 * (60 requests/minute on every /api/v1/** call) is enforced earlier, inside
 * RateLimitInterceptor.preHandle(), which writes its own 429 response
 * directly and never even reaches a controller - so it never throws this
 * exception or goes through this handler.
 */
public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
