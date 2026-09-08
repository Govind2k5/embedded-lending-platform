package com.lendingplatform.loanapplication;

import com.lendingplatform.borrower.BorrowerService;
import com.lendingplatform.common.exception.InvalidStateException;
import com.lendingplatform.eligibility.EligibilityService;
import com.lendingplatform.loan.LoanService;
import com.lendingplatform.merchant.MerchantService;
import com.lendingplatform.offer.LoanOfferService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Mockito-based unit tests for LoanApplicationService's state-machine guard
 * clauses - every collaborator (BorrowerService, MerchantService,
 * EligibilityService, LoanOfferService, LoanService, and the repository) is
 * mocked, so these tests run with no database and no Spring context at all,
 * purely checking that calling a workflow method from the WRONG status
 * throws InvalidStateException rather than proceeding.
 */
@ExtendWith(MockitoExtension.class)
class LoanApplicationServiceTest {

    @Mock
    private LoanApplicationRepository loanApplicationRepository;
    @Mock
    private BorrowerService borrowerService;
    @Mock
    private MerchantService merchantService;
    @Mock
    private EligibilityService eligibilityService;
    @Mock
    private LoanOfferService loanOfferService;
    @Mock
    private LoanService loanService;

    // Mockito wires the six @Mock fields above into this real
    // LoanApplicationService instance's constructor automatically.
    @InjectMocks
    private LoanApplicationService loanApplicationService;

    private LoanApplication application;

    @BeforeEach
    void setUp() {
        application = LoanApplication.builder()
                .id(1L)
                .borrowerId(1L)
                .merchantId(1L)
                .requestedAmount(BigDecimal.valueOf(50000))
                .requestedTenureMonths(12)
                .status(LoanApplicationStatus.CREATED)
                .build();
    }

    @Test
    void checkEligibilityRejectsApplicationNotInCreatedState() {
        // Simulate an application that has already had its eligibility checked once.
        application.setStatus(LoanApplicationStatus.OFFERS_AVAILABLE);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> loanApplicationService.checkEligibility(1L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("OFFERS_AVAILABLE"); // error message should name the actual (wrong) status, for a useful client error
    }

    @Test
    void selectOfferRejectsApplicationNotInOffersAvailableState() {
        // Trying to select an offer before eligibility has even been checked.
        application.setStatus(LoanApplicationStatus.CREATED);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> loanApplicationService.selectOffer(1L, 99L))
                .isInstanceOf(InvalidStateException.class);
    }

    @Test
    void approveRejectsApplicationNotInOfferSelectedState() {
        // Trying to approve before any offer has been selected.
        application.setStatus(LoanApplicationStatus.OFFERS_AVAILABLE);
        when(loanApplicationRepository.findById(1L)).thenReturn(Optional.of(application));

        assertThatThrownBy(() -> loanApplicationService.approve(1L))
                .isInstanceOf(InvalidStateException.class)
                .hasMessageContaining("OFFERS_AVAILABLE");
    }
}
