package com.lendingplatform.repayment.dto;

import com.lendingplatform.repayment.Repayment;
import com.lendingplatform.repayment.RepaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

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
