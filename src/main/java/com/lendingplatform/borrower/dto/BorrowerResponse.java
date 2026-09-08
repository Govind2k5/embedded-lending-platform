package com.lendingplatform.borrower.dto;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.borrower.EmploymentType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Outbound shape returned by the Borrower endpoints. Kept separate from the
 * Borrower entity so the API's public contract doesn't accidentally change
 * every time the entity's internal fields do, and so we never serialize a
 * lazily-loaded JPA proxy straight to JSON (a classic source of
 * LazyInitializationException / infinite-recursion bugs).
 */
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
    /** Static factory: maps an entity to its DTO. Called from the controller, never from the service layer. */
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
