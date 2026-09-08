package com.lendingplatform.loanapplication;

import org.springframework.data.jpa.repository.JpaRepository;

/** Plain Spring Data repository - LoanApplicationService always looks up applications by their primary key id. */
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
