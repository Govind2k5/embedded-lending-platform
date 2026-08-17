package com.lendingplatform.lender.dto;

import com.lendingplatform.lender.LenderType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record LenderRequest(
        @NotBlank String name,
        @NotNull LenderType type,
        @NotNull @PositiveOrZero BigDecimal minimumIncome,
        @NotNull @Min(300) @Max(900) Integer minimumCreditScore,
        @NotNull @Positive BigDecimal maximumLoanAmount,
        @NotNull @Positive Integer minimumTenureMonths,
        @NotNull @Positive Integer maximumTenureMonths,
        @NotNull @Positive BigDecimal baseInterestRate,
        boolean active
) {
}
