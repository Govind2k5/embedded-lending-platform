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
 *
 * Called from LoanApplicationService.checkEligibility(). Every eligible
 * lender returned here gets turned into a LoanOffer by LoanOfferService;
 * zero eligible lenders is a valid outcome (the application is REJECTED),
 * not an error.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EligibilityService {

    private final LenderCacheService lenderCacheService;

    // Spring injects every @Component implementing EligibilityRule into this
    // list automatically (currently: Income, CreditScore, LoanAmount,
    // Tenure) - see EligibilityRule's Javadoc for why this needs no
    // registry/factory.
    private final List<EligibilityRule> rules;

    /** Public entry point: which of the currently active lenders would approve this borrower/application combination? */
    public List<LenderRules> findEligibleLenders(Borrower borrower, LoanApplication application) {
        // Goes through Redis first via LenderCacheService - lender rules are
        // read on every single application but change rarely, so this is
        // the main thing the cache-aside cache is protecting Postgres from.
        List<LenderRules> activeLenders = lenderCacheService.getActiveLenderRules();

        return activeLenders.stream()
                .filter(lender -> isEligibleForLender(borrower, application, lender))
                .toList();
    }

    /** All 4 rules must pass for one lender; the loop short-circuits and returns false on the first rule that fails. */
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
