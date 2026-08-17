package com.lendingplatform.repayment.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * amount is optional - when omitted, the full remaining due amount for the
 * installment is simulated as paid.
 */
public record MakeRepaymentRequest(
        @Positive BigDecimal amount
) {
}
