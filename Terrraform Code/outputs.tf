output "alb_dns_name" {
  description = "The DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "frontend_url" {
  description = "The URL for the frontend application"
  value       = "http://${aws_lb.main.dns_name}"
}

output "backend_url" {
  description = "The URL for the backend API"
  value       = "http://${aws_lb.main.dns_name}/api"
}

output "rds_endpoint" {
  description = "The endpoint of the RDS instance"
  value       = aws_db_instance.main.endpoint
}
