# Spring Boot Redis POC

Spring Boot application demonstrating integration with AWS ElastiCache Serverless Redis.

## Features

- Spring Boot 3.1.5 with Java 17
- Redis integration using Lettuce client
- AWS ElastiCache Serverless support
- Spring Security with basic authentication
- Spring Boot Actuator for health checks
- AWS Parameter Store integration
- Docker containerization
- ECS Fargate deployment
- GitOps CI/CD pipeline

## Local Development

```bash
# Build the application
mvn clean package

# Run with Docker
docker build -t spring-redis-app .
docker run -p 8080:8080 spring-redis-app
```

## Deployment

The application is automatically deployed to AWS ECS via GitHub Actions when code is pushed to the `main` branch.

### Prerequisites

- AWS ECS cluster: `poc-cluster`
- ECR repository: `spring-redis-app`
- ElastiCache Serverless Redis cluster
- Parameter Store values:
  - `/poc/redis/endpoint`
  - `/poc/redis/port`
  - `/poc/redis/ssl`

### Health Check

Application health is available at `/actuator/health`

## Environment Profiles

- `serverless`: Production profile for AWS ElastiCache Serverless
- `simple`: Development profile for local Redis
- `resilient`: Profile with enhanced resilience features
