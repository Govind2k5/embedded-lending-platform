package com.lendingplatform.lender;

import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.lender.dto.LenderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LenderService {

    private final LenderRepository lenderRepository;
    private final LenderCacheService lenderCacheService;

    public Lender create(LenderRequest request) {
        Lender lender = Lender.builder()
                .name(request.name())
                .type(request.type())
                .minimumIncome(request.minimumIncome())
                .minimumCreditScore(request.minimumCreditScore())
                .maximumLoanAmount(request.maximumLoanAmount())
                .minimumTenureMonths(request.minimumTenureMonths())
                .maximumTenureMonths(request.maximumTenureMonths())
                .baseInterestRate(request.baseInterestRate())
                .active(request.active())
                .build();
        return lenderRepository.save(lender);
    }

    public Lender update(Long id, LenderRequest request) {
        Lender lender = getById(id);
        lender.setName(request.name());
        lender.setType(request.type());
        lender.setMinimumIncome(request.minimumIncome());
        lender.setMinimumCreditScore(request.minimumCreditScore());
        lender.setMaximumLoanAmount(request.maximumLoanAmount());
        lender.setMinimumTenureMonths(request.minimumTenureMonths());
        lender.setMaximumTenureMonths(request.maximumTenureMonths());
        lender.setBaseInterestRate(request.baseInterestRate());
        lender.setActive(request.active());
        Lender saved = lenderRepository.save(lender);
        lenderCacheService.evict(id);
        return saved;
    }

    public Lender getById(Long id) {
        return lenderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lender not found: " + id));
    }

    public List<Lender> getAll() {
        return lenderRepository.findAll();
    }
}
