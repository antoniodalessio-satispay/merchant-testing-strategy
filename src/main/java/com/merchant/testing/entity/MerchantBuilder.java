package com.merchant.testing.entity;

import com.merchant.testing.domain.enums.BusinessTypeEnum;

import java.time.LocalDateTime;

public final class MerchantBuilder {
    private Long id;
    private String name;
    private String email;
    private BusinessTypeEnum businessType;
    private String phonetics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private MerchantBuilder() {
    }

    public static MerchantBuilder aMerchant() {
        return new MerchantBuilder();
    }

    public MerchantBuilder withId(Long id) {
        this.id = id;
        return this;
    }

    public MerchantBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public MerchantBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public MerchantBuilder withBusinessType(BusinessTypeEnum businessType) {
        this.businessType = businessType;
        return this;
    }

    public MerchantBuilder withPhonetics(String phonetics) {
        this.phonetics = phonetics;
        return this;
    }

    public MerchantBuilder withCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public MerchantBuilder withUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public Merchant build() {
        Merchant merchant = new Merchant();
        merchant.setId(id);
        merchant.setName(name);
        merchant.setEmail(email);
        merchant.setBusinessType(businessType);
        merchant.setPhonetics(phonetics);
        merchant.setCreatedAt(createdAt);
        merchant.setUpdatedAt(updatedAt);
        return merchant;
    }
}
