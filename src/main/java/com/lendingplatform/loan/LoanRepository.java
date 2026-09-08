package com.lendingplatform.loan;

import org.springframework.data.jpa.repository.JpaRepository;

/** Plain Spring Data repository. Note LoanRepository.save() participates in JPA's automatic optimistic-lock check via Loan.version - see Loan.java. */
public interface LoanRepository extends JpaRepository<Loan, Long> {
}
