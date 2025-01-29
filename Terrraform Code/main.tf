provider "aws" {
  region = "us-east-2" # Changed to us-east-2

  default_tags {
    tags = {
      Environment = "Development"
      Project     = "GCATeam3"
      Terraform   = "true"
    }
  }
}



terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    awsutils = {
      source = "cloudposse/awsutils"
    }
  }
}