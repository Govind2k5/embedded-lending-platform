package com.lendingplatform.merchant.dto;

import jakarta.validation.constraints.NotBlank;

/** Inbound shape for POST /api/v1/merchants - just a name and an active flag, nothing else to validate. */
public record MerchantRequest(
        @NotBlank String name,
        boolean active
) {
}
