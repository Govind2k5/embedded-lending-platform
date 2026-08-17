package com.lendingplatform.loan;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @Column(name = "monthly_emi", nullable = false)
    private BigDecimal monthlyEmi;

    @Column(name = "outstanding_amount", nullable = false)
    private BigDecimal outstandingAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;

    @Version
    private Long version;
}
