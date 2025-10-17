```markdown
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

## 📋 PARÂMETROS DE ENTRADA
```yaml
AWS_ACCOUNT_ID: 221082174220
AWS_REGION: us-east-1
GitHubOrganization: Diego-Nardoni
GitHubRepository: spring-redis-app
SEU_NOME: Diego-Nardoni
Aplicação: Spring Boot já existente no GitHub
```

## 🔐 1) IDENTIDADE E ACESSO (GitHub Actions OIDC)
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

## 🛡️ 2) SEGURANÇA ENTERPRISE - IMPLEMENTADO

### 🚨 ETAPA OBRIGATÓRIA 1 - KMS ENCRYPTION:
```yaml
KMS Key: poc-encryption-key
  • KeyId: [DYNAMIC_KMS_KEY_ID]
  • Arn: arn:aws:kms:[AWS_REGION]:[AWS_ACCOUNT_ID]:key/[DYNAMIC_KMS_KEY_ID]
  • Description: "KMS key for POC encryption at rest"
  • EnableKeyRotation: true
  • KeyPolicy:
    - Enable IAM User Permissions: arn:aws:iam::[AWS_ACCOUNT_ID]:root
    - Allow CloudWatch Logs: logs.[AWS_REGION].amazonaws.com
    - Allow EBS: ec2.[AWS_REGION].amazonaws.com
    - Allow Parameter Store: ssm.[AWS_REGION].amazonaws.com
  • Tags: Environment=POC, Project=[GitHubRepository]
```

### 🚨 ETAPA OBRIGATÓRIA 2 - SECURITY SERVICES:
```yaml
Amazon Inspector:
  • Status: ENABLED
  • ECR Scanning: ENABLED
  • Enhanced Scanning: ENABLED
  • Scan on Push: ENABLED
  • Integration: Security Hub

Amazon GuardDuty:
  • Status: ENABLED
  • Runtime Monitoring: ENABLED
  • ECS Fargate Agent: ENABLED
  • Finding Frequency: SIX_HOURS

AWS Security Hub:
  • Status: ENABLED
  • Standards: AWS Foundational Security Best Practices

IAM Access Analyzer:
  • Analyzer: account-security-analyzer
  • Type: ACCOUNT
  • Status: ACTIVE

Amazon Macie:
  • Status: ENABLED

EBS Encryption:
  • Default Encryption: ENABLED
  • KMS Key: poc-encryption-key
```

## 🌐 3) NETWORKING AVANÇADO - ORDEM CRÍTICA

### ORDEM OBRIGATÓRIA DE CRIAÇÃO:
```yaml
ETAPA 1 - VPC Base:
  VPC:
    • CIDR: 10.0.0.0/16
    • Nome: [GitHubRepository]-vpc

ETAPA 2 - Subnets:
  Públicas (ALB):
    • [GitHubRepository]-public-subnet-1a: 10.0.1.0/24 ([AWS_REGION]a)
    • [GitHubRepository]-public-subnet-1b: 10.0.2.0/24 ([AWS_REGION]b)
    • [GitHubRepository]-public-subnet-1c: 10.0.3.0/24 ([AWS_REGION]c)

  Privadas (ECS/Redis):
    • [GitHubRepository]-private-subnet-1a: 10.0.11.0/24 ([AWS_REGION]a)
    • [GitHubRepository]-private-subnet-1b: 10.0.12.0/24 ([AWS_REGION]b)
    • [GitHubRepository]-private-subnet-1c: 10.0.13.0/24 ([AWS_REGION]c)
```

### Security Groups (resumo):
```yaml
[GitHubRepository]-sg-alb-cloudfront-only: Ingress TCP 80 de CloudFront
[GitHubRepository]-sg-app: Ingress TCP 8080 de [sg-alb-cloudfront-only]
[GitHubRepository]-sg-endpoints: Ingress TCP 443 de [sg-app]
[GitHubRepository]-sg-redis: Ingress TCP 6379 de [sg-app]
```

## 📦 4) ECR COM SECURITY SCANNING AVANÇADO
Detalhes: Scan on Push, Encryption: KMS (poc-encryption-key), Lifecycle policy

## 🔧 5) PARAMETER STORE
Armazene parâmetros sensíveis (redis endpoint, port, ssl) criptografados com KMS.

## ⚙️ 6) ECS FARGATE - ZERO DOWNTIME
Use estratégia de deployment com MaximumPercent 200 e MinimumHealthyPercent 0 para zero downtime.

----

> Observação: este arquivo contém o prompt completo (IaP). Para uso prático, copie e adapte os parâmetros antes de executar em ambiente de produção.

```
