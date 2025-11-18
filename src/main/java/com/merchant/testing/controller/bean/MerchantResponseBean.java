package com.merchant.testing.controller.bean;

import com.merchant.testing.domain.enums.BusinessTypeEnum;
import com.merchant.testing.entity.Merchant;

public record MerchantResponseBean(String id,
                                   BusinessTypeEnum businessType,
                                   String email,
                                   String name) {

    public static MerchantResponseBean from(Merchant merchant) {
        if (merchant == null) {
            throw new IllegalArgumentException("merchant must not be null");
        }
        if (merchant.getId() == null) {
            throw new IllegalArgumentException("merchant id must not be null");
        }
        return new MerchantResponseBean(
                merchant.getId().toString(),
                merchant.getBusinessType(),
                merchant.getEmail(),
                merchant.getName()
        );
    }
}
