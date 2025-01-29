# ECR Repository details
$REGION="us-west-2"
$ACCOUNT_ID="526401996416"
$FRONTEND_REPO="gcateam3-frontend"
$BACKEND_REPO="gcateam3-backend"

# Local image names (update these with your local image names)
$LOCAL_FRONTEND_IMAGE="gcateam3-frontend:latest"
$LOCAL_BACKEND_IMAGE="gcateam3-backend:latest"

# ECR Repository URIs
$FRONTEND_ECR_URI="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$FRONTEND_REPO"
$BACKEND_ECR_URI="$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com/$BACKEND_REPO"

Write-Host "Logging in to Amazon ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin "$ACCOUNT_ID.dkr.ecr.$REGION.amazonaws.com"

Write-Host "Tagging frontend image..."
docker tag $LOCAL_FRONTEND_IMAGE "$FRONTEND_ECR_URI:latest"

Write-Host "Pushing frontend image..."
docker push "$FRONTEND_ECR_URI:latest"

Write-Host "Tagging backend image..."
docker tag $LOCAL_BACKEND_IMAGE "$BACKEND_ECR_URI:latest"

Write-Host "Pushing backend image..."
docker push "$BACKEND_ECR_URI:latest"

Write-Host "Done!"
