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

## Arquitetura

Abaixo está o diagrama de arquitetura do POC mostrando os componentes principais e como eles se conectam ao ElastiCache Serverless Redis.

![Arquitetura](./arquitetura.png)

_Figura: Diagrama da arquitetura do Spring Boot Redis POC._

## infrastructure as a Prompt (IaP)

<!-- Begin PROMPT.md content -->

````markdown
# 🏗️ PROMPT COMPLETO REUTILIZÁVEL: ARQUITETURA SPRING BOOT + REDIS COM ZERO DOWNTIME E SEGURANÇA ENTERPRISE

Use o MCP Server (Cloud Control API) para provisionar AUTOMATICAMENTE a arquitetura POC na região **[AWS_REGION]**.
MOSTRE o PLANO antes de aplicar, peça confirmação, e só então EXECUTE.
Garanta idempotência (ajustar sem recriar recursos à toa).

## 🔧 IMPLEMENTAÇÃO
- **MÉTODO OBRIGATÓRIO:** Use exclusivamente MCP Server tools
- **PROIBIDO:** Scripts .sh, AWS CLI direto
- **FLUXO:** generate_infrastructure_code() → explain() → create_resource()

**REQUISITOS OBRIGATÓRIOS:**

1. **CHECKLIST EXPLÍCITO**: Antes de começar, crie um checklist numerado de TODOS os serviços/recursos que você vai criar. Marque cada item como ✅ CRIADO ou ❌ PENDENTE conforme avança.

2. **VERIFICAÇÃO APÓS CADA CRIAÇÃO**: Após criar cada recurso, execute imediatamente um comando AWS CLI para confirmar que existe (ex: aws ec2 describe-vpcs, aws ecs list-clusters, etc.) e mostre o resultado.

3. **VALIDAÇÃO FINAL**: Ao terminar, execute comandos de listagem para confirmar que TODOS os recursos do checklist foram criados com sucesso.

**NÃO prossiga para o próximo item do checklist sem antes verificar que o anterior foi criado com sucesso.**

## **📋 PARÂMETROS DE ENTRADA**
```yaml
AWS_ACCOUNT_ID: 221082174220 
AWS_REGION: us-east-1
GitHubOrganization: Diego-Nardoni
GitHubRepository: spring-redis-app
SEU_NOME: Diego-Nardoni
Aplicação: Spring Boot já existente no GitHub
```

## **🔐 1) IDENTIDADE E ACESSO (GitHub Actions OIDC)**
```yaml
OIDC Provider:
  • Issuer: https://token.actions.githubusercontent.com
  • Audience: sts.amazonaws.com
  • Nome: GitHubActionsOIDC

IAM Role: GitHubActionsECRDeployRole
  • Trust Policy: repo:[GitHubOrganization]/[GitHubRepository]:ref:refs/heads/main
  • Managed Policies:
    • AmazonEC2ContainerRegistryPowerUser
    • AmazonSSMReadOnlyAccess
  • Inline Policy: ECS update-service mínima + SSM GetParameter + KMS decrypt
```
## **🛡️ 2) SEGURANÇA ENTERPRISE - IMPLEMENTADO**

### **🚨 ETAPA OBRIGATÓRIA 1 - KMS ENCRYPTION:**
```yaml
KMS Key: poc-encryption-key
  • KeyId: [DYNAMIC_KMS_KEY_ID]  
  • Arn: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]
  • Description: "KMS key for POC encryption at rest"
  • EnableKeyRotation: true  #  Rotação anual automática
  • KeyPolicy:
    - Enable IAM User Permissions: arn:aws:iam::[AWS_ACCOUNT_ID]:root
    - Allow CloudWatch Logs: logs.[AWS_REGION].amazonaws.com
    - Allow EBS: ec2.[AWS_REGION].amazonaws.com
    - Allow Parameter Store: ssm.[AWS_REGION].amazonaws.com
  • Tags: Environment=POC, Project=[GitHubRepository]
```

