package com.lendingplatform.common.exception;

/**
 * Thrown whenever an operation is attempted on a resource that isn't in the
 * right status/state for it - mapped to HTTP 409 CONFLICT by
 * GlobalExceptionHandler. This is the single exception type behind most of
 * the workflow's guard clauses:
 *   - checking eligibility on an application that isn't CREATED
 *   - selecting an offer that isn't AVAILABLE, or doesn't belong to the application
 *   - approving an application that isn't OFFER_SELECTED
 *   - paying more than what's left due on a repayment installment
 * Using one exception type for all of these (rather than one per case) keeps
 * the state-machine guard logic simple to read in each service method.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
