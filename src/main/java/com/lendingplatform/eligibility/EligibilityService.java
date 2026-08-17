package com.lendingplatform.eligibility;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.lender.LenderCacheService;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Runs every registered EligibilityRule against each active lender and
 * returns only the lenders the borrower qualifies with for all rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final LenderCacheService lenderCacheService;
    private final List<EligibilityRule> rules;

    public List<LenderRules> findEligibleLenders(Borrower borrower, LoanApplication application) {
        List<LenderRules> activeLenders = lenderCacheService.getActiveLenderRules();

        return activeLenders.stream()
                .filter(lender -> isEligibleForLender(borrower, application, lender))
                .toList();
    }

    private boolean isEligibleForLender(Borrower borrower, LoanApplication application, LenderRules lender) {
        for (EligibilityRule rule : rules) {
            if (!rule.isEligible(borrower, application, lender)) {
                log.debug("Borrower {} failed rule {} for lender {}: {}",
                        borrower.getId(), rule.getClass().getSimpleName(), lender.id(), rule.getFailureReason());
                return false;
            }
        }
        return true;
    }
}
