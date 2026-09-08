package com.lendingplatform.merchant;

import org.springframework.data.jpa.repository.JpaRepository;

/** Plain Spring Data repository - no custom queries needed for merchants yet. */
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
}
