package com.lendingplatform.lender.dto;

import com.lendingplatform.lender.Lender;
import com.lendingplatform.lender.LenderType;

import java.math.BigDecimal;

/** Outbound shape for the Lender endpoints - maps straight from the JPA entity, never from the cached LenderRules. */
public record LenderResponse(
        Long id,
        String name,
        LenderType type,
        BigDecimal minimumIncome,
        Integer minimumCreditScore,
        BigDecimal maximumLoanAmount,
        Integer minimumTenureMonths,
        Integer maximumTenureMonths,
        BigDecimal baseInterestRate,
        boolean active
) {
    public static LenderResponse from(Lender lender) {
        return new LenderResponse(
                lender.getId(),
                lender.getName(),
                lender.getType(),
                lender.getMinimumIncome(),
                lender.getMinimumCreditScore(),
                lender.getMaximumLoanAmount(),
                lender.getMinimumTenureMonths(),
                lender.getMaximumTenureMonths(),
                lender.getBaseInterestRate(),
                lender.isActive()
        );
    }
}
