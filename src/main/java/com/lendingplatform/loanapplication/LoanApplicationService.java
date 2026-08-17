package com.lendingplatform.loanapplication;

import com.lendingplatform.borrower.Borrower;
import com.lendingplatform.borrower.BorrowerService;
import com.lendingplatform.common.exception.InvalidStateException;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.eligibility.EligibilityService;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loan.Loan;
import com.lendingplatform.loan.LoanService;
import com.lendingplatform.loanapplication.dto.LoanApplicationRequest;
import com.lendingplatform.merchant.MerchantService;
import com.lendingplatform.offer.LoanOffer;
import com.lendingplatform.offer.LoanOfferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanApplicationService {

    private final LoanApplicationRepository loanApplicationRepository;
    private final BorrowerService borrowerService;
    private final MerchantService merchantService;
    private final EligibilityService eligibilityService;
    private final LoanOfferService loanOfferService;
    private final LoanService loanService;

    public LoanApplication create(LoanApplicationRequest request) {
        borrowerService.getById(request.borrowerId());
        merchantService.getById(request.merchantId());

        LoanApplication application = LoanApplication.builder()
                .borrowerId(request.borrowerId())
                .merchantId(request.merchantId())
                .requestedAmount(request.requestedAmount())
                .requestedTenureMonths(request.requestedTenureMonths())
                .status(LoanApplicationStatus.CREATED)
                .build();

        LoanApplication saved = loanApplicationRepository.save(application);
        log.info("Loan application {} created for borrower {}", saved.getId(), saved.getBorrowerId());
        return saved;
    }

    public LoanApplication getById(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan application not found: " + id));
    }

    /**
     * Runs eligibility rules against every active lender. If at least one lender
     * qualifies, offers are generated and the application moves to OFFERS_AVAILABLE.
     * Otherwise the application is REJECTED - there is nothing further a borrower
     * can do on this application.
     */
    @Transactional
    public LoanApplication checkEligibility(Long applicationId) {
        LoanApplication application = getById(applicationId);
        if (application.getStatus() != LoanApplicationStatus.CREATED) {
            throw new InvalidStateException(
                    "Eligibility can only be checked once, application is in status " + application.getStatus());
        }

        Borrower borrower = borrowerService.getById(application.getBorrowerId());
        List<LenderRules> eligibleLenders = eligibilityService.findEligibleLenders(borrower, application);

        if (eligibleLenders.isEmpty()) {
            application.setStatus(LoanApplicationStatus.REJECTED);
            log.info("Application {} rejected, no lender matched eligibility rules", applicationId);
        } else {
            loanOfferService.generateOffers(application, eligibleLenders);
            application.setStatus(LoanApplicationStatus.OFFERS_AVAILABLE);
        }

        return loanApplicationRepository.save(application);
    }

    public List<LoanOffer> getOffers(Long applicationId) {
        getById(applicationId);
        return loanOfferService.getOffersForApplication(applicationId);
    }

    @Transactional
    public LoanApplication selectOffer(Long applicationId, Long offerId) {
        LoanApplication application = getById(applicationId);
        if (application.getStatus() != LoanApplicationStatus.OFFERS_AVAILABLE) {
            throw new InvalidStateException(
                    "Cannot select an offer, application is in status " + application.getStatus());
        }

        loanOfferService.selectOffer(applicationId, offerId);

        application.setStatus(LoanApplicationStatus.OFFER_SELECTED);
        return loanApplicationRepository.save(application);
    }

    /**
     * Creates the loan and its repayment schedule from the selected offer,
     * then marks the application APPROVED. All three writes happen in one
     * transaction so a failure partway through never leaves an orphaned loan
     * or a schedule-less loan behind.
     */
    @Transactional
    public Loan approve(Long applicationId) {
        LoanApplication application = getById(applicationId);
        if (application.getStatus() != LoanApplicationStatus.OFFER_SELECTED) {
            throw new InvalidStateException(
                    "Cannot approve, application is in status " + application.getStatus());
        }

        LoanOffer selectedOffer = loanOfferService.getSelectedOffer(applicationId);
        Loan loan = loanService.createLoanFromOffer(application, selectedOffer);

        application.setStatus(LoanApplicationStatus.APPROVED);
        loanApplicationRepository.save(application);

        log.info("Application {} approved, loan {} created", applicationId, loan.getId());
        return loan;
    }
}
