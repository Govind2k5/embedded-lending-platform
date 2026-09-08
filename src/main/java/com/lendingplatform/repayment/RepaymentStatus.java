package com.lendingplatform.repayment;

/**
 * PENDING -- every installment starts here when generateSchedule() creates it.
 * PARTIAL -- some, but not all, of the installment's total_amount has been paid.
 * PAID    -- fully paid (RepaymentService.makeRepayment sets this once paidAmount >= totalAmount).
 * LATE    -- ** never actually persisted anywhere **. RepaymentResponse.from()
 *            computes this on every read, purely for display: if the stored
 *            status is still PENDING or PARTIAL and the due date has
 *            already passed, the DTO reports LATE even though the database
 *            row still literally says PENDING/PARTIAL. Persisting a real
 *            LATE status would need a scheduled job sweeping overdue
 *            installments - infrastructure this project doesn't otherwise
 *            need, so it's computed instead. The tradeoff: a direct SQL
 *            query for "all late repayments" wouldn't find any today.
 */
public enum RepaymentStatus {
    PENDING,
    PAID,
    PARTIAL,
    LATE
}
