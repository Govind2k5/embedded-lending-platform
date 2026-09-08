package com.lendingplatform.repayment;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * JPA entity mapped to "repayments" - one row per EMI installment on a
 * Loan, all created at once by RepaymentService.generateSchedule() right
 * after the Loan itself is created. The DB schema has a
 * UNIQUE(loan_id, installment_number) constraint so this table can never
 * end up with two "installment #3"s for the same loan.
 *
 * Note there is no "remaining balance after this installment" column here -
 * that's computed transiently while building the schedule (see
 * AmortizationEntry.remainingBalance) but never persisted per-row; only
 * Loan.outstandingAmount tracks balance, at the loan level.
 */
@Entity
@Table(name = "repayments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Repayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_amount", nullable = false)
    private BigDecimal interestAmount;

    // principalAmount + interestAmount - the flat EMI for every installment
    // except the last, which absorbs rounding drift (see EmiCalculatorService).
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;

    // Starts at 0, incremented by RepaymentService.makeRepayment() - can be
    // less than totalAmount (a PARTIAL payment) or equal to it (PAID).
    @Column(name = "paid_amount", nullable = false)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RepaymentStatus status;

    // Only set once the installment is fully PAID - null for PENDING/PARTIAL rows.
    @Column(name = "paid_at")
    private Instant paidAt;
}
