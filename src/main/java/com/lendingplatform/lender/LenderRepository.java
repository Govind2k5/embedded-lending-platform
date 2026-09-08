package com.lendingplatform.lender;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LenderRepository extends JpaRepository<Lender, Long> {

    // Spring Data derives the query from the method name at startup:
    // "SELECT * FROM lenders WHERE active = true". Used by LenderCacheService
    // to know which lender ids to run the eligibility engine against - an
    // inactive lender is never even considered, let alone cached.
    List<Lender> findByActiveTrue();
}
