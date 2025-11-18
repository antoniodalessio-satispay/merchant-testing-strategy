package com.merchant.testing.service.merchant;

import com.merchant.testing.entity.Merchant;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class MerchantLoadServiceStrategy implements MerchantLoadService {
    private final MerchantLoadServiceS3 merchantLoadServiceS3;
    private final MerchantLoadServicePostgres merchantLoadServicePostgres;
    private final MerchantLoadServiceDynamoDB merchantLoadServiceDynamoDB;

    public MerchantLoadServiceStrategy(MerchantLoadServiceS3 merchantLoadServiceS3, MerchantLoadServicePostgres merchantLoadServicePostgres, MerchantLoadServiceDynamoDB merchantLoadServiceDynamoDB) {
        this.merchantLoadServiceS3 = merchantLoadServiceS3;
        this.merchantLoadServicePostgres = merchantLoadServicePostgres;
        this.merchantLoadServiceDynamoDB = merchantLoadServiceDynamoDB;
    }

    @Override
    public Optional<Merchant> loadMerchant(String merchantId) {
        return merchantLoadServiceDynamoDB.loadMerchant(merchantId)
                .or(() -> merchantLoadServicePostgres.loadMerchant(merchantId))
                .or(() -> merchantLoadServiceS3.loadMerchant(merchantId));
    }

    @Override
    public Optional<Merchant> loadMerchantByEmail(String email) {
        return merchantLoadServiceDynamoDB.loadMerchantByEmail(email)
                .or(() -> merchantLoadServicePostgres.loadMerchantByEmail(email))
                .or(() -> merchantLoadServiceS3.loadMerchantByEmail(email));
    }
}
