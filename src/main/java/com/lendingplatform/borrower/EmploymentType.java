package com.lendingplatform.borrower;

/**
 * The two employment categories the seed data and eligibility rules
 * recognize. Stored as a STRING column (see Borrower.employmentType), not
 * used by any eligibility rule directly today - income and credit score are
 * what actually gate eligibility - but kept as structured data because a
 * real underwriting system would price self-employed borrowers differently.
 */
public enum EmploymentType {
    SALARIED,
    SELF_EMPLOYED
}
