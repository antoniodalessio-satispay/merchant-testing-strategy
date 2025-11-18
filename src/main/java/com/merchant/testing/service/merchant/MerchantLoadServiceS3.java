package com.merchant.testing.service.merchant;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.merchant.testing.entity.Merchant;
import com.merchant.testing.service.aws.S3StorageService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class MerchantLoadServiceS3 implements MerchantLoadService {
    private final S3StorageService s3StorageService;
    private final ObjectMapper objectMapper;

    public MerchantLoadServiceS3(S3StorageService s3StorageService, ObjectMapper objectMapper) {
        this.s3StorageService = s3StorageService;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Merchant> loadMerchant(String merchantId) {
        return Optional.ofNullable(s3StorageService.downloadFile("merchants/"+merchantId))
                .map(this::parseMerchant);
    }

    @Override
    public Optional<Merchant> loadMerchantByEmail(String email) {
        return Optional.ofNullable(s3StorageService.downloadFile("merchants-email/"+email))
                .map(s -> parseMerchant(s));
    }

    private Merchant parseMerchant(String s) {
        try {
            return objectMapper.readValue(s, Merchant.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
