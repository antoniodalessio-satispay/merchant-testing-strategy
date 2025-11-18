# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Spring Boot application demonstrating testing strategies with different Spring contexts. The application manages merchant data and integrates with external services (PostgreSQL, AWS S3/DynamoDB, external APIs via HTTP).

**Purpose**: This project showcases how to test Spring Boot applications using different context configurations, including database integration, AWS services, and HTTP clients.

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Java Version**: 21 (LTS) - **Required**
- **Build Tool**: Maven
- **Database**: PostgreSQL with JPA/Hibernate
- **Database Migrations**: Flyway
- **AWS SDK**: v2 (S3 and DynamoDB clients)
- **HTTP Client**: OkHttp 4.12.0
- **Testing**: Testcontainers (PostgreSQL, LocalStack for AWS services)

### Java Version Setup

This project requires Java 21. If you have multiple Java versions installed:

**Using SDKMAN** (recommended):
```bash
# List installed versions
sdk list java

# Install Java 21 if not present
sdk install java 21.0.7-amzn

# Use Java 21 for current session
sdk use java 21.0.7-amzn

# Set Java 21 as default
sdk default java 21.0.7-amzn
```

**Using JAVA_HOME** (macOS):
```bash
# List available Java versions
/usr/libexec/java_home -V

# Set JAVA_HOME for current session
export JAVA_HOME=$(/usr/libexec/java_home -v 21)

# Verify Java version
java -version
```

**Temporary override for Maven**:
```bash
# Run Maven with specific Java version
JAVA_HOME=/path/to/java21 mvn clean install
```

## Build and Run Commands

### Build the project
```bash
mvn clean install
```

### Run the application
```bash
mvn spring-boot:run
```

### Run tests
```bash
mvn test
```

### Run a single test class
```bash
mvn test -Dtest=ClassName
```

### Run a specific test method
```bash
mvn test -Dtest=ClassName#methodName
```

## Application Architecture

### Package Structure
- `com.merchant.testing.entity` - JPA entities (Merchant)
- `com.merchant.testing.repository` - Spring Data JPA repositories
- `com.merchant.testing.service` - Business logic layer with external integrations
- `com.merchant.testing.controller` - REST API endpoints
- `com.merchant.testing.config` - Spring configuration classes (AWS, HTTP client)

### Key Components

**Database Layer**:
- `Merchant` entity with PostgreSQL persistence
- `MerchantRepository` using Spring Data JPA

**AWS Integration**:
- `AwsConfig`: Configures S3Client and DynamoDbClient beans
- `S3StorageService`: File upload/download operations
- `DynamoDbService`: NoSQL data operations

**HTTP Client**:
- `HttpClientConfig`: Configures OkHttpClient bean with timeouts
- `ExternalApiService`: Makes HTTP GET/POST requests to external APIs

**Business Logic**:
- `MerchantService`: Orchestrates merchant operations across database, AWS, and external APIs
- `MerchantLoadServiceStrategy`: Strategy pattern implementation that queries multiple data sources (DynamoDB → PostgreSQL → S3) in order
- `MerchantPhoneticService`: Enriches merchant names with phonetic data from external dictionary API

### Configuration

The application is configured via `src/main/resources/application.properties`:
- Database connection (PostgreSQL)
- Flyway migrations (enabled, baseline on migrate)
- JPA/Hibernate (ddl-auto set to `validate` - schema managed by Flyway)
- AWS region and resource names (S3 bucket, DynamoDB table)
- External API base URL
- Server port

### Database Migrations with Flyway

The project uses Flyway for database schema version control:

**Migration Files Location**: `src/main/resources/db/migration/`

**Naming Convention**: `V{version}__{description}.sql`
- Example: `V1__Create_merchants_table.sql`

**Key Configuration**:
- `spring.jpa.hibernate.ddl-auto=validate` - Hibernate only validates, doesn't create/update schema
- `spring.flyway.baseline-on-migrate=true` - Allows Flyway to work with existing databases
- Flyway runs automatically on application startup

**Creating New Migrations**:
```bash
# Create a new migration file with incremented version number
# Example: V2__Add_merchant_status_column.sql
touch src/main/resources/db/migration/V2__Add_merchant_status_column.sql
```

