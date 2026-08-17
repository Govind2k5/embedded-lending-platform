package com.lendingplatform.loanapplication;

import com.lendingplatform.common.exception.RateLimitExceededException;
import com.lendingplatform.common.idempotency.IdempotencyService;
import com.lendingplatform.common.ratelimit.RateLimiterService;
import com.lendingplatform.loan.dto.LoanResponse;
import com.lendingplatform.loanapplication.dto.LoanApplicationRequest;
import com.lendingplatform.loanapplication.dto.LoanApplicationResponse;
import com.lendingplatform.offer.dto.LoanOfferResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.List;

@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Applications")
public class LoanApplicationController {

    private static final int APPLICATIONS_PER_MINUTE_PER_BORROWER = 10;

    private final LoanApplicationService loanApplicationService;
    private final IdempotencyService idempotencyService;
    private final RateLimiterService rateLimiterService;

    @PostMapping
    public ResponseEntity<LoanApplicationResponse> create(
            @Valid @RequestBody LoanApplicationRequest request,
            @Parameter(description = "Client-generated key to safely retry this request")
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        enforceBorrowerCreationLimit(request.borrowerId());

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            LoanApplicationResponse response = LoanApplicationResponse.from(loanApplicationService.create(request));
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        }

        return idempotencyService.execute(idempotencyKey, request, HttpStatus.CREATED, LoanApplicationResponse.class,
                () -> LoanApplicationResponse.from(loanApplicationService.create(request)));
    }

    @GetMapping("/{id}")
    public LoanApplicationResponse getById(@PathVariable Long id) {
        return LoanApplicationResponse.from(loanApplicationService.getById(id));
    }

    @PostMapping("/{id}/check-eligibility")
    public LoanApplicationResponse checkEligibility(@PathVariable Long id) {
        return LoanApplicationResponse.from(loanApplicationService.checkEligibility(id));
    }

    @GetMapping("/{id}/offers")
    public List<LoanOfferResponse> getOffers(@PathVariable Long id) {
        return loanApplicationService.getOffers(id).stream().map(LoanOfferResponse::from).toList();
    }

    @PostMapping("/{id}/offers/{offerId}/select")
    public LoanApplicationResponse selectOffer(@PathVariable Long id, @PathVariable Long offerId) {
        return LoanApplicationResponse.from(loanApplicationService.selectOffer(id, offerId));
    }

    @PostMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id) {
        return LoanResponse.from(loanApplicationService.approve(id));
    }

    private void enforceBorrowerCreationLimit(Long borrowerId) {
        String key = rateLimiterService.windowedKey("ratelimit:app-create:borrower:" + borrowerId, 60);
        if (!rateLimiterService.isAllowed(key, APPLICATIONS_PER_MINUTE_PER_BORROWER, Duration.ofMinutes(2))) {
            throw new RateLimitExceededException(
                    "Too many loan applications, limit is " + APPLICATIONS_PER_MINUTE_PER_BORROWER + " per minute per borrower");
        }
    }
}
