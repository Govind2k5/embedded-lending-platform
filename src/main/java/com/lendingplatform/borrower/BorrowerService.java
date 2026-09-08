package com.lendingplatform.borrower;

import com.lendingplatform.borrower.dto.BorrowerRequest;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Business logic for borrowers. Every other package that needs a Borrower
 * (eligibility, loanapplication) calls getById() here rather than injecting
 * BorrowerRepository directly or holding a JPA @ManyToOne reference - this
 * is the one seam other packages are allowed to depend on.
 */
@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

    /** Builds a Borrower from the validated request DTO and persists it. */
    public Borrower create(BorrowerRequest request) {
        Borrower borrower = Borrower.builder()
                .name(request.name())
                .email(request.email())
                .phone(request.phone())
                .dateOfBirth(request.dateOfBirth())
                .monthlyIncome(request.monthlyIncome())
                .employmentType(request.employmentType())
                .creditScore(request.creditScore())
                .build();
        return borrowerRepository.save(borrower);
    }

    /**
     * Looks up a borrower by id or throws. Callers (e.g.
     * LoanApplicationService.create) rely on this throwing rather than
     * returning null/Optional, so a missing borrower always surfaces
     * consistently as a 404 via GlobalExceptionHandler.
     */
    public Borrower getById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found: " + id));
    }

    public List<Borrower> getAll() {
        return borrowerRepository.findAll();
    }
}
