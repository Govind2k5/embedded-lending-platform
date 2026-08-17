package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class IncomeEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        return borrower.getMonthlyIncome().compareTo(lender.minimumIncome()) >= 0;
    }

    @Override
    public String getFailureReason() {
        return "Monthly income below lender's minimum requirement";
    }
}
