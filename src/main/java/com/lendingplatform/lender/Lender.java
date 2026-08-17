package com.lendingplatform.lender;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "lenders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lender {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LenderType type;

    @Column(name = "minimum_income", nullable = false)
    private BigDecimal minimumIncome;

    @Column(name = "minimum_credit_score", nullable = false)
    private Integer minimumCreditScore;

    @Column(name = "maximum_loan_amount", nullable = false)
    private BigDecimal maximumLoanAmount;

    @Column(name = "minimum_tenure_months", nullable = false)
    private Integer minimumTenureMonths;

    @Column(name = "maximum_tenure_months", nullable = false)
    private Integer maximumTenureMonths;

    @Column(name = "base_interest_rate", nullable = false)
    private BigDecimal baseInterestRate;

    @Column(nullable = false)
    private boolean active;
}
