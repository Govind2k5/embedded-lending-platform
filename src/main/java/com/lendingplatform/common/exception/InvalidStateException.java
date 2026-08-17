package com.lendingplatform.common.exception;

/**
 * Thrown when an operation is attempted on a resource that is not in a valid
 * state for it, e.g. approving an application that has no selected offer.
 */
public class InvalidStateException extends RuntimeException {

    public InvalidStateException(String message) {
        super(message);
    }
}
