package com.lendingplatform.repayment;

import com.lendingplatform.common.emi.AmortizationEntry;
import com.lendingplatform.common.emi.EmiCalculatorService;
import com.lendingplatform.common.exception.InvalidStateException;
import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.loan.Loan;
import com.lendingplatform.loan.LoanRepository;
import com.lendingplatform.loan.LoanStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepaymentService {

    private final RepaymentRepository repaymentRepository;
    // Note: this service reaches directly into LoanRepository (not
    // LoanService) to read/update the Loan during a repayment - repayment
    // and loan are tightly coupled around the same aggregate (the loan's
    // outstanding balance), so it's simpler to update it in the same
    // transaction here than to round-trip through another service.
    private final LoanRepository loanRepository;
    private final EmiCalculatorService emiCalculatorService;

    /**
     * Builds and persists the full installment schedule for a brand-new
     * loan. Called once, from LoanService.createLoanFromOffer(), inside the
     * same transaction that created the Loan row.
     */
    public List<Repayment> generateSchedule(Loan loan) {
        List<AmortizationEntry> schedule = emiCalculatorService.generateSchedule(
                loan.getPrincipalAmount(), loan.getInterestRate(), loan.getTenureMonths(), loan.getStartDate());

        List<Repayment> repayments = schedule.stream()
                .map(entry -> Repayment.builder()
                        .loanId(loan.getId())
                        .installmentNumber(entry.installmentNumber())
                        .dueDate(entry.dueDate())
                        .principalAmount(entry.principalComponent())
                        .interestAmount(entry.interestComponent())
                        .totalAmount(entry.emiAmount())
                        .paidAmount(BigDecimal.ZERO)
                        .status(RepaymentStatus.PENDING)
                        .build())
                .toList();

        return repaymentRepository.saveAll(repayments);
    }

    /** Read-only: the full schedule for GET /loans/{id}/repayments, in installment order. */
    public List<Repayment> getScheduleForLoan(Long loanId) {
        return repaymentRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId);
    }

    /**
     * Simulates a repayment: updates the installment's paid amount/status and
     * reduces the loan's outstanding balance in the same transaction. The
     * loan's optimistic @Version guards against two concurrent repayments
     * corrupting the outstanding balance.
     */
    @Transactional
    public Repayment makeRepayment(Long loanId, Long repaymentId, BigDecimal requestedAmount) {
        Repayment repayment = repaymentRepository.findById(repaymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Repayment not found: " + repaymentId));
        if (!repayment.getLoanId().equals(loanId)) {
            throw new InvalidStateException("Repayment " + repaymentId + " does not belong to loan " + loanId);
        }
        if (repayment.getStatus() == RepaymentStatus.PAID) {
            throw new InvalidStateException("Repayment " + repaymentId + " is already fully paid");
        }

        BigDecimal remainingDue = repayment.getTotalAmount().subtract(repayment.getPaidAmount());
        // No amount in the request body means "pay whatever's still due, in full".
        BigDecimal amountToApply = requestedAmount != null ? requestedAmount : remainingDue;

        if (amountToApply.compareTo(remainingDue) > 0) {
            // Reject overpayment outright rather than silently capping it or
            // crediting the excess elsewhere - keeps the payment model simple.
            throw new InvalidStateException("Payment amount exceeds the remaining due amount of " + remainingDue);
        }

        repayment.setPaidAmount(repayment.getPaidAmount().add(amountToApply));
        boolean fullyPaid = repayment.getPaidAmount().compareTo(repayment.getTotalAmount()) >= 0;
        repayment.setStatus(fullyPaid ? RepaymentStatus.PAID : RepaymentStatus.PARTIAL);
        if (fullyPaid) {
            repayment.setPaidAt(Instant.now());
        }
        repaymentRepository.save(repayment);

        // Second write in the same transaction: pull the same amount off
        // the loan's running balance. If another repayment on this same
        // loan committed between when we started this method and this
        // save(), Loan.version's optimistic check makes THIS save fail with
        // ObjectOptimisticLockingFailureException (-> 409 CONCURRENT_UPDATE)
        // instead of silently overwriting the other update.
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(amountToApply));
        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setOutstandingAmount(BigDecimal.ZERO); // guards against a tiny negative balance from rounding
            loan.setStatus(LoanStatus.COMPLETED);
        }
        loanRepository.save(loan);

        log.info("Repayment {} on loan {} processed, amount={}", repaymentId, loanId, amountToApply);
        return repayment;
    }
}
