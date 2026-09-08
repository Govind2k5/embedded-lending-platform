package com.lendingplatform.loanapplication.dto;

import com.lendingplatform.loanapplication.LoanApplication;
import com.lendingplatform.loanapplication.LoanApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Outbound shape returned by every loan-application endpoint. This is also
 * exactly what gets cached (as the `responseBody` of an IdempotencyRecord)
 * when a client uses an Idempotency-Key on POST /loan-applications - see
 * IdempotencyService.
 */
public record LoanApplicationResponse(
        Long id,
        Long borrowerId,
        Long merchantId,
        BigDecimal requestedAmount,
        Integer requestedTenureMonths,
        LoanApplicationStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static LoanApplicationResponse from(LoanApplication application) {
        return new LoanApplicationResponse(
                application.getId(),
                application.getBorrowerId(),
                application.getMerchantId(),
                application.getRequestedAmount(),
                application.getRequestedTenureMonths(),
                application.getStatus(),
                application.getCreatedAt(),
                application.getUpdatedAt()
        );
    }
}
