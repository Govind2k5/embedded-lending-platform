package com.lendingplatform.loanapplication.dto;

import com.lendingplatform.loanapplication.LoanApplication;
import com.lendingplatform.loanapplication.LoanApplicationStatus;

import java.math.BigDecimal;
import java.time.Instant;

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
