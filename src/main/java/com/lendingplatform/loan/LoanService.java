package com.lendingplatform.loan;

import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.loanapplication.LoanApplication;
import com.lendingplatform.offer.LoanOffer;
import com.lendingplatform.repayment.RepaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final RepaymentService repaymentService;

    /**
     * Creates the loan from the selected offer and immediately generates its
     * repayment schedule. Called from within LoanApplicationService.approve(),
     * which wraps this in a single transaction.
     */
    public Loan createLoanFromOffer(LoanApplication application, LoanOffer offer) {
        LocalDate startDate = LocalDate.now();

        Loan loan = Loan.builder()
                .applicationId(application.getId())
                .offerId(offer.getId())
                .borrowerId(application.getBorrowerId())
                .lenderId(offer.getLenderId())
                .principalAmount(offer.getApprovedAmount())
                .interestRate(offer.getInterestRate())
                .tenureMonths(offer.getTenureMonths())
                .monthlyEmi(offer.getMonthlyEmi())
                .outstandingAmount(offer.getApprovedAmount())
                .status(LoanStatus.ACTIVE)
                .startDate(startDate)
                .maturityDate(startDate.plusMonths(offer.getTenureMonths()))
                .build();

        Loan savedLoan = loanRepository.save(loan);
        repaymentService.generateSchedule(savedLoan);

        log.info("Loan {} created for application {}", savedLoan.getId(), application.getId());
        return savedLoan;
    }

    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }
}
