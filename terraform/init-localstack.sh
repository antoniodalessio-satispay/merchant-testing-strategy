#!/bin/bash

# Script to initialize LocalStack with Terraform
# This script applies Terraform configuration to LocalStack

set -e

echo "Initializing Terraform..."
terraform init

echo "Planning Terraform changes..."
terraform plan

echo "Applying Terraform configuration to LocalStack..."
terraform apply -auto-approve

echo "LocalStack resources created successfully!"
echo ""
echo "Resource details:"
terraform output
