package com.lendingplatform.repayment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RepaymentRepository extends JpaRepository<Repayment, Long> {

    // Powers GET /loans/{id}/repayments - returns the whole schedule in
    // natural order (installment 1, 2, 3, ...) rather than insertion or id
    // order, which happen to be the same today but shouldn't be relied on.
    List<Repayment> findByLoanIdOrderByInstallmentNumberAsc(Long loanId);
}
