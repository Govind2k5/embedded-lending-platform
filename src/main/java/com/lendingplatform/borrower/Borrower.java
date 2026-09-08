package com.lendingplatform.borrower;

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
 * JPA entity mapped to the "borrowers" table (see V1__init_schema.sql).
 * Represents a person who might take a loan. This is reference data that
 * every other package (loanapplication, eligibility, loan, repayment) reads
 * by id, but never joins to directly via a JPA relationship - see
 * BorrowerService for the only supported way another package touches this.
 *
 * Lombok annotations generate the boilerplate we'd otherwise hand-write:
 * @Getter/@Setter      -> getX()/setX() for every field
 * @NoArgsConstructor    -> a public no-arg constructor (required by JPA/Hibernate)
 * @AllArgsConstructor   -> a constructor taking every field, used by @Builder
 * @Builder              -> Borrower.builder().name(...).email(...).build()
 */
@Entity
@Table(name = "borrowers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Borrower {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Postgres BIGSERIAL: DB assigns the next id on insert
    private Long id;

    @Column(nullable = false)
    private String name;

    // UNIQUE at the DB level (see schema) - two borrowers can't share an email.
    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    // Used directly by IncomeEligibilityRule when checking a lender's minimum income.
    @Column(name = "monthly_income", nullable = false)
    private BigDecimal monthlyIncome;

    // Stored as its enum name ("SALARIED"/"SELF_EMPLOYED") in the DB column,
    // not as an ordinal integer - safer if the enum is ever reordered.
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false)
    private EmploymentType employmentType;

    // 300-900 range enforced at the API boundary (see BorrowerRequest), not here.
    // Used directly by CreditScoreEligibilityRule.
    @Column(name = "credit_score", nullable = false)
    private Integer creditScore;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * JPA lifecycle callback: runs automatically right before the INSERT.
     * Stamps createdAt server-side so callers never have to (and can't lie
     * about) when a borrower record was actually created.
     */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
