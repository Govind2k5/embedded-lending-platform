package com.lendingplatform.borrower.dto;

import com.lendingplatform.borrower.EmploymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Inbound shape for POST /api/v1/borrowers - a Java record (immutable, all
 * fields final, equals/hashCode/toString generated). This is a separate
 * type from the Borrower entity on purpose: it only carries what a client
 * is allowed to submit (no id, no createdAt - those are server-controlled),
 * and it's where Bean Validation annotations live.
 *
 * Every annotation here is enforced by @Valid on BorrowerController.create();
 * a violation short-circuits into a 400 VALIDATION_ERROR before the
 * controller method body ever runs (see GlobalExceptionHandler).
 */
public record BorrowerRequest(
        @NotBlank String name,
        @NotBlank @Email String email,          // must be non-blank AND look like an email address
        @NotBlank String phone,
        @NotNull @Past LocalDate dateOfBirth,   // @Past: must be a date before today (can't be born in the future)
        @NotNull @PositiveOrZero BigDecimal monthlyIncome, // 0 is allowed (e.g. unemployed applicant), negative is not
        @NotNull EmploymentType employmentType,
        @NotNull @Min(300) @Max(900) Integer creditScore   // matches the real-world CIBIL score range used in India
) {
}
