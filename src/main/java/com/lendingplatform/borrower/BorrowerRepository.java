package com.lendingplatform.borrower;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
}
