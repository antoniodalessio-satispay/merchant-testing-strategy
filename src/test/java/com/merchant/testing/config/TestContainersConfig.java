package com.merchant.testing.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.utility.DockerImageName;

import static org.testcontainers.containers.localstack.LocalStackContainer.Service.*;

/**
 * Base Testcontainers configuration for integration tests.
 * This configuration can be imported in test classes using @Import(TestContainersConfig.class)
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    /**
     * PostgreSQL container with automatic Spring Boot connection configuration.
     * The @ServiceConnection annotation automatically configures the datasource properties.
     */
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("merchant_test_db")
                .withUsername("test_user")
                .withPassword("test_pass")
                .withReuse(true); // Reuse container across test executions for faster tests
    }

    /**
     * LocalStack container for AWS service emulation.
     * Provides S3, DynamoDB, and other AWS services locally.
     */
    @Bean
    public LocalStackContainer localStackContainer() {
        return new LocalStackContainer(DockerImageName.parse("localstack/localstack:3.0"))
                .withServices(S3, DYNAMODB)
                .withReuse(true); // Reuse container across test executions
    }
}
