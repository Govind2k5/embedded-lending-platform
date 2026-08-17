package com.lendingplatform.merchant.dto;

import com.lendingplatform.merchant.Merchant;

import java.time.Instant;

public record MerchantResponse(
        Long id,
        String name,
        boolean active,
        Instant createdAt
) {
    public static MerchantResponse from(Merchant merchant) {
        return new MerchantResponse(merchant.getId(), merchant.getName(), merchant.isActive(), merchant.getCreatedAt());
    }
}
