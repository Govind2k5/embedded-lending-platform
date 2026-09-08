package com.lendingplatform.loan.dto;

import com.lendingplatform.loan.Loan;
import com.lendingplatform.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Outbound shape for GET /api/v1/loans/{id} and the response of POST .../approve. Note this intentionally omits Loan.version - that's an internal JPA concurrency-control detail, not part of the public API contract. */
public record LoanResponse(
        Long id,
        Long applicationId,
        Long offerId,
        Long borrowerId,
        Long lenderId,
        BigDecimal principalAmount,
        BigDecimal interestRate,
        Integer tenureMonths,
        BigDecimal monthlyEmi,
        BigDecimal outstandingAmount,
        LoanStatus status,
        LocalDate startDate,
        LocalDate maturityDate
) {
    public static LoanResponse from(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getApplicationId(),
                loan.getOfferId(),
                loan.getBorrowerId(),
                loan.getLenderId(),
                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTenureMonths(),
                loan.getMonthlyEmi(),
                loan.getOutstandingAmount(),
                loan.getStatus(),
                loan.getStartDate(),
                loan.getMaturityDate()
        );
    }
}
