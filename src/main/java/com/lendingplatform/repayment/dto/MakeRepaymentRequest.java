package com.lendingplatform.repayment.dto;

import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * Request body for POST /loans/{id}/repayments/{repaymentId}.
 * amount is optional - when omitted (or the whole request body is omitted -
 * see LoanController.makeRepayment, which accepts a null body), the full
 * remaining due amount for the installment is simulated as paid.
 */
public record MakeRepaymentRequest(
        @Positive BigDecimal amount
) {
}
