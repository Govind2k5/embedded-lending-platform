package com.lendingplatform.offer;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * JPA entity mapped to "loan_offers" - one row per lender that a
 * LoanApplication was found eligible for (see LoanOfferService.generateOffers,
 * which creates one of these per matching lender in EligibilityService's result).
 * approvedAmount/tenureMonths are copied straight from the application's
 * request (eligibility already confirmed they fit the lender's limits);
 * interestRate is the lender's flat baseInterestRate; monthlyEmi is
 * pre-computed via EmiCalculatorService so the borrower can compare offers
 * without any extra calculation on the client side.
 */
@Entity
@Table(name = "loan_offers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "application_id", nullable = false)
    private Long applicationId;

    @Column(name = "lender_id", nullable = false)
    private Long lenderId;

    @Column(name = "approved_amount", nullable = false)
    private BigDecimal approvedAmount;

    @Column(name = "interest_rate", nullable = false)
    private BigDecimal interestRate;

    @Column(name = "tenure_months", nullable = false)
    private Integer tenureMonths;

    @Column(name = "monthly_emi", nullable = false)
    private BigDecimal monthlyEmi;

    // See OfferStatus: AVAILABLE -> SELECTED (this one) / EXPIRED (its
    // siblings), driven entirely by LoanOfferService.selectOffer().
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OfferStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Every freshly-generated offer starts life as AVAILABLE, stamped with its creation time. */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (status == null) {
            status = OfferStatus.AVAILABLE;
        }
    }
}
