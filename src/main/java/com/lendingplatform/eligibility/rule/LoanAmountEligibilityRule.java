package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class LoanAmountEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        return application.getRequestedAmount().compareTo(lender.maximumLoanAmount()) <= 0;
    }

    @Override
    public String getFailureReason() {
        return "Requested amount exceeds lender's maximum loan amount";
    }
}