### **🚨 ETAPA OBRIGATÓRIA 2 - SECURITY SERVICES:**
```yaml
Amazon Inspector:  # 
  • Status: ENABLED
  • ECR Scanning: ENABLED  # ✅ Container vulnerability scanning ativo
  • Enhanced Scanning: ENABLED
  • Scan on Push: ENABLED
  • Integration: Security Hub
  • Command: aws inspector2 enable --account-ids [AWS_ACCOUNT_ID] --resource-types ECR

Amazon GuardDuty:  # 
  • Status: ENABLED
  • Runtime Monitoring: ENABLED  # ✅ ECS Fargate protection ativo
  • ECS Fargate Agent: ENABLED   # ✅ Runtime threat detection ativo
  • Features: CloudTrail, DNS, Flow Logs, S3, RDS, Lambda, EBS Malware
  • Finding Frequency: SIX_HOURS
  • Command: aws guardduty update-detector --features Name=RUNTIME_MONITORING,Status=ENABLED

AWS Security Hub:  # 
  • Status: ENABLED
  • Standards: AWS Foundational Security Best Practices v1.0.0 e v1.2.0
  • Centralized Findings: ENABLED

IAM Access Analyzer:  # 
  • Analyzer: account-security-analyzer
  • Type: ACCOUNT
  • Status: ACTIVE
  • Findings: 0 ativos (3 arquivados como esperados)  # 

Amazon Macie:  # 
  • Status: ENABLED
  • Finding Publishing: FIFTEEN_MINUTES
  • S3 Data Classification: ENABLED

EBS Encryption:  # 
  • Default Encryption: ENABLED  # ✅ Account-wide default ativo
  • KMS Key: poc-encryption-key
  • Command: aws ec2 enable-ebs-encryption-by-default
```

## **🌐 3) NETWORKING AVANÇADO - ORDEM CRÍTICA**

### **🚨 ORDEM OBRIGATÓRIA DE CRIAÇÃO:**
```yaml
ETAPA 1 - VPC Base:
  VPC:
    • CIDR: 10.0.0.0/16
    • Nome: [GitHubRepository]-vpc
    • EnableDnsSupport: true
    • EnableDnsHostnames: true

ETAPA 2 - Subnets:
  Públicas (ALB):
    • [GitHubRepository]-public-subnet-1a: 10.0.1.0/24 ([AWS_REGION]a)
    • [GitHubRepository]-public-subnet-1b: 10.0.2.0/24 ([AWS_REGION]b)
    • [GitHubRepository]-public-subnet-1c: 10.0.3.0/24 ([AWS_REGION]c)

  Privadas (ECS/Redis):
    • [GitHubRepository]-private-subnet-1a: 10.0.11.0/24 ([AWS_REGION]a)
    • [GitHubRepository]-private-subnet-1b: 10.0.12.0/24 ([AWS_REGION]b)
    • [GitHubRepository]-private-subnet-1c: 10.0.13.0/24 ([AWS_REGION]c)

ETAPA 3 - Internet Gateway:
  • Nome: [GitHubRepository]-igw
  • Attach to: [GitHubRepository]-vpc

ETAPA 4 - Route Tables (CRÍTICO - SEPARADOS):
  [GitHubRepository]-public-rt:
    • VPC: [GitHubRepository]-vpc
    • Routes:
      - 10.0.0.0/16 → local
      - 0.0.0.0/0 → [GitHubRepository]-igw

  [GitHubRepository]-private-rt:  # ✅ CRÍTICO: SEPARADO DO PÚBLICO
    • VPC: [GitHubRepository]-vpc
    • Routes:
      - 10.0.0.0/16 → local
      # ✅ SEM ROTA PARA INTERNET - COMUNICAÇÃO 100% PRIVADA

ETAPA 5 - VPC Endpoints (SEM NAT Gateway):
  S3 Gateway Endpoint:
    • Service: com.amazonaws.[AWS_REGION].s3
    • Route Tables: [[GitHubRepository]-public-rt, [GitHubRepository]-private-rt]  # ✅ AMBOS OBRIGATÓRIO
    • Policy: Allow All

  ECR API Interface Endpoint:
    • Service: com.amazonaws.[AWS_REGION].ecr.api
    • Subnets: [[GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c]
    • Security Groups: [GitHubRepository]-sg-endpoints
    • Private DNS: ENABLED

  ECR DKR Interface Endpoint:
    • Service: com.amazonaws.[AWS_REGION].ecr.dkr
    • Subnets: [[GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c]
    • Security Groups: [GitHubRepository]-sg-endpoints
    • Private DNS: ENABLED

  CloudWatch Logs Interface Endpoint:
    • Service: com.amazonaws.[AWS_REGION].logs
    • Subnets: [[GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c]
    • Security Groups: [GitHubRepository]-sg-endpoints
    • Private DNS: ENABLED

  SSM Interface Endpoint:  # ✅ CRÍTICO para Parameter Store
    • Service: com.amazonaws.[AWS_REGION].ssm
    • Subnets: [[GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c]
    • Security Groups: [GitHubRepository]-sg-endpoints
    • Private DNS: ENABLED

ETAPA 6 - Associar Subnets aos Route Tables:
  Subnets Públicas → [GitHubRepository]-public-rt:
    • [GitHubRepository]-public-subnet-1a
    • [GitHubRepository]-public-subnet-1b
    • [GitHubRepository]-public-subnet-1c

  Subnets Privadas → [GitHubRepository]-private-rt:  # ✅ CRÍTICO
    • [GitHubRepository]-private-subnet-1a
    • [GitHubRepository]-private-subnet-1b
    • [GitHubRepository]-private-subnet-1c
```

