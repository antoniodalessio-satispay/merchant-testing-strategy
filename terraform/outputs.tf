output "s3_bucket_name" {
  description = "Name of the created S3 bucket"
  value       = aws_s3_bucket.merchant_bucket.id
}

output "s3_bucket_arn" {
  description = "ARN of the created S3 bucket"
  value       = aws_s3_bucket.merchant_bucket.arn
}

output "dynamodb_table_name" {
  description = "Name of the created DynamoDB table"
  value       = aws_dynamodb_table.merchant_table.id
}

output "dynamodb_table_arn" {
  description = "ARN of the created DynamoDB table"
  value       = aws_dynamodb_table.merchant_table.arn
}
