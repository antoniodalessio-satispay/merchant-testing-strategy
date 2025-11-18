package com.merchant.testing.service.merchant;

import com.merchant.testing.entity.Merchant;
import com.merchant.testing.repository.MerchantRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class MerchantLoadServicePostgres implements MerchantLoadService {
    private final MerchantRepository merchantRepository;

    public MerchantLoadServicePostgres(MerchantRepository merchantRepository) {
        this.merchantRepository = merchantRepository;
    }

    @Override
    public Optional<Merchant> loadMerchant(String merchantId) {
        return merchantRepository.findById(Long.parseLong(merchantId));
    }

    @Override
    public Optional<Merchant> loadMerchantByEmail(String email) {
        return merchantRepository.findByEmail(email);
    }
}
