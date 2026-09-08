package com.lendingplatform.repayment.dto;

import com.lendingplatform.repayment.Repayment;
import com.lendingplatform.repayment.RepaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Outbound shape for the repayment endpoints. from() is where the LATE
 * status is computed - see RepaymentStatus's Javadoc for why LATE only
 * ever exists here and never in the database itself.
 */
public record RepaymentResponse(
        Long id,
        Long loanId,
        Integer installmentNumber,
        LocalDate dueDate,
        BigDecimal principalAmount,
        BigDecimal interestAmount,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        RepaymentStatus status,
        Instant paidAt
) {
    public static RepaymentResponse from(Repayment repayment) {
        RepaymentStatus displayStatus = repayment.getStatus();
        // Derive LATE at read time: still owing money (PENDING/PARTIAL) and
        // the due date has already passed. This never touches the database
        // row - it's purely what gets shown to the caller.
        boolean isOverdue = repayment.getDueDate().isBefore(LocalDate.now());
        if (isOverdue && (displayStatus == RepaymentStatus.PENDING || displayStatus == RepaymentStatus.PARTIAL)) {
            displayStatus = RepaymentStatus.LATE;
        }

        return new RepaymentResponse(
                repayment.getId(),
                repayment.getLoanId(),
                repayment.getInstallmentNumber(),
                repayment.getDueDate(),
                repayment.getPrincipalAmount(),
                repayment.getInterestAmount(),
                repayment.getTotalAmount(),
                repayment.getPaidAmount(),
                displayStatus,
                repayment.getPaidAt()
        );
    }
}
