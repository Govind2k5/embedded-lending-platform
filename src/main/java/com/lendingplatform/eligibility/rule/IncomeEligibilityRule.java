package com.lendingplatform.eligibility.rule;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.eligibility.EligibilityRule;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import org.springframework.stereotype.Component;

/** Rejects a lender if the borrower's monthly income is below that lender's minimum requirement. */
@Component // auto-discovered and added to EligibilityService's List<EligibilityRule> - no manual wiring
public class IncomeEligibilityRule implements EligibilityRule {

    @Override
    public boolean isEligible(Borrower borrower, LoanApplication application, LenderRules lender) {
        // BigDecimal.compareTo, not equals() - equals() would treat 40000.0
        // and 40000.00 as different values because it also compares scale;
        // compareTo compares numeric value only, which is what we want here.
        return borrower.getMonthlyIncome().compareTo(lender.minimumIncome()) >= 0;
    }

    @Override
    public String getFailureReason() {
        return "Monthly income below lender's minimum requirement";
    }
}
