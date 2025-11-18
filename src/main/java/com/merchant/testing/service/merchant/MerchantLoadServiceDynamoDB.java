package com.merchant.testing.service.merchant;

import com.merchant.testing.domain.enums.BusinessTypeEnum;
import com.merchant.testing.entity.Merchant;
import com.merchant.testing.entity.MerchantBuilder;
import com.merchant.testing.service.aws.DynamoDbService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;
import java.util.Optional;

@Service
class MerchantLoadServiceDynamoDB implements MerchantLoadService {
    private final DynamoDbService dynamoDbService;

    public MerchantLoadServiceDynamoDB(DynamoDbService dynamoDbService) {
        this.dynamoDbService = dynamoDbService;
    }


    @Override
    public Optional<Merchant> loadMerchant(String merchantId) {
        return Optional.ofNullable(dynamoDbService.getItem(merchantId))
                .map(this::toMerchant);

    }

    private Merchant toMerchant(@NotNull Map<String, AttributeValue> stringAttributeValueMap) {
        return MerchantBuilder.aMerchant()
                .withId(Long.parseLong(stringAttributeValueMap.get("id").s()))
                .withName(stringAttributeValueMap.get("name").s())
                .withEmail(stringAttributeValueMap.get("email").s())
                .withBusinessType(BusinessTypeEnum.valueOf(stringAttributeValueMap.get("businessType").s().toUpperCase()))
                .withPhonetics(stringAttributeValueMap.get("phonetics").s())
                .build();
    }

    @Override
    public Optional<Merchant> loadMerchantByEmail(String email) {
        return Optional.ofNullable(dynamoDbService.queryItem(Map.of("email", email)))
                .map(this::toMerchant);
    }
}
