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
                .monthlyEmi(offer.getMonthlyEmi()) // copied from the offer, not recalculated - see Loan.monthlyEmi's comment
                .outstandingAmount(offer.getApprovedAmount()) // full principal is owed on day one
                .status(LoanStatus.ACTIVE)
                .startDate(startDate)
                .maturityDate(startDate.plusMonths(offer.getTenureMonths()))
                .build();

        Loan savedLoan = loanRepository.save(loan);
        // Must run AFTER the loan has an id (needs savedLoan.getId() as the
        // FK for every Repayment row) - this call and the save() above are
        // both inside the single @Transactional boundary owned by the
        // caller (LoanApplicationService.approve), so if schedule
        // generation throws, the loan insert above rolls back too.
        repaymentService.generateSchedule(savedLoan);

        log.info("Loan {} created for application {}", savedLoan.getId(), application.getId());
        return savedLoan;
    }

    public Loan getById(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + id));
    }
}
