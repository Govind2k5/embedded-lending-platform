package com.lendingplatform.borrower;

import com.lendingplatform.borrower.dto.BorrowerRequest;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowerService {

    private final BorrowerRepository borrowerRepository;

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

    public Borrower getById(Long id) {
        return borrowerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Borrower not found: " + id));
    }

    public List<Borrower> getAll() {
        return borrowerRepository.findAll();
    }
}
