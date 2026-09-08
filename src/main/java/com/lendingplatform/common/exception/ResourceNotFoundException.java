package com.lendingplatform.common.exception;

/**
 * Thrown by every *Service.getById()-style lookup across the codebase
 * (BorrowerService, LenderService, MerchantService, LoanApplicationService,
 * LoanOfferService, LoanService, RepaymentService) when the requested id
 * doesn't exist. Mapped to HTTP 404 NOT_FOUND by GlobalExceptionHandler.
 * Using one shared exception type means every "not found" case across every
 * package produces the exact same error shape without each service needing
 * its own custom exception class.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
