package com.lendingplatform.loanapplication;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity mapped to the "loan_applications" table - the record that
 * anchors one borrower's whole journey through the platform. Everything
 * else (offers, the eventual loan, its repayments) traces back to one of
 * these via a foreign key.
 *
 * borrowerId/merchantId are plain FK columns, not @ManyToOne relationships
 * - see LoanApplicationService.create(), which explicitly calls
 * BorrowerService.getById()/MerchantService.getById() to validate they
 * exist before saving, rather than relying on Hibernate to fetch them lazily.
 */
@Entity
@Table(name = "loan_applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "borrower_id", nullable = false)
    private Long borrowerId;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    // What the borrower asked for - NOT necessarily what they'll be approved
    // for by every lender, though in this project's simplified flow the
    // approved amount always equals this (see LoanAmountEligibilityRule and
    // LoanOfferService).
    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "requested_tenure_months", nullable = false)
    private Integer requestedTenureMonths;

    // See LoanApplicationStatus for the full state machine. Driven entirely
    // by LoanApplicationService - never set directly by a controller.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanApplicationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    /** Stamps createdAt/updatedAt and defaults a brand-new application to CREATED, right before the INSERT. */
    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = LoanApplicationStatus.CREATED;
        }
    }

    /** Runs before every UPDATE (e.g. every status transition) so updatedAt always reflects the last change, automatically. */
    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
