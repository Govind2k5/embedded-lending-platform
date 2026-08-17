package com.lendingplatform.loanapplication.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record LoanApplicationRequest(
        @NotNull Long borrowerId,
        @NotNull Long merchantId,
        @NotNull @Positive BigDecimal requestedAmount,
        @NotNull @Positive Integer requestedTenureMonths
) {
}
