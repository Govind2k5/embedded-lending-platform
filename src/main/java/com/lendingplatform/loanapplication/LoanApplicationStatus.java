package com.lendingplatform.loanapplication;

/**
 * The full state machine for a LoanApplication, driven entirely by
 * LoanApplicationService's guard clauses (each transition method checks the
 * current status before doing anything, and throws InvalidStateException
 * if it's wrong).
 *
 * The states this project's code actually reaches, in order:
 *   CREATED           -- set on insert (LoanApplicationService.create)
 *     -> OFFERS_AVAILABLE  if checkEligibility() finds >=1 matching lender
 *     -> REJECTED          if checkEligibility() finds zero matching lenders (a valid terminal outcome, not an error)
 *   OFFERS_AVAILABLE  -> OFFER_SELECTED   once selectOffer() succeeds
 *   OFFER_SELECTED    -> APPROVED         once approve() succeeds (this is also when the Loan row is created)
 *
 * ELIGIBILITY_CHECKED, CANCELLED and EXPIRED are intentionally defined but
 * UNREACHABLE by any current code path - they were modeled for
 * functionality this project doesn't implement yet (e.g. splitting
 * "run the rules" from "generate offers" into two separate steps, or an
 * application-expiry background job). Worth knowing this distinction if
 * asked about it - it's a documented gap, not an oversight.
 */
public enum LoanApplicationStatus {
    CREATED,
    ELIGIBILITY_CHECKED, // unreachable today - see class Javadoc
    OFFERS_AVAILABLE,
    OFFER_SELECTED,
    APPROVED,
    REJECTED,
    CANCELLED,           // unreachable today - see class Javadoc
    EXPIRED              // unreachable today - see class Javadoc
}
