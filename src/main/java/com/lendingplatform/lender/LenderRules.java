package com.lendingplatform.lender;

import java.math.BigDecimal;

/**
 * Slim, cacheable view of a lender's eligibility configuration.
 * This is what gets stored in Redis under "lender:{id}:rules".
 */
public record LenderRules(
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
    public static LenderRules from(Lender lender) {
        return new LenderRules(
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
