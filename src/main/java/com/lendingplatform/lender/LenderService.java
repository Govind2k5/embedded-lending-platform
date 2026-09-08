package com.lendingplatform.lender;

import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.lender.dto.LenderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * CRUD-ish business logic for lender configuration. The one thing that
 * makes this more than plain CRUD is update(): it has to keep Postgres
 * (source of truth) and Redis (LenderCacheService's cache-aside cache) in
 * sync, otherwise the eligibility engine could keep using stale limits for
 * up to 10 minutes after an admin changes them.
 */
@Service
@RequiredArgsConstructor
public class LenderService {

    private final LenderRepository lenderRepository;
    private final LenderCacheService lenderCacheService;

    public Lender create(LenderRequest request) {
        // No cache interaction needed here - a brand-new lender has nothing
        // cached yet, so there's nothing stale to evict.
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

    /**
     * Full update of an existing lender's rules. Order matters: we save the
     * Postgres row FIRST, then evict the Redis cache entry - so there is
     * never a window where Redis is stale relative to a committed Postgres
     * write. If eviction happened before the save (or the save failed), a
     * concurrent reader could repopulate the cache with the old values.
     */
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
        lenderCacheService.evict(id); // DEL lender:{id}:rules - next read is a guaranteed cache miss, forcing a fresh load
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
