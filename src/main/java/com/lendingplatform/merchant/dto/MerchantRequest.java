package com.lendingplatform.merchant.dto;

import jakarta.validation.constraints.NotBlank;

public record MerchantRequest(
        @NotBlank String name,
        boolean active
) {
}
