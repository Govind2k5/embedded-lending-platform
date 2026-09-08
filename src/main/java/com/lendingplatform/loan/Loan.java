package com.lendingplatform.loan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity mapped to "loans" - created exactly once, by
 * LoanService.createLoanFromOffer(), from a SELECTED LoanOffer. Carries
 * copies of applicationId/offerId/borrowerId/lenderId (all plain FK
 * columns, not JPA relationships) so a Loan can always be traced back to
 * the application and offer it came from without a join.
 */
@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "offer_id", nullable = false)
    private Long offerId;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "lender_id", nullable = false)
    private Long lenderId;

    @Column(name = "principal_amount", nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    // Copied from the offer at creation time, NOT recalculated here - see
    // LoanService.createLoanFromOffer(). Safe because the approved
    // amount/tenure/rate always match the offer's by the time a loan is
    // created, but worth knowing it's a copy, not a fresh computation.
    @Column(name = "monthly_emi", nullable = false)
    private BigDecimal monthlyEmi;

    // The one mutable financial figure on this entity - decremented by
    // RepaymentService.makeRepayment() every time an installment is paid,
    // clamped to zero, and used to flip status to COMPLETED.
    @Column(name = "outstanding_amount", nullable = false)
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    /**
     * JPA optimistic-locking column (mapped to the "version" BIGINT column
     * in the schema). Every UPDATE Hibernate issues for this entity
     * automatically includes "WHERE version = <the value it read>" and
     * bumps it by 1 on success. If two requests (e.g. two concurrent
     * repayments on the same loan) both read version=3, whichever commits
     * first wins and moves it to 4; the second one's UPDATE matches zero
     * rows, and Hibernate throws ObjectOptimisticLockingFailureException -
     * mapped by GlobalExceptionHandler to 409 CONCURRENT_UPDATE. This is
     * what makes concurrent balance updates on the same loan safe without
     * an explicit database or distributed lock.
     */
    @Version
    private Long version;
}
