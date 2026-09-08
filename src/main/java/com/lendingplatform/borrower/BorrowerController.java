package com.lendingplatform.borrower;

import com.lendingplatform.borrower.dto.BorrowerRequest;
import com.lendingplatform.borrower.dto.BorrowerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST layer for borrower profiles. Thin on purpose - no business logic
 * lives here, only: accept the HTTP request, validate it, delegate to
 * BorrowerService, and shape the response DTO. This is the
 * Controller -> Service -> Repository layering used in every package.
 */
@RestController // combines @Controller + @ResponseBody: every method's return value is serialized straight to JSON
@RequestMapping("/api/v1/borrowers")
@RequiredArgsConstructor // Lombok generates a constructor for the final field below, which Spring uses for constructor injection
@Tag(name = "Borrowers") // groups these endpoints under "Borrowers" in the generated Swagger UI
public class BorrowerController {

    private final BorrowerService borrowerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201, not the default 200, since this creates a new resource
    public BorrowerResponse create(@Valid @RequestBody BorrowerRequest request) {
        // @Valid triggers Bean Validation on BorrowerRequest's annotations
        // (@NotBlank, @Email, @Min/@Max, etc.) - a failure here never reaches
        // this method body, it's short-circuited into a 400 VALIDATION_ERROR
        // by GlobalExceptionHandler.
        return BorrowerResponse.from(borrowerService.create(request));
    }

    @GetMapping("/{id}")
    public BorrowerResponse getById(@PathVariable Long id) {
        // BorrowerService.getById throws ResourceNotFoundException (-> 404) if missing.
        return BorrowerResponse.from(borrowerService.getById(id));
    }

    @GetMapping
    public List<BorrowerResponse> getAll() {
        return borrowerService.getAll().stream().map(BorrowerResponse::from).toList();
    }
}
