resource "aws_db_subnet_group" "main" {
  name       = "gcateam3-db-subnet-group"
  subnet_ids = [aws_subnet.private.id, aws_subnet.private_secondary.id]

  tags = {
    Name = "GCATeam3-db-subnet-group"
  }
}

resource "aws_db_instance" "main" {
  identifier          = "gcateam3-db"
  allocated_storage   = 20
  storage_type        = "gp2"
  engine              = "mysql"
  engine_version      = "8.0"
  instance_class      = "db.t3.micro"
  db_name             = "dmsproject"
  username            = var.db_username
  password            = var.db_password
  skip_final_snapshot = true

  db_subnet_group_name    = aws_db_subnet_group.main.name
  vpc_security_group_ids  = [aws_security_group.rds_sg.id]
  backup_retention_period = 7
  multi_az                = false

  tags = {
    Name = "GCATeam3-db"
  }
}