### **Security Groups:**
```yaml
[GitHubRepository]-sg-alb-cloudfront-only:
  • Ingress: TCP 80 de CloudFront Prefix List (pl-3b927c52)
  • Descrição: "ALB Security Group - CloudFront Only"

[GitHubRepository]-sg-app:
  • Ingress: TCP 8080 de [GitHubRepository]-sg-alb-cloudfront-only (referência por Security Group)
  • Egress: ALL TRAFFIC 0.0.0.0/0 (permite saída para VPC Endpoints)
  • Descrição: "ECS Tasks Security Group"

[GitHubRepository]-sg-endpoints:  # 
  • Ingress: TCP 443 de [GitHubRepository]-sg-app (referência por Security Group)
  • Descrição: "VPC Endpoints Security Group"

[GitHubRepository]-sg-redis:
  • Ingress: TCP 6379 de [GitHubRepository]-sg-app (referência por Security Group)
  • Descrição: "Redis Security Group"
```

### **🚨 VALIDAÇÕES CRÍTICAS PRÉ-ECS:**
```yaml
Antes de criar ECS Service, EXECUTAR:
  1. aws ec2 describe-route-tables --filters "Name=vpc-id,Values=[VPC_ID]"
  2. aws ec2 describe-vpc-endpoints --filters "Name=vpc-id,Values=[VPC_ID]"
  3. Verificar se S3 Gateway está em AMBOS os route tables
  4. Verificar se subnets privadas estão no route table privado
  5. Verificar se todos VPC Endpoints estão "available"
  6. Testar conectividade: aws ecr describe-repositories (via VPC Endpoint)
  7. Verificar Inspector ECR: aws inspector2 batch-get-account-status   
  8. Verificar GuardDuty runtime: aws guardduty get-detector --detector-id [DETECTOR_ID]  
  9. Verificar KMS key: aws kms describe-key --key-id [DYNAMIC_KMS_KEY_ID]   
```

