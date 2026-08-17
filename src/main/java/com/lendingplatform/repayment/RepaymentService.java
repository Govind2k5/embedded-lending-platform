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
    private final LoanRepository loanRepository;
    private final EmiCalculatorService emiCalculatorService;

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
        BigDecimal amountToApply = requestedAmount != null ? requestedAmount : remainingDue;

        if (amountToApply.compareTo(remainingDue) > 0) {
            throw new InvalidStateException("Payment amount exceeds the remaining due amount of " + remainingDue);
        }

        repayment.setPaidAmount(repayment.getPaidAmount().add(amountToApply));
        boolean fullyPaid = repayment.getPaidAmount().compareTo(repayment.getTotalAmount()) >= 0;
        repayment.setStatus(fullyPaid ? RepaymentStatus.PAID : RepaymentStatus.PARTIAL);
        if (fullyPaid) {
            repayment.setPaidAt(Instant.now());
        }
        repaymentRepository.save(repayment);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + loanId));
        loan.setOutstandingAmount(loan.getOutstandingAmount().subtract(amountToApply));
        if (loan.getOutstandingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setOutstandingAmount(BigDecimal.ZERO);
            loan.setStatus(LoanStatus.COMPLETED);
        }
        loanRepository.save(loan);

        log.info("Repayment {} on loan {} processed, amount={}", repaymentId, loanId, amountToApply);
        return repayment;
    }
}
