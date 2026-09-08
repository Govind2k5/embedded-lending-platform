package com.lendingplatform.merchant;

import com.lendingplatform.common.exception.ResourceNotFoundException;
import com.lendingplatform.merchant.dto.MerchantRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** Business logic for merchants. LoanApplicationService.create() calls getById() to confirm a merchantId is real before creating an application. */
@Service
@RequiredArgsConstructor
public class MerchantService {

    private final MerchantRepository merchantRepository;

    public Merchant create(MerchantRequest request) {
        Merchant merchant = Merchant.builder()
                .name(request.name())
                .active(request.active())
                .build();
        return merchantRepository.save(merchant);
    }

    public Merchant getById(Long id) {
        return merchantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Merchant not found: " + id));
    }

    public List<Merchant> getAll() {
        return merchantRepository.findAll();
    }
}
