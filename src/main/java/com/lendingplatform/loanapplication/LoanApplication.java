package com.lendingplatform.loanapplication;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

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

    @Column(name = "requested_amount", nullable = false)
    private BigDecimal requestedAmount;

    @Column(name = "requested_tenure_months", nullable = false)
    private Integer requestedTenureMonths;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanApplicationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = LoanApplicationStatus.CREATED;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