## **📦 4) ECR COM SECURITY SCANNING AVANÇADO**
```yaml
Repositório: [GitHubRepository]
URI: [AWS_ACCOUNT_ID].dkr.ecr.[AWS_REGION].amazonaws.com/[GitHubRepository]
Lifecycle Policy: manter últimas 10 imagens
Scan on Push: true
Encryption: KMS (poc-encryption-key)
Image Tag Mutability: MUTABLE

Security Scanning Configuration:
  • Enhanced Scanning: ENABLED
  • Inspector Integration: ENABLED  # ✅ Container vulnerability scanning
  • Scan Frequency: ON_PUSH
  • Filter Criteria: Critical and High vulnerabilities
  • Continuous Monitoring: ENABLED
  • Security Hub Integration: ENABLED

Security Gate Pipeline:  # ✅ CRÍTICO
  • Block deployment se CVE CRITICAL encontrado
  • Allow deployment apenas com CVE LOW/MEDIUM
  • Notification via SNS para security team
```

## **🔧 5) AWS PARAMETER STORE COM ENCRYPTION**
```yaml
Parameters:  # Todos encrypted com KMS
  /[GitHubRepository]/redis/endpoint:
    • Type: String
    • Value: "DYNAMIC_FROM_ELASTICACHE"  # ✅ Será preenchido após ElastiCache
    • Description: "Redis Serverless endpoint for [GitHubRepository]"
    • KmsKeyId: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]

  /[GitHubRepository]/redis/port:
    • Type: String
    • Value: "6379"
    • Description: "Redis port for [GitHubRepository]"
    • KmsKeyId: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]

  /[GitHubRepository]/redis/ssl:  # ✅ CRÍTICO
    • Type: String
    • Value: "true"
    • Description: "Redis SSL enabled for [GitHubRepository]"
    • KmsKeyId: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]

IAM Policy para ECS Tasks:  # KMS decrypt permission
  • Effect: Allow
  • Action: ssm:GetParameter, ssm:GetParameters, ssm:GetParametersByPath
  • Resource: arn:aws:ssm:[AWS_REGION]:[AWS_ACCOUNT_ID]:parameter/[GitHubRepository]/*
  • KMS Decrypt: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]
```

