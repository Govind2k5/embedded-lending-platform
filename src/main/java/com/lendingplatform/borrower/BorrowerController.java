package com.lendingplatform.borrower;

import com.lendingplatform.borrower.dto.BorrowerRequest;
import com.lendingplatform.borrower.dto.BorrowerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor
@Tag(name = "Borrowers")
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BorrowerResponse create(@Valid @RequestBody BorrowerRequest request) {
        return BorrowerResponse.from(borrowerService.create(request));
    }

    @GetMapping("/{id}")
    public BorrowerResponse getById(@PathVariable Long id) {
        return BorrowerResponse.from(borrowerService.getById(id));
    }

    @GetMapping
    public List<BorrowerResponse> getAll() {
        return borrowerService.getAll().stream().map(BorrowerResponse::from).toList();
    }
}
