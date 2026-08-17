package com.lendingplatform.integration;

import com.lendingplatform.loanapplication.dto.LoanApplicationRequest;
import com.lendingplatform.loanapplication.dto.LoanApplicationResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class IdempotencyIntegrationTest extends AbstractIntegrationTest {

    private static final Long BORROWER_ID = 2L; // Priya Sharma
    private static final Long MERCHANT_ID = 2L;

    @Test
    void repeatingSameKeyAndBodyReturnsTheOriginalApplicationInsteadOfCreatingANewOne() {
        LoanApplicationRequest request = new LoanApplicationRequest(
                BORROWER_ID, MERCHANT_ID, BigDecimal.valueOf(30000), 12);
        HttpEntity<LoanApplicationRequest> entity = withIdempotencyKey(request, "integration-test-key-1");

        ResponseEntity<LoanApplicationResponse> first = restTemplate.postForEntity(
                "/api/v1/loan-applications", entity, LoanApplicationResponse.class);
        ResponseEntity<LoanApplicationResponse> replay = restTemplate.postForEntity(
                "/api/v1/loan-applications", entity, LoanApplicationResponse.class);

        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replay.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(replay.getBody().id()).isEqualTo(first.getBody().id());
    }

    @Test
    void reusingKeyWithDifferentBodyIsRejected() {
        String key = "integration-test-key-2";
        HttpEntity<LoanApplicationRequest> first = withIdempotencyKey(
                new LoanApplicationRequest(BORROWER_ID, MERCHANT_ID, BigDecimal.valueOf(30000), 12), key);
        HttpEntity<LoanApplicationRequest> different = withIdempotencyKey(
                new LoanApplicationRequest(BORROWER_ID, MERCHANT_ID, BigDecimal.valueOf(99999), 12), key);

        restTemplate.postForEntity("/api/v1/loan-applications", first, LoanApplicationResponse.class);
        ResponseEntity<String> conflictResponse = restTemplate.postForEntity(
                "/api/v1/loan-applications", different, String.class);

        assertThat(conflictResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    private HttpEntity<LoanApplicationRequest> withIdempotencyKey(LoanApplicationRequest request, String key) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Idempotency-Key", key);
        return new HttpEntity<>(request, headers);
    }
}
