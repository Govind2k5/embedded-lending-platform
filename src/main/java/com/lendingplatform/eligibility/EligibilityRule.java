package com.lendingplatform.eligibility;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;

/**
 * A single eligibility check a lender applies to a borrower's application.
 * Implementations should each check exactly one thing.
 *
 * Every class implementing this interface is annotated @Component (see the
 * four implementations in the .rule sub-package), so Spring automatically
 * collects ALL of them into the List<EligibilityRule> that
 * EligibilityService is constructor-injected with - no manual registry, no
 * factory class, no annotation-based discovery. Adding a fifth rule later
 * is just adding a fifth @Component class; nothing else needs to change.
 *
 * This is a deliberately simple alternative to a full rules engine (like
 * Drools): a rules engine buys dynamic, no-redeploy rule authoring, which
 * isn't needed here since these are fixed business rules, not end-user
 * configuration.
 */
public interface EligibilityRule {

    /** Returns true if `borrower`'s `application` satisfies this one rule against `lender`'s limits. */
    boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender);

    /** Human-readable reason shown in debug logs (see EligibilityService) when this rule rejects a lender. */
    String getFailureReason();
}
