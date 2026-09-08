package com.lendingplatform.borrower;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Spring Data JPA repository for Borrower. Extending JpaRepository<Borrower, Long>
 * gives us findById, findAll, save, delete, etc. for free - Spring generates
 * the implementation at runtime, we never write SQL or an implementation
 * class ourselves. No custom query methods are needed here yet; add them
 * as `findByXxx(...)` method signatures if a new lookup is ever required.
 */
public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
}
