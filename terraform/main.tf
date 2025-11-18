terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region                      = var.aws_region
  access_key                  = var.aws_access_key
  secret_key                  = var.aws_secret_key
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  endpoints {
    s3       = var.localstack_endpoint
    dynamodb = var.localstack_endpoint
  }
}

# S3 Bucket for merchant data storage
resource "aws_s3_bucket" "merchant_bucket" {
  bucket = var.s3_bucket_name

  tags = {
    Name        = "Merchant Bucket"
    Environment = var.environment
  }
}

# S3 Bucket versioning
resource "aws_s3_bucket_versioning" "merchant_bucket_versioning" {
  bucket = aws_s3_bucket.merchant_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

# DynamoDB Table for merchant metadata
resource "aws_dynamodb_table" "merchant_table" {
  name           = var.dynamodb_table_name
  billing_mode   = "PAY_PER_REQUEST"
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "merchantName"
    type = "S"
  }

  global_secondary_index {
    name            = "MerchantNameIndex"
    hash_key        = "merchantName"
    projection_type = "ALL"
  }

  tags = {
    Name        = "Merchant Table"
    Environment = var.environment
  }
}
