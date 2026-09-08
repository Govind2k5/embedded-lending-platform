package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

/**
 * Rejects a lender if the requested loan amount exceeds that lender's
 * maximum. This is what guarantees, further downstream, that
 * LoanOfferService can safely copy the requested amount straight onto the
 * offer as the "approved" amount - by the time an offer is generated, this
 * rule has already confirmed the amount fits.
 */
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
