package com.lendingplatform.lender;

import com.lendingplatform.lender.dto.LenderRequest;
import com.lendingplatform.lender.dto.LenderResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return LenderResponse.from(lenderService.update(id, request));
    }

    @GetMapping("/{id}")
    public LenderResponse getById(@PathVariable Long id) {
        return LenderResponse.from(lenderService.getById(id));
    }

    @GetMapping
    public List<LenderResponse> getAll() {
        return lenderService.getAll().stream().map(LenderResponse::from).toList();
    }
}
