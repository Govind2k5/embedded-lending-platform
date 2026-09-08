package com.lendingplatform.eligibility;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.borrower.EmploymentType;
import com.lendingplatform.eligibility.rule.CreditScoreEligibilityRule;
import com.lendingplatform.eligibility.rule.IncomeEligibilityRule;
import com.lendingplatform.eligibility.rule.LoanAmountEligibilityRule;
import com.lendingplatform.eligibility.rule.TenureEligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.lender.LenderType;
import com.lendingplatform.loanapplication.LoanApplication;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the 4 individual EligibilityRule implementations, tested
 * in isolation from EligibilityService and from Spring entirely - each
 * rule is a one-line pure function, so each gets a one-line-assertion test.
 * Fixtures are built by hand rather than loaded from a database, since
 * these rules don't touch persistence at all.
 */
class EligibilityRulesTest {

    // A "clearly eligible" baseline borrower - each test below only tweaks
    // the one field relevant to the rule under test.
    private final Borrower borrower = borrowerWith(BigDecimal.valueOf(50000), 720);

    private static Borrower borrowerWith(BigDecimal monthlyIncome, int creditScore) {
        return Borrower.builder()
                .id(1L)
                .name("Test Borrower")
                .email("test@example.com")
                .phone("9800000000")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .monthlyIncome(monthlyIncome)
                .employmentType(EmploymentType.SALARIED)
                .creditScore(creditScore)
                .build();
    }

    // A representative lender (matches BankOne's seeded rules).
    private final LenderRules lenderRules = new LenderRules(
            1L, "BankOne", LenderType.BANK,
            BigDecimal.valueOf(40000), 700, BigDecimal.valueOf(500000), 6, 60, BigDecimal.valueOf(11.5), true);

    private LoanApplication applicationFor(BigDecimal amount, int tenureMonths) {
        return LoanApplication.builder()
                .id(1L)
                .borrowerId(1L)
                .merchantId(1L)
                .requestedAmount(amount)
                .requestedTenureMonths(tenureMonths)
                .build();
    }

    @Test
    void incomeRulePassesWhenIncomeMeetsMinimum() {
        assertThat(new IncomeEligibilityRule().isEligible(borrower, applicationFor(BigDecimal.valueOf(100000), 12), lenderRules))
                .isTrue();
    }

    @Test
    void incomeRuleFailsWhenIncomeBelowMinimum() {
        Borrower lowIncomeBorrower = borrowerWith(BigDecimal.valueOf(20000), 720);
        assertThat(new IncomeEligibilityRule().isEligible(lowIncomeBorrower, applicationFor(BigDecimal.valueOf(100000), 12), lenderRules))
                .isFalse();
    }

    @Test
    void creditScoreRuleFailsWhenBelowMinimum() {
        Borrower lowScoreBorrower = borrowerWith(BigDecimal.valueOf(50000), 650);
        assertThat(new CreditScoreEligibilityRule().isEligible(lowScoreBorrower, applicationFor(BigDecimal.valueOf(100000), 12), lenderRules))
                .isFalse();
    }

    @Test
    void loanAmountRuleFailsWhenRequestedAmountExceedsMax() {
        // 600000 > lender's maximumLoanAmount of 500000
        assertThat(new LoanAmountEligibilityRule().isEligible(borrower, applicationFor(BigDecimal.valueOf(600000), 12), lenderRules))
                .isFalse();
    }

    @Test
    void tenureRuleFailsWhenOutsideLenderRange() {
        // 90 months > lender's max of 60; 3 months < lender's min of 6 - both ends of the range are checked.
        assertThat(new TenureEligibilityRule().isEligible(borrower, applicationFor(BigDecimal.valueOf(100000), 90), lenderRules))
                .isFalse();
        assertThat(new TenureEligibilityRule().isEligible(borrower, applicationFor(BigDecimal.valueOf(100000), 3), lenderRules))
                .isFalse();
    }

    @Test
    void tenureRulePassesWhenWithinLenderRange() {
        assertThat(new TenureEligibilityRule().isEligible(borrower, applicationFor(BigDecimal.valueOf(100000), 24), lenderRules))
                .isTrue();
    }
}
