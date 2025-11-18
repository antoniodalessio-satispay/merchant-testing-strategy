# Terraform Configuration for LocalStack

This directory contains Terraform configuration to create AWS resources (S3 bucket and DynamoDB table) in LocalStack for testing purposes.

## Prerequisites

- [Terraform](https://www.terraform.io/downloads.html) (>= 1.0)
- [LocalStack](https://docs.localstack.cloud/getting-started/installation/) running on `http://localhost:4566`

## Quick Start with LocalStack

### 1. Start LocalStack

Using Docker:
```bash
docker run -d --rm -p 4566:4566 -p 4510-4559:4510-4559 localstack/localstack:3.0
```

Or using Docker Compose (create `docker-compose.yml` in project root):
```yaml
version: '3.8'
services:
  localstack:
    image: localstack/localstack:3.0
    ports:
      - "4566:4566"
    environment:
      - SERVICES=s3,dynamodb
      - DEBUG=1
```

Then run:
```bash
docker-compose up -d
```

### 2. Apply Terraform Configuration

Using the initialization script:
```bash
cd terraform
./init-localstack.sh
```

Or manually:
```bash
cd terraform
terraform init
terraform plan
terraform apply
```

### 3. Verify Resources

Check S3 bucket:
```bash
aws --endpoint-url=http://localhost:4566 s3 ls
```

Check DynamoDB table:
```bash
aws --endpoint-url=http://localhost:4566 dynamodb list-tables
```

## Resources Created

- **S3 Bucket**: `merchant-bucket` (with versioning enabled)
- **DynamoDB Table**: `merchant-table` with:
  - Primary key: `id` (String)
  - Global Secondary Index: `MerchantNameIndex` on `merchantName`

## Configuration

Default configuration uses LocalStack. To customize:

1. Copy the example variables file:
   ```bash
   cp terraform.tfvars.example terraform.tfvars
   ```

2. Edit `terraform.tfvars` with your values

## Clean Up

To destroy all resources:
```bash
terraform destroy
```

## Using with Testcontainers

When using Testcontainers in your tests, the LocalStack container will be started automatically. You can then use this Terraform configuration to create resources programmatically in your test setup.

Example:
```java
// In your test setup
LocalStackContainer localstack = new LocalStackContainer(...);
localstack.start();

// Execute terraform to create resources
// (You would typically do this in a @BeforeAll method)
```
