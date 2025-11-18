variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

variable "aws_access_key" {
  description = "AWS access key (for LocalStack use 'test')"
  type        = string
  default     = "test"
}

variable "aws_secret_key" {
  description = "AWS secret key (for LocalStack use 'test')"
  type        = string
  default     = "test"
}

variable "localstack_endpoint" {
  description = "LocalStack endpoint URL"
  type        = string
  default     = "http://localhost:4566"
}

variable "s3_bucket_name" {
  description = "Name of the S3 bucket for merchant data"
  type        = string
  default     = "merchant-bucket"
}

variable "dynamodb_table_name" {
  description = "Name of the DynamoDB table for merchant metadata"
  type        = string
  default     = "merchant-table"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "test"
}
