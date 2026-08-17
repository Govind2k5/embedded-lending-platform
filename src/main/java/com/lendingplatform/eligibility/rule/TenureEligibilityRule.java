package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

@Component
public class TenureEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        int requestedTenure = application.getRequestedTenureMonths();
        return requestedTenure >= lender.minimumTenureMonths() && requestedTenure <= lender.maximumTenureMonths();
    }

    @Override
    public String getFailureReason() {
        return "Requested tenure is outside lender's supported tenure range";
    }
}
