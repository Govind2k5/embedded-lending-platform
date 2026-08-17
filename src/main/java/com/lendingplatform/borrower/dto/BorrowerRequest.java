package com.lendingplatform.borrower.dto;

import com.lendingplatform.borrower.EmploymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BorrowerRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank String phone,
        @NotNull @Past LocalDate dateOfBirth,
        @NotNull @PositiveOrZero BigDecimal monthlyIncome,
        @NotNull EmploymentType employmentType,
        @NotNull @Min(300) @Max(900) Integer creditScore
) {
}
