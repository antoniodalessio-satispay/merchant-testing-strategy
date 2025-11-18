package com.merchant.testing.service.merchant;

import com.merchant.testing.entity.Merchant;

import java.util.Optional;

public interface MerchantLoadService {
    Optional<Merchant> loadMerchant(String merchantId);
    Optional<Merchant> loadMerchantByEmail(String email);
}
