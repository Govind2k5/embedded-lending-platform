package com.lendingplatform.common.emi;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One row of a full repayment schedule, as computed in-memory by
 * EmiCalculatorService.generateSchedule() - NOT a database entity. Each
 * entry gets mapped into a persisted Repayment row by
 * RepaymentService.generateSchedule(), except for remainingBalance, which
 * is computed here for the amortization math but is never actually stored
 * on the Repayment row - only Loan.outstandingAmount tracks balance at the
 * loan level (see RepaymentService for why).
 */
public record AmortizationEntry(
        int installmentNumber,       // 1-based: 1, 2, 3, ... tenureMonths
        LocalDate dueDate,           // startDate + installmentNumber months
        BigDecimal principalComponent, // portion of this EMI that reduces the outstanding balance
        BigDecimal interestComponent,  // portion of this EMI that is interest on the current balance
        BigDecimal emiAmount,          // principalComponent + interestComponent (equal to the flat EMI, except the last installment)
        BigDecimal remainingBalance    // balance left after this installment - should be exactly 0 on the final entry
) {
}
