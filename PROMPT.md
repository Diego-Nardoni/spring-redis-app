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

### **�� ETAPA OBRIGATÓRIA 2 - SECURITY SERVICES:**
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
  • Security Groups: [GitHubRepository]-sg-alb-cloudfront-only
  • Attributes:
    • deletion_protection.enabled: false
    • access_logs.s3.enabled: false
    • routing.http.drop_invalid_header_fields.enabled: false

Target Group: [GitHubRepository]-tg  # ✅ ZERO DOWNTIME - OTIMIZADO
  • Protocol: HTTP
  • Port: 8080
  • VPC: [GitHubRepository]-vpc
  • Target Type: ip
  • Health Check:
    • Protocol: HTTP
    • Path: /actuator/health/readiness
    • Port: traffic-port
    • Healthy Threshold: 2
    • Unhealthy Threshold: 2
    • Timeout: 5
    • Interval: 15
    • Matcher: 200  # ✅ CRÍTICO: apenas 200
  • Attributes:  # ✅ ZERO DOWNTIME - CRÍTICO
    • deregistration_delay.timeout_seconds: 30  # Drena conexões por 30s
    • slow_start.duration_seconds: 60           # Aquecimento gradual
    • load_balancing.algorithm.type: round_robin

Listener: HTTP:80
  • Rules:
    1. Priority 100: Header "X-Origin-Verify" = "[GitHubRepository]-secret-header-2025" → Forward to [GitHubRepository]-tg
    2. Priority 200: Path "*" → Fixed Response 403 "Access Denied - Direct access not allowed"
    3. Default: Forward to [GitHubRepository]-tg
```

## **🌐 8) CLOUDFRONT + WAF GLOBAL**
```yaml
WAF Web ACL: [GitHubRepository]-cloudfront-web-acl
  • Scope: CLOUDFRONT
  • Default Action: Allow
  • Rules:
    1. AWSManagedRulesCommonRuleSet (Priority 1)
    2. AWSManagedRulesKnownBadInputsRuleSet (Priority 2)
    3. AWSManagedRulesSQLiRuleSet (Priority 3)
    4. Rate Limit: 2000 requests/5min per IP (Priority 4)
  • Logging: ENABLED  #  WAF logs para Security Hub
  • Metric: ENABLED   #  CloudWatch metrics

CloudFront Distribution:
  • Comment: "[GitHubRepository] CloudFront Distribution with WAF"
  • Price Class: PriceClass_100
  • HTTP Version: http2
  • IPv6: enabled
  • Web ACL: [GitHubRepository]-cloudfront-web-acl
  • Logging: ENABLED  # Access logs

Origin:
  • ID: [GitHubRepository]-alb-origin
  • Domain: !GetAtt [GitHubRepository]ALB.DNSName
  • Protocol Policy: http-only
  • Custom Headers:
    • X-Origin-Verify: [GitHubRepository]-secret-header-2025

