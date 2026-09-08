package com.lendingplatform.offer;

import com.lendingplatform.common.emi.EmiCalculatorService;
import com.lendingplatform.common.exception.InvalidStateException;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.lender.LenderRules;
import com.lendingplatform.loanapplication.LoanApplication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanOfferService {

    private final LoanOfferRepository loanOfferRepository;
    private final EmiCalculatorService emiCalculatorService;

    /**
     * Creates one AVAILABLE offer per eligible lender. The approved amount and tenure
     * mirror what the borrower requested since eligibility rules already confirmed
     * the request fits within the lender's limits.
     */
    public List<LoanOffer> generateOffers(LoanApplication application, List<LenderRules> eligibleLenders) {
        List<LoanOffer> offers = eligibleLenders.stream()
                .map(lender -> buildOffer(application, lender))
                .toList();
        List<LoanOffer> saved = loanOfferRepository.saveAll(offers);
        log.info("Generated {} offer(s) for application {}", saved.size(), application.getId());
        return saved;
    }

    /** One offer = the application's requested terms, priced at this specific lender's flat rate. */
    private LoanOffer buildOffer(LoanApplication application, LenderRules lender) {
        var emi = emiCalculatorService.calculateEmi(
                application.getRequestedAmount(), lender.baseInterestRate(), application.getRequestedTenureMonths());

        return LoanOffer.builder()
                .applicationId(application.getId())
                .lenderId(lender.id())
                .approvedAmount(application.getRequestedAmount())
                .interestRate(lender.baseInterestRate())
                .tenureMonths(application.getRequestedTenureMonths())
                .monthlyEmi(emi)
                .status(OfferStatus.AVAILABLE)
                .build();
    }

    /** Used by the "view offers" endpoint - returns whatever offers exist for this application, regardless of status. */
    public List<LoanOffer> getOffersForApplication(Long applicationId) {
        return loanOfferRepository.findByApplicationId(applicationId);
    }

    public LoanOffer getById(Long offerId) {
        return loanOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan offer not found: " + offerId));
    }

    /**
     * Marks the chosen offer SELECTED and expires the other offers on the same
     * application so only one offer can ever be carried forward into a loan.
     */
    public LoanOffer selectOffer(Long applicationId, Long offerId) {
        LoanOffer offer = getById(offerId);
        // Two separate, explicit guard checks (rather than one combined
        // condition) so the error message can say exactly what's wrong:
        // wrong application vs. already selected/expired are different bugs
        // for a client to fix.
        if (!offer.getApplicationId().equals(applicationId)) {
            throw new InvalidStateException("Offer " + offerId + " does not belong to application " + applicationId);
        }
        if (offer.getStatus() != OfferStatus.AVAILABLE) {
            throw new InvalidStateException("Offer " + offerId + " is not available for selection");
        }

        offer.setStatus(OfferStatus.SELECTED);
        loanOfferRepository.save(offer);

        // Expire every sibling offer still AVAILABLE on this application -
        // this is what makes "one selected offer per application" an
        // invariant rather than just a convention.
        List<LoanOffer> otherOffers = loanOfferRepository.findByApplicationId(applicationId);
        for (LoanOffer other : otherOffers) {
            if (!other.getId().equals(offerId) && other.getStatus() == OfferStatus.AVAILABLE) {
                other.setStatus(OfferStatus.EXPIRED);
                loanOfferRepository.save(other);
            }
        }

        return offer;
    }

    /** Used by LoanApplicationService.approve() to find which offer to build the Loan from. */
    public LoanOffer getSelectedOffer(Long applicationId) {
        return loanOfferRepository.findByApplicationId(applicationId).stream()
                .filter(offer -> offer.getStatus() == OfferStatus.SELECTED)
                .findFirst()
                .orElseThrow(() -> new InvalidStateException(
                        "No selected offer found for application " + applicationId));
    }
}
