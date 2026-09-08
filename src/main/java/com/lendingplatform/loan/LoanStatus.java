package com.lendingplatform.loan;

/**
 * Only two of these are actually reachable by current code:
 *   ACTIVE    -- set when LoanService.createLoanFromOffer() creates the loan
 *   COMPLETED -- set by RepaymentService.makeRepayment() once outstandingAmount hits zero
 *
 * DEFAULTED and CANCELLED are modeled for functionality this project
 * doesn't implement (e.g. a background job flagging loans with missed
 * payments past a grace period) - defined for completeness, not currently
 * reachable from any endpoint.
 */
public enum LoanStatus {
    ACTIVE,
    COMPLETED,
    DEFAULTED,   // unreachable today - see class Javadoc
    CANCELLED    // unreachable today - see class Javadoc
}
