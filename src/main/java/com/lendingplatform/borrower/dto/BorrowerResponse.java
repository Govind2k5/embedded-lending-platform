package com.lendingplatform.borrower.dto;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.borrower.EmploymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record BorrowerResponse(
        Long id,
        String name,
        String email,
        String phone,
        LocalDate dateOfBirth,
        BigDecimal monthlyIncome,
        EmploymentType employmentType,
        Integer creditScore,
        Instant createdAt
) {
    public static BorrowerResponse from(Borrower borrower) {
        return new BorrowerResponse(
                borrower.getId(),
                borrower.getName(),
                borrower.getEmail(),
                borrower.getPhone(),
                borrower.getDateOfBirth(),
                borrower.getMonthlyIncome(),
                borrower.getEmploymentType(),
                borrower.getCreditScore(),
                borrower.getCreatedAt()
        );
    }
}
