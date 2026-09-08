package com.lendingplatform.lender;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * JPA entity mapped to the "lenders" table. Each row is one fictional
 * bank/NBFC (see V2__seed_data.sql for BankOne / QuickCredit NBFC /
 * PrimeBank) with the eligibility rules that gate which borrowers it will
 * lend to. This is the PostgreSQL source-of-truth record; the eligibility
 * engine and offer generation never read this entity directly in the hot
 * path - they read the cached LenderRules projection instead (see
 * LenderCacheService) so a frequently-read, rarely-changed table doesn't
 * hit Postgres on every single loan application.
 */
@Entity
@Table(name = "lenders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // BANK or NBFC - purely descriptive today, no rule branches on it.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LenderType type;

    // --- everything below is read by the 4 EligibilityRule implementations ---
    @Column(name = "minimum_income", nullable = false)
    private BigDecimal minimumIncome;

    @Column(name = "minimum_credit_score", nullable = false)
    private Integer minimumCreditScore;

    @Column(name = "maximum_loan_amount", nullable = false)
    private BigDecimal maximumLoanAmount;

    @Column(name = "minimum_tenure_months", nullable = false)
    private Integer minimumTenureMonths;

    @Column(name = "maximum_tenure_months", nullable = false)
    private Integer maximumTenureMonths;

    // Flat annual rate applied to every approved offer from this lender -
    // no risk-based pricing per borrower in this project.
    @Column(name = "base_interest_rate", nullable = false)
    private BigDecimal baseInterestRate;

    // Inactive lenders are skipped entirely by LenderRepository.findByActiveTrue()
    // during eligibility checks - no rule even runs against them.
    @Column(nullable = false)
    private boolean active;
}
