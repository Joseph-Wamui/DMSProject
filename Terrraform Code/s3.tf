resource "aws_s3_bucket" "document_storage" {
  bucket        = "gcateam3-document-storage"
  force_destroy = true # This allows the bucket to be deleted even if it contains objects

  tags = {
    Name = "GCATeam3-document-storage"
  }
}

resource "aws_s3_bucket_public_access_block" "document_storage" {
  bucket = aws_s3_bucket.document_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_versioning" "document_storage" {
  bucket = aws_s3_bucket.document_storage.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "document_storage" {
  bucket = aws_s3_bucket.document_storage.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}
