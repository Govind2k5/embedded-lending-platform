package com.lendingplatform.offer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanOfferRepository extends JpaRepository<LoanOffer, Long> {

    List<LoanOffer> findByApplicationId(Long applicationId);
}