## **⚙️ 6) ECS FARGATE COM ZERO DOWNTIME, AUTO SCALING E X-RAY**
```yaml
Cluster: [GitHubRepository]-cluster
  • Capacity Providers: FARGATE, FARGATE_SPOT
  • Container Insights: ENABLED  # Métricas detalhadas
  • Execute Command Logging: ENABLED  # Audit trail

Task Definition: [GitHubRepository]-task
  • Family: [GitHubRepository]-task
  • CPU: 512
  • Memory: 1024
  • Network Mode: awsvpc
  • Requires Compatibilities: FARGATE

Container Definitions:  #  X-Ray Tracing
  [GitHubRepository]-app:
    • Name: [GitHubRepository]-app
    • Image: [AWS_ACCOUNT_ID].dkr.ecr.[AWS_REGION].amazonaws.com/[GitHubRepository]:latest
    • Port: 8080
    • Environment Variables:  # ✅ X-RAY VARIABLES ADDED
      • SPRING_PROFILES_ACTIVE: "production"
      • _X_AMZN_TRACE_ID: ""
      • AWS_XRAY_TRACING_NAME: "[GitHubRepository]"
      • AWS_XRAY_DAEMON_ADDRESS: "xray-daemon:2000"
    • Log Configuration:  # Encrypted logs
      • Driver: awslogs
      • Group: /ecs/[GitHubRepository]-task
      • Region: [AWS_REGION]
      • Stream Prefix: ecs
      • KmsKeyId: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]
    • Health Check:  # ✅ ZERO DOWNTIME - OTIMIZADO
      • Command: ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health/readiness || exit 1"]
      • Interval: 15
      • Timeout: 5
      • Retries: 2
      • Start Period: 60
    • Depends On:  # ✅ X-RAY DEPENDENCY
      • Container Name: xray-daemon
      • Condition: START

  xray-daemon:  # X-Ray Sidecar
    • Name: xray-daemon
    • Image: public.ecr.aws/xray/aws-xray-daemon:latest  # ✅ ECR público para VPC compatibility
    • CPU: 32
    • Memory Reservation: 256
    • Port Mappings:
      • Container Port: 2000
      • Protocol: udp
    • Essential: true
    • Environment:
      • AWS_REGION: [AWS_REGION]
    • Log Configuration:
      • Driver: awslogs
      • Group: /ecs/[GitHubRepository]-task
      • Region: [AWS_REGION]
      • Stream Prefix: xray

Task Role: ecsTaskRole  # X-Ray permissions added
  • Managed Policies:
    • AWSXRayDaemonWriteAccess  # X-Ray tracing permissions
  • Inline Policy:
    • Effect: Allow
    • Action: ssm:GetParameter, ssm:GetParameters, ssm:GetParametersByPath
    • Resource: arn:aws:ssm:[AWS_REGION]:[AWS_ACCOUNT_ID]:parameter/[GitHubRepository]/*
    • KMS Decrypt: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]

Service: [GitHubRepository]-service 
  • Cluster: [GitHubRepository]-cluster
  • Task Definition: [GitHubRepository]-task:LATEST
  • Desired Count: 3  # ✅ MUDANÇA: agora controlado por auto scaling
  • Launch Type: FARGATE
  • Platform Version: LATEST
  • Network Configuration:
    • Subnets: [GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c
    • Security Groups: [GitHubRepository]-sg-app
    • Assign Public IP: DISABLED
  • Load Balancer:
    • Target Group: [GitHubRepository]-tg
    • Container: [GitHubRepository]-app
    • Port: 8080
  • Deployment Configuration:  # ✅ ZERO DOWNTIME - CRÍTICO
    • Maximum Percent: 200           # ✅ Permite dobrar containers temporariamente
    • Minimum Healthy Percent: 0    # ✅ Zero downtime deployment
    • Circuit Breaker: enabled com rollback  # Proteção automática
    • Deployment Controller: ECS (Rolling Update)
  • Health Check Grace Period: 300 segundos
  • Capacity Provider Strategy:
    • FARGATE_SPOT: weight 2, base 0
    • FARGATE: weight 1, base 1
  • Runtime Monitoring: ENABLED  # GuardDuty integration

Auto Scaling Configuration:  #  Multi-métrica
  Scalable Target:
    • Service Namespace: ecs
    • Resource ID: service/[GitHubRepository]-cluster/[GitHubRepository]-service
    • Scalable Dimension: ecs:service:DesiredCount
    • Min Capacity: 2
    • Max Capacity: 15

  Scaling Policies:  # 3 políticas ativas
    CPU Utilization Policy:
      • Target Value: 75%
      • Scale Out Cooldown: 180s
      • Scale In Cooldown: 300s
      • Metric: ECSServiceAverageCPUUtilization

    ALB Request Count Policy: 
      • Target Value: 1000 requests/target
      • Scale Out Cooldown: 180s
      • Scale In Cooldown: 300s
      • Metric: ALBRequestCountPerTarget

    Memory Utilization Policy:  
      • Target Value: 80%
      • Scale Out Cooldown: 180s
      • Scale In Cooldown: 300s
      • Metric: ECSServiceAverageMemoryUtilization

  CloudWatch Alarms:  # 6 alarmes automáticos
    • CPU High/Low alarms
    • ALB Request Count High/Low alarms
    • Memory High/Low alarms

Roles:
  • Task Role: ecsTaskRole (existente com Parameter Store access + KMS decrypt + X-Ray)
  • Execution Role: ecsTaskExecutionRole (AWS managed + KMS decrypt)
```

## **🔄 7) APPLICATION LOAD BALANCER COM ZERO DOWNTIME**
```yaml
ALB: [GitHubRepository]-alb
  • Scheme: internet-facing
  • Type: application
  • IP Address Type: ipv4
  • Subnets: [GitHubRepository]-public-subnet-1a, [GitHubRepository]-public-subnet-1b, [GitHubRepository]-public-subnet-1c
```

<!-- End PROMPT.md content -->