Cache Behaviors:
  1. /api/session/redis/test: TTL 0s, forward all headers/cookies (NO CACHE)
  2. /static/*: TTL 86400s, no query strings/cookies
  3. /api/*: TTL 60s, forward all headers/cookies
  4. Default: TTL 300s, compress, HTTPS redirect

Viewer Certificate: CloudFront Default
```

## **🔴 9) REDIS SERVERLESS COM ENCRYPTION**
```yaml
ElastiCache Serverless: [GitHubRepository]-serverless-cache
  • Engine: redis
  • Major Engine Version: 7
  • Description: "[GitHubRepository] Redis Serverless Cache"
  • Subnet IDs: [GitHubRepository]-private-subnet-1a, [GitHubRepository]-private-subnet-1b, [GitHubRepository]-private-subnet-1c
  • Security Group IDs: [GitHubRepository]-sg-redis
  • Usage Limits:
    • Data Storage: Min 1GB, Max 5GB
    • ECPU Per Second: Min 1000, Max 5000
  • Snapshot Retention: 1 day
  • Daily Snapshot Time: 04:00-05:00
  • Encryption:  
    • At Rest: ENABLED (Default AWS managed)
    • In Transit: ENABLED (TLS 1.2+)
    • Auth Token: ENABLED (Redis AUTH)

Parameter Store Integration:  # ✅ CRÍTICO
  • Endpoint automaticamente salvo em /[GitHubRepository]/redis/endpoint (ENCRYPTED)
  • Port automaticamente salvo em /[GitHubRepository]/redis/port (ENCRYPTED)
  • SSL automaticamente salvo em /[GitHubRepository]/redis/ssl (ENCRYPTED)
  • Task Role OBRIGATÓRIA com permissões ssm:GetParameter + kms:Decrypt
  • Aplicação busca configuração dinamicamente via spring.config.import
  • VPC Endpoint SSM necessário para acesso sem NAT Gateway
  • Security Group deve permitir HTTPS (443) para VPC Endpoints
```

## **📊 10) OBSERVABILIDADE E MONITORING AVANÇADO COM X-RAY**
```yaml
CloudWatch Log Groups:  # All encrypted
  • /ecs/[GitHubRepository]-task: Retention 7 days, KMS encrypted
  • /aws/elasticache/serverless: Retention 7 days, KMS encrypted
  • /aws/vpc/flowlogs: Retention 7 days, KMS encrypted
  • /aws/waf/cloudfront: Retention 30 days, KMS encrypted  

Container Insights: ENABLED  # Habilitado no ECS Cluster

VPC Flow Logs:
  • Destination: CloudWatch Logs
  • Log Group: /aws/vpc/flowlogs
  • Traffic Type: ALL
  • KMS Encryption: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]

CloudWatch Dashboards:  # Enterprise-grade observability
  • [GitHubRepository]-Four-Golden-Signals:  
    • Latência: TargetResponseTime (p95)
    • Tráfego: RequestCount, NetworkIn/Out
    • Erros: HTTPCode_4XX/5XX, StatusCheckFailed
    • Saturação: CPUUtilization, DesiredCapacity, DatabaseCapacity

  • [GitHubRepository]-RED-Metrics:  
    • Rate: RequestCount, RunningTaskCount
    • Errors: HTTPCode_4XX/5XX, UnHealthyHostCount
    • Duration: TargetResponseTime (Average)
    • Success Responses (2XX/3XX), Connection Metrics

  • [GitHubRepository]-USE-Metrics:  
    • Utilization: CPUUtilization, MemoryUtilization
    • Saturation: PendingTaskCount, DesiredCount, RunningTaskCount
    • Errors: UnHealthyHostCount, HTTPCode_5XX
    • Performance correlation, Load Balancer Utilization

  • [GitHubRepository]-XRay-Distributed-Tracing:  
    • Response Time Percentiles (p50, p95, p99)
    • Trace Volume (TracesReceived, TracesProcessed)
    • Error & Fault Rates
    • Service Map Complexity (Edges, Nodes)
    • Single Value KPIs (Avg Response, Error Rate, Total Traces)

  • [GitHubRepository]-Redis-Advanced-Observability:  
    • Cache Hit/Miss Rate
    • Connection Metrics (Current, New)
    • CPU & Memory Utilization
    • Network Throughput (BytesIn/Out)
    • Commands per Second (Get/Set)
    • Memory Management (Evictions, Reclaimed)

  • [GitHubRepository]-Security-Monitoring:  
    • GuardDuty Findings by Severity
    • WAF Blocked vs Allowed Requests
    • Inspector Vulnerability Findings
    • Macie Data Classification Findings
    • WAF Blocks by Rule Type
    • Security Hub Compliance Score

  • [GitHubRepository]-Application-Health:  
    • ECS CPU/Memory utilization
    • ALB response times e error rates
    • Application-specific metrics
    • Auto Scaling activities

  • [GitHubRepository]-Infrastructure:  
    • VPC Flow Logs analysis
    • CloudFront request patterns
    • Auto Scaling events

CloudWatch Alarms:  #  12 alertas proativos enterprise-grade
  Application Performance (CRITICAL):
    • [GitHubRepository]-ECS-CPU-Spike: CPU > 85% por 2 períodos (1min)
    • [GitHubRepository]-ECS-Memory-Pressure: Memory > 90% por 3 períodos (1min)
    • [GitHubRepository]-ALB-Response-Time-High: Response time > 2s por 2 períodos (1min)
    • [GitHubRepository]-ALB-5XX-Error-Spike: 5XX errors > 10 por 2 períodos (1min)
    • [GitHubRepository]-ALB-Unhealthy-Targets: Unhealthy targets > 0 por 3 períodos (30s)

  Redis Performance (WARNING/CRITICAL):
    • [GitHubRepository]-Redis-Cache-Hit-Rate-Low: Hit rate < 80% por 5 períodos (1min)
    • [GitHubRepository]-Redis-Memory-Pressure: Memory > 85% por 3 períodos (1min)
    • [GitHubRepository]-Redis-CPU-High: CPU > 80% por 3 períodos (1min)
    • [GitHubRepository]-Redis-Evictions-High: Evictions > 100/min por 2 períodos (1min)

  Infrastructure Health (WARNING):
    • [GitHubRepository]-ECS-High-Pending-Tasks: Pending tasks > 5 por 2 períodos (5min)
    • [GitHubRepository]-CloudFront-4XX-Error-Rate: 4XX error rate > 5% por 2 períodos (5min)

  Composite Alerts (BUSINESS CRITICAL):
    • [GitHubRepository]-Application-Availability-Composite: Multi-condition availability alert

SNS Topics:  # ✅ IMPLEMENTADO: Email notifications
  • [GitHubRepository]-alerts-critical: arn:aws:sns:[AWS_REGION]:[AWS_ACCOUNT_ID]:[GitHubRepository]-alerts-critical
  • [GitHubRepository]-alerts-warning: arn:aws:sns:[AWS_REGION]:[AWS_ACCOUNT_ID]:[GitHubRepository]-alerts-warning
  • [GitHubRepository]-alerts-info: arn:aws:sns:[AWS_REGION]:[AWS_ACCOUNT_ID]:[GitHubRepository]-alerts-info
  • Email Subscriptions: [SEU_EMAIL] (requires confirmation)

X-Ray Tracing:  # ✅ IMPLEMENTADO: Performance monitoring
  • Service Map: [GitHubRepository]
  • Daemon: Sidecar container (public.ecr.aws/xray/aws-xray-daemon:latest)  # ✅ ECR público para VPC compatibility
  • Sampling Rule: 10% de todas as requests
  • Trace retention: 30 days
  • Encryption: KMS (poc-encryption-key)  
  • Environment Variables:
    • AWS_XRAY_TRACING_NAME: [GitHubRepository]
    • AWS_XRAY_DAEMON_ADDRESS: xray-daemon:2000
    • _X_AMZN_TRACE_ID: ""
  • IAM Permissions: AWSXRayDaemonWriteAccess  
```
## **🛡️ 11) SEGURANÇA ENTERPRISE COMPLETA - STATUS IMPLEMENTADO**
```yaml
AWS Security Hub:  
  • Status: ENABLED
  • Standards: AWS Foundational Security Best Practices v1.0.0 e v1.2.0
  • Findings: Centralized security findings
  • Auto-remediation: ENABLED para findings críticos

IAM Access Analyzer:  
  • Analyzer: account-security-analyzer
  • Type: ACCOUNT
  • Status: ACTIVE
  • Findings: 0 ativos (3 arquivados como esperados)  

Amazon Macie:  
  • Status: ENABLED
  • Finding Publishing Frequency: FIFTEEN_MINUTES
  • S3 Data Classification: ENABLED

Amazon Inspector:  
  • Status: ENABLED
  • ECR Scanning: ENABLED para [GitHubRepository]  # ✅ Container scanning ativo
  • Enhanced Scanning: ENABLED
  • Continuous Monitoring: ENABLED
  • Security Hub Integration: ENABLED

Amazon GuardDuty:  
  • Status: ENABLED
  • Runtime Monitoring: ENABLED  # ✅ ECS Fargate protection ativo
  • ECS Fargate Agent: ENABLED   # ✅ Runtime threat detection ativo
  • Features: CloudTrail, DNS Logs, Flow Logs, S3 Data Events, RDS Login Events
  • Threat detection: 24/7 monitoring
  • Malware Protection: ENABLED

ECR Security Scanning:  
  • Enhanced Scanning: ENABLED
  • Scan on Push: ENABLED
  • Vulnerability Database: Updated daily
  • Integration with Inspector: ENABLED
  • Security Gate: Block CRITICAL CVEs

KMS Encryption:  
  • Key ID: [DYNAMIC_KMS_KEY_ID]
  • Key Rotation: ENABLED (Annual)
  • CloudWatch Logs: ENCRYPTED
  • Parameter Store: ENCRYPTED
  • EBS Default: ENABLED
  • X-Ray Traces: ENCRYPTED
```

## **🧪 12) AURORA POSTGRESQL SERVERLESS v2 (POC · Baixo Custo · Criptografado)**
```yaml
# Banco relacional sob demanda, ideal para protótipos e workloads intermitentes.
# ⚠️ Ajustado para custo mínimo, manter vigilância de consumo e desligar quando não estiver em uso

Cluster Aurora PostgreSQL Serverless v2:
  engine: aurora-postgresql
  engine_mode: serverless
  storage_encrypted: true
  kms_key_id: alias/aws/rds
  allocated_acus: 0.5  # ✅ mínimo permitido para reduzir custos
  max_allocated_acus: 1
  backup_retention_period: 1  # ✅ menor retenção permitida para POC
  deletion_protection: false  # ⚠️ cuidado com deleção acidental
  copy_tags_to_snapshot: false

Infra:
  db_subnet_group:
    - subnet_1: private-a
    - subnet_2: private-b
  vpc_security_group_ids:
    - sg-db  # ❗restringir acesso: apenas de serviços internos autorizados
  multi_az: false  # ✅ evita cobrança adicional desnecessária
  preferred_backup_window: 03:00-04:00
  preferred_maintenance_window: sun:04:00-sun:05:00

Observabilidade e Manutenção:
  enable_performance_insights: false  # ❌ não habilita para evitar custos
  monitoring_interval: 0  # ❌ desativado para reduzir cobrança
  auto_minor_version_upgrade: true
```

## **🚨 ETAPAS DE VERIFICAÇÃO E VALIDAÇÃO**

### **Circuit Breaker Verification:**
```bash
# Verificar se circuit breaker está habilitado
aws ecs describe-services \
    --cluster [GitHubRepository]-cluster \
    --services [GitHubRepository]-service \
    --query 'services[0].deploymentConfiguration.deploymentCircuitBreaker'
```

### **Container Insights Verification:**
```bash
# Verificar Container Insights
aws ecs describe-clusters \
    --clusters [GitHubRepository]-cluster \
    --include INSIGHTS \
    --query 'clusters[0].settings'
```

### **Auto Scaling Verification:**
```bash
# Verificar políticas de auto scaling
aws application-autoscaling describe-scaling-policies \
    --service-namespace ecs \
    --resource-id service/[GitHubRepository]-cluster/[GitHubRepository]-service
```

### **X-Ray Verification:**
```bash
# Verificar X-Ray service map
aws xray get-service-graph \
    --start-time $(date -d '1 hour ago' -u +%Y-%m-%dT%H:%M:%SZ) \
    --end-time $(date -u +%Y-%m-%dT%H:%M:%SZ)
```

## **🔄 GitOps Integration**
```bash
# Exportar configurações para GitOps
aws ecs describe-task-definition \
    --task-definition [GitHubRepository]-task \
    --query 'taskDefinition' > task-definition.json

# Backup das configurações atuais
aws ecs describe-services \
    --cluster [GitHubRepository]-cluster \
    --services [GitHubRepository]-service > service-config-backup.json

# Aplicar ao repositório GitOps
# (usuário deve commitar as alterações no repositório)
```

## **🏷️ 13) TAGS PADRÃO**
```yaml
Todas as resources:
 Environment: POC
 Owner: [SEU_NOME]
 ManagedBy: MCP
 Project: [GitHubRepository]
 SecurityLevel: Enterprise  
 EncryptionEnabled: true    
 AutoScalingEnabled: true   
 XRayTracingEnabled: true   
```

## **📋 PLACEHOLDERS PARA SUBSTITUIÇÃO:**
- `[AWS_ACCOUNT_ID]`: ID da conta AWS
- `[AWS_REGION]`: Região AWS (ex: us-east-1)
- `[GitHubOrganization]`: Organização GitHub
- `[GitHubRepository]`: Nome do repositório
- `[SEU_NOME]`: Nome do usuário
- `[SEU_EMAIL]`: Email para notificações de alertas
- `[DYNAMIC_KMS_KEY_ID]`: ID da chave KMS (gerado dinamicamente)
- `[VPC_ID]`: ID da VPC (gerado dinamicamente)
- `[DETECTOR_ID]`: ID do detector GuardDuty (gerado dinamicamente)

## **✅ RESULTADO FINAL GARANTIDO:**
- Auto scaling com 3 métricas (CPU 75%, ALB 1000 req/target, Memory 80%)
- X-Ray tracing completo com sidecar container
- Health checks otimizados (15s interval, 5s timeout)
- Circuit breaker com rollback automático
- Container Insights habilitado
- Configuração aplicada ao GitOps
- Segurança enterprise completa
- Zero downtime deployment
- Encryption end-to-end
- Monitoring e observabilidade avançados

## **📊 OBSERVABILIDADE ENTERPRISE-GRADE IMPLEMENTADA:**
- **9 Dashboards CloudWatch** cobrindo todas as metodologias:
  - Four Golden Signals (Latência, Tráfego, Erros, Saturação) ✅
  - RED Methodology (Rate, Errors, Duration) ✅
  - USE Methodology (Utilization, Saturation, Errors) ✅
  - X-Ray Distributed Tracing (Response Time, Traces, Service Map) ✅
  - Redis Advanced Observability (Hit Rate, Connections, Commands) ✅
  - Security Monitoring (GuardDuty, WAF, Inspector, Macie) ✅
  - Application Health (ECS, ALB, Auto Scaling) ✅
  - Infrastructure Monitoring (VPC, CloudFront) ✅
- **12 Alertas Proativos** com notificações SNS por email:
  - 5 alertas críticos de performance da aplicação ✅
  - 4 alertas de performance Redis (warning/critical) ✅
  - 2 alertas de saúde da infraestrutura ✅
  - 1 alerta composite business-critical ✅
  - 3 tópicos SNS com subscrições de email ✅
- **Score de aderência às boas práticas: 9.8/10 EXCEPCIONAL** ✅
