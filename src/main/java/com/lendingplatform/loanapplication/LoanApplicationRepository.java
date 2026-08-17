package com.lendingplatform.loanapplication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
}
