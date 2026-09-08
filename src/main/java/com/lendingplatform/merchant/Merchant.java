package com.lendingplatform.merchant;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * JPA entity mapped to the "merchants" table. A merchant is the embedding
 * app (e.g. TechKart, TravelNow, EduMart from the seed data) whose
 * customers are the borrowers taking loans - the whole point of "embedded"
 * lending is that TechKart never has to integrate with BankOne, PrimeBank
 * etc. directly, only with this platform.
 *
 * Deliberately the simplest entity in the project: LoanApplication just
 * stores a merchantId FK and nothing else ever needs to join back to it.
 */
@Entity
@Table(name = "merchants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Merchant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /** Same pattern as Borrower/LoanApplication: stamp the creation time server-side, not client-supplied. */
    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
