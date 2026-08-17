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

    @GetMapping("/{id}/repayments")
    public List<RepaymentResponse> getRepaymentSchedule(@PathVariable Long id) {
        return repaymentService.getScheduleForLoan(id).stream().map(RepaymentResponse::from).toList();
    }

    @PostMapping("/{id}/repayments/{repaymentId}")
    public RepaymentResponse makeRepayment(@PathVariable Long id, @PathVariable Long repaymentId,
                                            @Valid @RequestBody(required = false) MakeRepaymentRequest request) {
        var amount = request != null ? request.amount() : null;
        return RepaymentResponse.from(repaymentService.makeRepayment(id, repaymentId, amount));
    }
}
