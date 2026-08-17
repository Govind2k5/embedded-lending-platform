package com.lendingplatform.lender;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LenderRepository extends JpaRepository<Lender, Long> {

    List<Lender> findByActiveTrue();
}
