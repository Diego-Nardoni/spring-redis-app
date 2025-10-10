#!/bin/bash

set -e

# Configuration
ECR_REPOSITORY="221082174220.dkr.ecr.us-east-1.amazonaws.com/spring-redis-app"
REGION="us-east-1"
CLUSTER_NAME="poc-cluster"
SERVICE_NAME="poc-spring-redis-service"

echo "=== Building and Deploying Spring Redis POC ==="

# Build the application
echo "Building application..."
mvn clean package -DskipTests

# Login to ECR
echo "Logging in to ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY

# Build Docker image
echo "Building Docker image..."
COMMIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "latest")
IMAGE_TAG="${ECR_REPOSITORY}:${COMMIT_HASH}"

docker build -t $IMAGE_TAG .

# Push to ECR
echo "Pushing image to ECR..."
docker push $IMAGE_TAG

# Update ECS service
echo "Updating ECS service..."
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --force-new-deployment \
    --region $REGION

echo "=== Deployment completed successfully ==="
echo "Image: $IMAGE_TAG"
echo "Monitor deployment: aws ecs describe-services --cluster $CLUSTER_NAME --services $SERVICE_NAME --region $REGION"
