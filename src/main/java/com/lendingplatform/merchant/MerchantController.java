package com.lendingplatform.merchant;

import com.lendingplatform.merchant.dto.MerchantRequest;
import com.lendingplatform.merchant.dto.MerchantResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchants")
public class MerchantController {

    private final MerchantService merchantService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MerchantResponse create(@Valid @RequestBody MerchantRequest request) {
        return MerchantResponse.from(merchantService.create(request));
    }

    @GetMapping("/{id}")
    public MerchantResponse getById(@PathVariable Long id) {
        return MerchantResponse.from(merchantService.getById(id));
    }

    @GetMapping
    public List<MerchantResponse> getAll() {
        return merchantService.getAll().stream().map(MerchantResponse::from).toList();
    }
}