**Flyway Commands** (via Maven):
```bash
# Get migration status
mvn flyway:info

# Migrate to latest version
mvn flyway:migrate

# Clean database (WARNING: drops all objects)
mvn flyway:clean

# Repair migration history
mvn flyway:repair
```

**Existing Migrations**:
- `V1__Create_merchants_table.sql`: Initial schema with merchants table including:
  - id (BIGSERIAL, primary key)
  - name, email (required, email is unique)
  - business_type (VARCHAR, enum values: SMALL, MEDIUM, LARGE)
  - phonetics (VARCHAR, optional)
  - created_at, updated_at (TIMESTAMP with defaults)
  - Indexes on email and business_type

## Testing Strategy

This application is designed to demonstrate different testing contexts:

1. **Database Testing**: Use Testcontainers with PostgreSQL container for repository and service tests
2. **AWS Service Testing**: Use Testcontainers with LocalStack for S3 and DynamoDB integration tests
3. **HTTP Client Testing**: Mock OkHttpClient or use WireMock for external API testing
4. **Integration Testing**: Use `@SpringBootTest` with full context or sliced contexts (`@WebMvcTest`, `@DataJpaTest`)

Key dependencies already included:
- `testcontainers` (core)
- `testcontainers-postgresql`
- `testcontainers-localstack`
- `spring-boot-starter-test`

### Test Configuration

**Test Properties** (`src/test/resources/application-test.properties`):
- `spring.jpa.hibernate.ddl-auto=create-drop` - Tests use in-memory schema (different from production's `validate`)
- Debug logging enabled for `com.merchant.testing` package
- Test-specific AWS resource names (test-merchant-bucket, test-merchant-table)
- LocalStack endpoints override production AWS configuration

**Test Containers Configuration** (`src/test/java/com/merchant/testing/config/`):

**TestContainersConfig**: Base configuration with PostgreSQL and LocalStack containers
- PostgreSQL container uses `@ServiceConnection` for automatic Spring Boot configuration
- LocalStack container provides S3 and DynamoDB services
- Both containers use `withReuse(true)` for faster test execution
- Containers are shared across test classes for performance

**LocalStackAwsConfig**: AWS client configuration for tests
- Overrides production AWS beans with LocalStack-configured clients
- Uses `@Primary` annotation to take precedence in test context
- Configures both S3Client and DynamoDbClient with LocalStack endpoints

**Usage in tests**:
```java
@SpringBootTest
@Import({TestContainersConfig.class, LocalStackAwsConfig.class})
@ActiveProfiles("test")
class MyIntegrationTest {
    // Test implementation
    // PostgreSQL and LocalStack containers start automatically
    // No need to manage container lifecycle manually
}
```

**Important Notes**:
- Testcontainers automatically starts containers when tests run (no manual setup needed)
- Container reuse requires Docker and reduces test startup time
- For AWS integration tests, `LocalStackAwsConfig` must be imported to override production beans
- Use `@ActiveProfiles("test")` to load test-specific properties

### Terraform Infrastructure

The `terraform/` directory contains infrastructure-as-code for creating AWS resources in LocalStack.

**When to use Terraform**:
- Manual testing with a standalone LocalStack instance
- Creating AWS resources outside of automated tests
- Sharing consistent infrastructure setup across team

**When Terraform is NOT needed**:
- Running automated tests (Testcontainers manages LocalStack and resources)
- Integration tests create their own resources dynamically

**Resources created**:
- S3 bucket: `merchant-bucket` (with versioning enabled)
- DynamoDB table: `merchant-table` with:
  - Primary key: `id` (String)
  - Global Secondary Index: `MerchantNameIndex` on `merchantName`

**Setup LocalStack with Terraform** (for manual testing):
```bash
# Start LocalStack
docker run -d --rm -p 4566:4566 localstack/localstack:3.0

# Apply Terraform configuration
cd terraform
./init-localstack.sh
```

**Verify resources**:
```bash
# Check S3 bucket
aws --endpoint-url=http://localhost:4566 s3 ls

# Check DynamoDB table
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

See `terraform/README.md` for detailed documentation.