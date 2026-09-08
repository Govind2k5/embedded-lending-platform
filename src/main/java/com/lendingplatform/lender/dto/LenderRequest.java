package com.lendingplatform.lender.dto;

import com.lendingplatform.lender.LenderType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Inbound shape for POST/PUT /api/v1/lenders. Used for both creation and
 * full updates (LenderController.update requires every field, there is no
 * separate "patch" DTO with optional fields).
 */
public record LenderRequest(
        @NotBlank String name,
        @NotNull LenderType type,
        @NotNull @PositiveOrZero BigDecimal minimumIncome,
        @NotNull @Min(300) @Max(900) Integer minimumCreditScore, // same 300-900 range as Borrower.creditScore
        @NotNull @Positive BigDecimal maximumLoanAmount,
        @NotNull @Positive Integer minimumTenureMonths,
        @NotNull @Positive Integer maximumTenureMonths,
        @NotNull @Positive BigDecimal baseInterestRate, // annual percentage, e.g. 11.5 means 11.5% p.a.
        boolean active // no validation needed - a primitive boolean can't be null
) {
}
