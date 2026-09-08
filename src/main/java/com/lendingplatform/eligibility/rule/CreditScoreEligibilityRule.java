package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

/** Rejects a lender if the borrower's credit score is below that lender's minimum requirement. */
@Component
public class CreditScoreEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        // Plain int comparison is fine here - unlike money, a credit score
        // has no fractional/scale concerns, so no BigDecimal needed.
        return borrower.getCreditScore() >= lender.minimumCreditScore();
    }

    @Override
    public String getFailureReason() {
        return "Credit score below lender's minimum requirement";
    }
}
