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

/**
 * REST layer for the core loan-application workflow - every endpoint here
 * maps to one step of the lifecycle described in LoanApplicationService.
 * This controller is also where the two cross-cutting concerns specific to
 * application creation live: idempotency and the per-borrower rate limit
 * (the general per-IP rate limit is handled earlier, for every endpoint, by
 * RateLimitInterceptor).
 */
@RestController
@RequestMapping("/api/v1/loan-applications")
@RequiredArgsConstructor
@Tag(name = "Loan Applications")
public class LoanApplicationController {

    private static final int APPLICATIONS_PER_MINUTE_PER_BORROWER = 10;

    private final LoanApplicationService loanApplicationService;
    private final IdempotencyService idempotencyService;
    private final RateLimiterService rateLimiterService;

    /**
     * Step 1: create an application. Accepts an optional Idempotency-Key
     * header - if present, retries with the same key+body replay the
     * original response instead of creating a second application (see
     * IdempotencyService); if absent, every call creates a new application,
     * same as any normal POST.
     */
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

    /** Step 2: run the 4-rule eligibility engine against every active lender and generate offers for the matches. */
    @PostMapping("/{id}/check-eligibility")
    public LoanApplicationResponse checkEligibility(@PathVariable Long id) {
        return LoanApplicationResponse.from(loanApplicationService.checkEligibility(id));
    }

    /** Step 3: view the offers generated for this application (read-only, no state change). */
    @GetMapping("/{id}/offers")
    public List<LoanOfferResponse> getOffers(@PathVariable Long id) {
        return loanApplicationService.getOffers(id).stream().map(LoanOfferResponse::from).toList();
    }

    /** Step 4: the borrower picks one offer; every sibling offer on the same application is automatically expired. */
    @PostMapping("/{id}/offers/{offerId}/select")
    public LoanApplicationResponse selectOffer(@PathVariable Long id, @PathVariable Long offerId) {
        return LoanApplicationResponse.from(loanApplicationService.selectOffer(id, offerId));
    }

    /** Step 5: creates the Loan and its full repayment schedule in one transaction, then marks the application APPROVED. */
    @PostMapping("/{id}/approve")
    public LoanResponse approve(@PathVariable Long id) {
        return LoanResponse.from(loanApplicationService.approve(id));
    }

    /**
     * Endpoint-specific rate limit: at most 10 application creations per
     * minute for a single borrower id, checked BEFORE any DB work happens.
     * This is separate from (and in addition to) the general 60/minute-per-IP
     * limit already enforced by RateLimitInterceptor for every /api/v1/** call.
     */
    private void enforceBorrowerCreationLimit(Long borrowerId) {
        String key = rateLimiterService.windowedKey("ratelimit:app-create:borrower:" + borrowerId, 60);
        if (!rateLimiterService.isAllowed(key, APPLICATIONS_PER_MINUTE_PER_BORROWER, Duration.ofMinutes(2))) {
            throw new RateLimitExceededException(
                    "Too many loan applications, limit is " + APPLICATIONS_PER_MINUTE_PER_BORROWER + " per minute per borrower");
        }
    }
}
