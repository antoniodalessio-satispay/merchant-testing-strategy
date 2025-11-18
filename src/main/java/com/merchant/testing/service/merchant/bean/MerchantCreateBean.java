package com.merchant.testing.service.merchant.bean;

import com.merchant.testing.domain.enums.BusinessTypeEnum;

public record MerchantCreateBean (String name, String email, BusinessTypeEnum businessType) {
}
