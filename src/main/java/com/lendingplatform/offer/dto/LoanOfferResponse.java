package com.lendingplatform.offer.dto;

import com.lendingplatform.offer.LoanOffer;
import com.lendingplatform.offer.OfferStatus;

import java.math.BigDecimal;
import java.time.Instant;

/** Outbound shape for GET /api/v1/loan-applications/{id}/offers - one entry per lender that matched eligibility. */
public record LoanOfferResponse(
        Long id,
        Long applicationId,
        Long lenderId,
        BigDecimal approvedAmount,
        BigDecimal interestRate,
        Integer tenureMonths,
        BigDecimal monthlyEmi,
        OfferStatus status,
        Instant createdAt
) {
    public static LoanOfferResponse from(LoanOffer offer) {
        return new LoanOfferResponse(
                offer.getId(),
                offer.getApplicationId(),
                offer.getLenderId(),
                offer.getApprovedAmount(),
                offer.getInterestRate(),
                offer.getTenureMonths(),
                offer.getMonthlyEmi(),
                offer.getStatus(),
                offer.getCreatedAt()
        );
    }
}
