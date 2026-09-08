package com.lendingplatform.loanapplication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/** Inbound shape for POST /api/v1/loan-applications - what a merchant app submits on behalf of a borrower. */
public record LoanApplicationRequest(
        @NotNull Long borrowerId,
        @NotNull Long merchantId,
        @NotNull @Positive BigDecimal requestedAmount,       // must be > 0 - a zero or negative loan makes no sense
        @NotNull @Positive Integer requestedTenureMonths     // must be > 0 months
) {
}
