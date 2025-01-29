# Get AWS account ID
$accountId = aws sts get-caller-identity --query "Account" --output text

# Set AWS region
$region = "us-east-2"

# ECR login
Write-Host "Logging in to Amazon ECR..."
aws ecr get-login-password --region $region | docker login --username AWS --password-stdin "$accountId.dkr.ecr.$region.amazonaws.com"

# Function to build and push image
function Build-And-Push-Image {
    param (
        [string]$serviceName,
        [string]$dockerfilePath
    )
    
    Write-Host "`nProcessing $serviceName..."
    $repositoryUri = "$accountId.dkr.ecr.$region.amazonaws.com/gcateam3-$serviceName"
    
    # Build the Docker image
    Write-Host "Building $serviceName image..."
    docker build -t "gcateam3-$serviceName" $dockerfilePath
    
    # Tag the image
    Write-Host "Tagging $serviceName image..."
    docker tag "gcateam3-$serviceName`:latest" "$repositoryUri`:latest"
    
    # Push the image
    Write-Host "Pushing $serviceName image to ECR..."
    docker push "$repositoryUri`:latest"
}

# Build and push frontend
Build-And-Push-Image -serviceName "frontend" -dockerfilePath "./frontend"

# Build and push backend
Build-And-Push-Image -serviceName "backend" -dockerfilePath "./backend"

Write-Host "`nAll images have been pushed to ECR successfully!"

# Display the pushed images
Write-Host "`nVerifying pushed images..."
Write-Host "`nFrontend repository images:"
aws ecr describe-images --repository-name gcateam3-frontend
Write-Host "`nBackend repository images:"
aws ecr describe-images --repository-name gcateam3-backend
