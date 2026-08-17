package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class CreditScoreEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        return borrower.getCreditScore() >= lender.minimumCreditScore();
    }

    @Override
    public String getFailureReason() {
        return "Credit score below lender's minimum requirement";
    }
}
