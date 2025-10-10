# GitOps - Multi-Account Deployment

## 🎯 Objetivo
GitHub Actions que funciona em **qualquer conta AWS** sem hardcoding.

## ✅ Configuração Dinâmica

### GitHub Secrets Necessários:
```
AWS_ROLE_ARN: arn:aws:iam::YOUR_ACCOUNT_ID:role/GitHubActionsRole
AWS_REGION: us-east-1 (ou sua região preferida)
```

### Arquivos Configurados:
- **`task-definition.json`** - Placeholders dinâmicos (`${AWS_ACCOUNT_ID}`, `${AWS_REGION}`)
- **`application-production.yml`** - Sem hardcoding de região
- **`.github/workflows/deploy.yml`** - Detecta conta e região automaticamente

### Como Funciona:
1. **Detecta Account ID** automaticamente via `aws sts get-caller-identity`
2. **Detecta Region** via configuração AWS
3. **Substitui placeholders** no task-definition.json
4. **Deploy** com valores corretos para a conta atual

## 🚀 Para Usar em Qualquer Conta

### 1. Configure GitHub Secrets:
```bash
# No GitHub Repository Settings > Secrets
AWS_ROLE_ARN: arn:aws:iam::SUA_CONTA:role/GitHubActionsRole
AWS_REGION: sua-regiao (opcional, default: us-east-1)
```

### 2. Infraestrutura Necessária (via MCP):
- ECR repository: `spring-redis-app`
- ECS cluster: `poc-cluster`
- ECS service: `poc-spring-redis-service`
- IAM roles: `ecsTaskExecutionRole`, `ecsTaskRole`
- Parameter Store: `/poc/redis/*`

### 3. Deploy:
```bash
git push origin main
```

## ✅ Zero Hardcoding

- ❌ Account ID hardcoded
- ❌ Region hardcoded  
- ❌ ARNs hardcoded
- ✅ **Tudo dinâmico!**

Funciona em **qualquer conta AWS** com a infraestrutura correta!
