package com.lendingplatform.loan.dto;

import com.lendingplatform.loan.Loan;
import com.lendingplatform.loan.LoanStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

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
