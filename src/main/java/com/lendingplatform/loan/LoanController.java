package com.lendingplatform.loan;

import com.lendingplatform.loan.dto.LoanResponse;
import com.lendingplatform.repayment.RepaymentService;
import com.lendingplatform.repayment.dto.MakeRepaymentRequest;
import com.lendingplatform.repayment.dto.RepaymentResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST layer for viewing an approved loan and its repayment schedule, and
 * for simulating a repayment. Note this controller also exposes the
 * repayment endpoints (nested under /loans/{id}/repayments) even though the
 * repayment logic itself lives in the `repayment` package - keeping the
 * URL hierarchy (a repayment belongs to a loan) separate from the package
 * hierarchy (repayment is its own domain module) is a deliberate choice.
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
@Tag(name = "Loans")
public class LoanController {

    private final LoanService loanService;
    private final RepaymentService repaymentService;

    @GetMapping("/{id}")
    public LoanResponse getById(@PathVariable Long id) {
        return LoanResponse.from(loanService.getById(id));
    }

    /** Read-only view of every installment generated at approval time (see RepaymentService.generateSchedule). */
    @GetMapping("/{id}/repayments")
    public List<RepaymentResponse> getRepaymentSchedule(@PathVariable Long id) {
        return repaymentService.getScheduleForLoan(id).stream().map(RepaymentResponse::from).toList();
    }

    /**
     * Simulates paying one installment. The request body is optional
     * (required = false) - omitting `amount` pays whatever is still due on
     * that installment in full; a smaller amount records a PARTIAL payment.
     * See RepaymentService.makeRepayment() for the actual balance/status logic.
     */
    @PostMapping("/{id}/repayments/{repaymentId}")
    public RepaymentResponse makeRepayment(@PathVariable Long id, @PathVariable Long repaymentId,
                                            @Valid @RequestBody(required = false) MakeRepaymentRequest request) {
        var amount = request != null ? request.amount() : null;
        return RepaymentResponse.from(repaymentService.makeRepayment(id, repaymentId, amount));
    }
}
