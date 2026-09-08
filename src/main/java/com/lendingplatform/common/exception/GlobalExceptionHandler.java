package com.lendingplatform.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * One centralized place that turns every exception type thrown anywhere in
 * the app into the same consistent ErrorResponse JSON shape + the right
 * HTTP status. @RestControllerAdvice makes Spring apply these
 * @ExceptionHandler methods across EVERY @RestController in the app - no
 * individual controller has its own try/catch blocks.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Missing borrower/lender/merchant/application/offer/loan/repayment id -> 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), request);
    }

    /** Wrong status for the requested operation (see InvalidStateException's own Javadoc for the full list) -> 409. */
    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidState(InvalidStateException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), request);
    }

    /** Same Idempotency-Key reused with a different request body -> 409. */
    @ExceptionHandler(DuplicateRequestException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateRequest(DuplicateRequestException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "DUPLICATE_REQUEST", ex.getMessage(), request);
    }

    /**
     * Thrown by Hibernate itself (not by our own code) when a JPA @Version
     * check fails - i.e. two requests tried to update the same row (a Loan,
     * in this project) and lost the race. See Loan.version and
     * RepaymentService.makeRepayment(). Mapped to 409 so the client knows
     * to reload the resource and retry, rather than silently overwriting.
     */
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "CONCURRENT_UPDATE",
                "This resource was updated concurrently, please retry", request);
    }

    /** Per-borrower loan-application creation limit exceeded (10/minute) -> 429. */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimit(RateLimitExceededException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMIT_EXCEEDED", ex.getMessage(), request);
    }

    /**
     * Bean Validation (@Valid) failure on a request DTO - thrown by Spring
     * itself before the controller method body ever runs. We flatten every
     * failing field into one comma-separated message so a client can see
     * everything wrong with the request in a single response, not one
     * field at a time.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", message, request);
    }

    /** Catch-all for bad input that isn't caught by Bean Validation annotations -> 400. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST", ex.getMessage(), request);
    }

    /**
     * Last-resort handler for anything unexpected (a NullPointerException,
     * a bug, a database connectivity blip). Deliberately never leaks the
     * real exception message or stack trace to the client - that's an
     * information-disclosure risk, not a debugging convenience for a
     * legitimate caller. The full exception (with stack trace) is logged
     * server-side via log.error so a developer can actually diagnose it.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong", request);
    }

    /** Shared helper so every handler above builds the exact same ErrorResponse shape. */
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String error, String message,
                                                          HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), error, message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
