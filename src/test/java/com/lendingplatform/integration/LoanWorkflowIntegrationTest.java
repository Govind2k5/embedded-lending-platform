package com.lendingplatform.integration;

import com.lendingplatform.loan.dto.LoanResponse;
import com.lendingplatform.loanapplication.LoanApplicationStatus;
import com.lendingplatform.loanapplication.dto.LoanApplicationRequest;
import com.lendingplatform.loanapplication.dto.LoanApplicationResponse;
import com.lendingplatform.offer.dto.LoanOfferResponse;
import com.lendingplatform.repayment.RepaymentStatus;
import com.lendingplatform.repayment.dto.RepaymentResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Walks the whole borrower journey end to end against a real Postgres +
 * Redis, using the borrowers/lenders/merchants Flyway seeds instead of
 * creating fixtures by hand.
 */
class LoanWorkflowIntegrationTest extends AbstractIntegrationTest {

    private static final Long ROHAN_BORROWER_ID = 1L; // income 55000, credit score 720
    private static final Long TECHKART_MERCHANT_ID = 1L;

    @Test
    void fullLoanLifecycleFromApplicationToRepayment() {
        // Step 1: create the application.
        LoanApplicationRequest request = new LoanApplicationRequest(
                ROHAN_BORROWER_ID, TECHKART_MERCHANT_ID, BigDecimal.valueOf(200000), 24);

        ResponseEntity<LoanApplicationResponse> createResponse =
                restTemplate.postForEntity("/api/v1/loan-applications", request, LoanApplicationResponse.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long applicationId = createResponse.getBody().id();
        assertThat(createResponse.getBody().status()).isEqualTo(LoanApplicationStatus.CREATED);

        // Step 2: check eligibility - Rohan qualifies for BankOne and QuickCredit NBFC (see V2 seed data comments).
        ResponseEntity<LoanApplicationResponse> eligibilityResponse = restTemplate.postForEntity(
                "/api/v1/loan-applications/{id}/check-eligibility", null, LoanApplicationResponse.class, applicationId);
        assertThat(eligibilityResponse.getBody().status()).isEqualTo(LoanApplicationStatus.OFFERS_AVAILABLE);

        // Step 3: view the generated offers.
        ResponseEntity<LoanOfferResponse[]> offersResponse = restTemplate.getForEntity(
                "/api/v1/loan-applications/{id}/offers", LoanOfferResponse[].class, applicationId);
        List<LoanOfferResponse> offers = List.of(offersResponse.getBody());
        assertThat(offers).isNotEmpty();
        Long offerId = offers.get(0).id();

        // Step 4: select the first offer.
        ResponseEntity<LoanApplicationResponse> selectResponse = restTemplate.postForEntity(
                "/api/v1/loan-applications/{id}/offers/{offerId}/select", null,
                LoanApplicationResponse.class, applicationId, offerId);
        assertThat(selectResponse.getBody().status()).isEqualTo(LoanApplicationStatus.OFFER_SELECTED);

        // Step 5: approve - creates the Loan and its full repayment schedule in one transaction.
        ResponseEntity<LoanResponse> approveResponse = restTemplate.postForEntity(
                "/api/v1/loan-applications/{id}/approve", null, LoanResponse.class, applicationId);
        assertThat(approveResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        LoanResponse loan = approveResponse.getBody();
        assertThat(loan.status().name()).isEqualTo("ACTIVE");
        assertThat(loan.outstandingAmount()).isEqualByComparingTo(loan.principalAmount()); // full principal owed on day one

        // Step 6: view the schedule - 24 installments, all still PENDING.
        ResponseEntity<RepaymentResponse[]> scheduleResponse = restTemplate.getForEntity(
                "/api/v1/loans/{id}/repayments", RepaymentResponse[].class, loan.id());
        List<RepaymentResponse> schedule = List.of(scheduleResponse.getBody());
        assertThat(schedule).hasSize(24);
        assertThat(schedule).allMatch(installment -> installment.status() == RepaymentStatus.PENDING);

        // Step 7: pay the first installment in full.
        Long firstRepaymentId = schedule.get(0).id();
        // TestRestTemplate defaults to application/x-www-form-urlencoded
        // when posting a null body, which the server's @RequestBody(required=false)
        // handler doesn't accept (it only understands JSON) - an explicit
        // JSON content-type header with an empty body avoids that mismatch.
        // (A plain curl request with no body at all doesn't hit this, since
        // it sends no Content-Type header - this is purely a test-client quirk.)
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        ResponseEntity<RepaymentResponse> paymentResponse = restTemplate.postForEntity(
                "/api/v1/loans/{id}/repayments/{repaymentId}", new HttpEntity<>(jsonHeaders),
                RepaymentResponse.class, loan.id(), firstRepaymentId);
        assertThat(paymentResponse.getBody().status()).isEqualTo(RepaymentStatus.PAID);

        // The loan's outstanding balance must drop by exactly that installment's total amount.
        ResponseEntity<LoanResponse> loanAfterPayment = restTemplate.getForEntity(
                "/api/v1/loans/{id}", LoanResponse.class, loan.id());
        assertThat(loanAfterPayment.getBody().outstandingAmount())
                .isEqualByComparingTo(loan.principalAmount().subtract(schedule.get(0).totalAmount()));
    }
}
