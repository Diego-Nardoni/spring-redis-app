#!/bin/bash

set -e

# Configurações
ECR_REPOSITORY="221082174220.dkr.ecr.us-east-1.amazonaws.com/spring-redis-poc"
CLUSTER_NAME="poc-cluster"
SERVICE_NAME="spring-redis-poc-service"
TASK_DEFINITION_FAMILY="spring-redis-poc"
REGION="us-east-1"

echo "=== Iniciando deploy da aplicação Spring Redis POC ==="

# 1. Build da aplicação
echo "1. Compilando aplicação..."
mvn clean package -DskipTests

# 2. Login no ECR
echo "2. Fazendo login no ECR..."
aws ecr get-login-password --region $REGION | docker login --username AWS --password-stdin $ECR_REPOSITORY

# 3. Build da imagem Docker
echo "3. Construindo imagem Docker..."
docker build -t spring-redis-poc .
docker tag spring-redis-poc:latest $ECR_REPOSITORY:latest

# 4. Push da imagem
echo "4. Enviando imagem para ECR..."
docker push $ECR_REPOSITORY:latest

# 5. Registrar nova task definition
echo "5. Registrando nova task definition..."
aws ecs register-task-definition \
    --cli-input-json file://task-definition.json \
    --region $REGION

# 6. Atualizar serviço ECS
echo "6. Atualizando serviço ECS..."
aws ecs update-service \
    --cluster $CLUSTER_NAME \
    --service $SERVICE_NAME \
    --task-definition $TASK_DEFINITION_FAMILY \
    --force-new-deployment \
    --region $REGION

# 7. Aguardar deployment
echo "7. Aguardando deployment..."
aws ecs wait services-stable \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --region $REGION

echo "=== Deploy concluído com sucesso! ==="

# 8. Mostrar status do serviço
echo "8. Status do serviço:"
aws ecs describe-services \
    --cluster $CLUSTER_NAME \
    --services $SERVICE_NAME \
    --region $REGION \
    --query 'services[0].{Status:status,Running:runningCount,Desired:desiredCount,TaskDefinition:taskDefinition}'
