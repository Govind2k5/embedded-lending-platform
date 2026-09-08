package com.lendingplatform.lender;

import java.math.BigDecimal;

/**
 * Slim, cacheable projection of a Lender's eligibility configuration - this
 * is the exact object stored as JSON in Redis under "lender:{id}:rules" (see
 * LenderCacheService) and the type every EligibilityRule implementation is
 * written against, instead of the full JPA Lender entity.
 *
 * Why a separate record instead of just caching the Lender entity? Two
 * reasons: (1) it's a plain, dependency-free DTO that serializes/deserializes
 * predictably with Jackson - no lazy-loading proxies, no JPA metadata to
 * confuse the cache; (2) it only carries the fields eligibility rules and
 * offer generation actually need, keeping the cached payload small.
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
    /** Maps a freshly-loaded Postgres entity into the cacheable shape, on a cache miss. */
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
