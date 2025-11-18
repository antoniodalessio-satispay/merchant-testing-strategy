package com.merchant.testing.service.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.HashMap;
import java.util.Map;

@Service
public class DynamoDbService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table}")
    private String tableName;

    public DynamoDbService(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    public void putItem(String id, String merchantName) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("id", AttributeValue.builder().s(id).build());
        item.put("merchantName", AttributeValue.builder().s(merchantName).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
    }
    public Map<String, AttributeValue> queryItem(Map<String, String> filter) {
        Map<String, AttributeValue> item = new HashMap<>();
        for (Map.Entry<String, String> entry : filter.entrySet()) {
            item.put(entry.getKey(), AttributeValue.builder().s(entry.getValue()).build());
        }
        return item;
    }
    public Map<String, AttributeValue> getItem(String id) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", AttributeValue.builder().s(id).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);
        return response.item();
    }
}
