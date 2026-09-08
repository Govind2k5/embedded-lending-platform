package com.lendingplatform.lender;

import com.lendingplatform.lender.dto.LenderRequest;
import com.lendingplatform.lender.dto.LenderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST layer for managing lender configuration (the eligibility limits and
 * interest rate each fictional bank/NBFC lends under). PUT is the
 * interesting one here: it's the only write path that must also invalidate
 * the Redis cache (see LenderService.update() -> LenderCacheService.evict()),
 * otherwise a config change wouldn't be visible until the 10-minute TTL expired.
 */
@RestController
@RequestMapping("/api/v1/lenders")
@RequiredArgsConstructor
@Tag(name = "Lenders")
public class LenderController {

    private final LenderService lenderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LenderResponse create(@Valid @RequestBody LenderRequest request) {
        return LenderResponse.from(lenderService.create(request));
    }

    @PutMapping("/{id}")
    public LenderResponse update(@PathVariable Long id, @Valid @RequestBody LenderRequest request) {
        // Full replace, not a partial patch - every field on LenderRequest is required.
        return LenderResponse.from(lenderService.update(id, request));
    }

    @GetMapping("/{id}")
    public LenderResponse getById(@PathVariable Long id) {
        // Note: this always reads straight from Postgres via LenderService/LenderRepository.
        // The Redis-backed cache (LenderCacheService) is only used internally by the
        // eligibility engine, not by this admin-facing read endpoint.
        return LenderResponse.from(lenderService.getById(id));
    }

    @GetMapping
    public List<LenderResponse> getAll() {
        return lenderService.getAll().stream().map(LenderResponse::from).toList();
    }
}
