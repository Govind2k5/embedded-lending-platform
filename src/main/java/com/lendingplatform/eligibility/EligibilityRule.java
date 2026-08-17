package com.lendingplatform.eligibility;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;

/**
 * A single eligibility check a lender applies to a borrower's application.
 * Implementations should each check exactly one thing.
 */
public interface EligibilityRule {

    boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender);

    String getFailureReason();
}
