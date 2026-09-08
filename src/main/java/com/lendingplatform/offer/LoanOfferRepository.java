package com.lendingplatform.offer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanOfferRepository extends JpaRepository<LoanOffer, Long> {

    // Backs three different things in LoanOfferService: listing all offers
    // for the "view offers" endpoint, finding the sibling offers to expire
    // when one is selected, and finding the SELECTED offer at approval time.
    List<LoanOffer> findByApplicationId(Long applicationId);
}
