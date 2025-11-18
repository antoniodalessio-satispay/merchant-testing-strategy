package com.merchant.testing.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.localstack.LocalStackContainer;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * AWS configuration for tests using LocalStack.
 * Overrides the production AWS beans with LocalStack-configured clients.
 */
@TestConfiguration
public class LocalStackAwsConfig {

    /**
     * S3 client configured to use LocalStack endpoint.
     */
    @Bean
    @Primary
    public S3Client s3Client(LocalStackContainer localStackContainer) {
        return S3Client.builder()
                .endpointOverride(localStackContainer.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        localStackContainer.getAccessKey(),
                                        localStackContainer.getSecretKey()
                                )
                        )
                )
                .region(Region.of(localStackContainer.getRegion()))
                .build();
    }

    /**
     * DynamoDB client configured to use LocalStack endpoint.
     */
    @Bean
    @Primary
    public DynamoDbClient dynamoDbClient(LocalStackContainer localStackContainer) {
        return DynamoDbClient.builder()
                .endpointOverride(localStackContainer.getEndpoint())
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(
                                        localStackContainer.getAccessKey(),
                                        localStackContainer.getSecretKey()
                                )
                        )
                )
                .region(Region.of(localStackContainer.getRegion()))
                .build();
    }
}
