package com.lendingplatform.repayment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    List<Repayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
}
