# 🚀 Spring Boot Redis POC

Aplicação Spring Boot com integração Redis e AWS Parameter Store, deployada em ECS Fargate com GitOps completo.

## 📋 Visão Geral

Esta aplicação demonstra:
- **Spring Boot 3.1.5** com Java 17
- **Redis** para cache e sessões de usuários
- **AWS Parameter Store** para configuração dinâmica
- **ECS Fargate** para deployment containerizado
- **GitOps** com GitHub Actions para deploy automático

## 🏗️ Arquitetura

```
Internet → ALB → ECS Fargate → Redis
                      ↓
              Parameter Store
```

## ✅ Configuração Dinâmica

### Parameter Store
```
/poc/redis/endpoint: seu-redis-endpoint.amazonaws.com
/poc/redis/port: 6379
/poc/redis/ssl: false
```

### GitHub Secrets
```
AWS_ROLE_ARN: arn:aws:iam::SUA_CONTA:role/GitHubActionsRole
AWS_REGION: sua-regiao (opcional)
```

## 🚀 Deploy GitOps

```bash
git add .
git commit -m "deploy: update application"
git push origin main
```

**GitHub Actions executará automaticamente:**
- Build da aplicação
- Push para ECR com tag dinâmica
- Deploy no ECS com zero hardcoding
- Aplicação conecta no Redis via Parameter Store

## 🔧 Funcionalidades

- **Sessões Redis**: Armazenamento de sessões de usuários
- **Parameter Store**: Configuração dinâmica sem hardcoding
- **Health Checks**: Monitoramento da aplicação
- **Multi-Account**: Funciona em qualquer conta AWS

## 📦 Estrutura

```
├── src/main/resources/
│   └── application-production.yml    # Configuração Parameter Store
├── task-definition.json              # Template dinâmico ECS
├── .github/workflows/deploy.yml      # GitOps workflow
├── Dockerfile                        # Container build
└── GITOPS-SETUP.md                  # Documentação deploy
```

## 🎯 Zero Hardcoding

- ✅ Account ID: Detectado automaticamente
- ✅ Region: Configurável via secrets
- ✅ Redis endpoint: Via Parameter Store
- ✅ ARNs: Construídos dinamicamente

**Funciona em qualquer conta AWS!** 🎉

### Componentes AWS
- **ECS Fargate**: Execução da aplicação
- **ElastiCache Serverless**: Cache Redis
- **Parameter Store**: Configuração dinâmica
- **ALB**: Load balancing
- **CloudFront**: CDN global
- **WAF**: Proteção de segurança
- **ECR**: Registry de containers

## 🔧 Configuração

### Parameter Store
```yaml
/poc/redis/port: 6379
/poc/redis/ssl: true
```

### Application Properties
```yaml
spring:
  config:
    import: "aws-parameterstore:/poc/"
  cloud:
    aws:
      parameterstore:
        enabled: true
        prefix: /poc
        fail-fast: false
  data:
    redis:
      host: ${redis.endpoint:localhost}
      port: ${redis.port:6379}
      ssl:
        enabled: ${redis.ssl:false}
```

## 🚀 Deploy

### Pré-requisitos
- AWS CLI configurado
- Docker instalado
- Maven 3.6+
- Java 17+

### Deploy Automático
```bash
./build-and-deploy.sh
```

### Deploy Manual
```bash
# 1. Build
mvn clean package -DskipTests

# 2. Docker build & push
docker build -t spring-redis-poc .

# 3. Deploy ECS
./deploy.sh
```

### Setup Inicial
```bash
# Configurar Parameter Store
./setup-parameter-store.sh
```

## 🧪 Testes

### Teste Local
```bash
# Testar Parameter Store
./test-parameter-store.sh

# Testar Redis
./test-redis-connection.sh

# Validar segurança
./validate-security-setup.sh
```

### Endpoints de Teste
```bash
# Health check
curl http://localhost:8080/actuator/health

# Teste Redis
curl http://localhost:8080/api/session/redis/test

# Arquitetura
curl http://localhost:8080/api/test/architecture
```

## 📊 Monitoramento

### CloudWatch Logs
- `/ecs/spring-redis-poc` - Application logs
- `/aws/elasticache/serverless` - Redis logs

### Métricas
- ECS CPU/Memory utilization
- Redis connection metrics
- ALB response times
- Parameter Store access

### Alarms
- ECS CPU > 80%
- ALB 5xx errors > 5%
- Redis connection failures

## 🔐 Segurança

### IAM Roles
- **ECS Task Role**: Parameter Store access
- **ECS Execution Role**: ECR + CloudWatch

### Security Groups
- ALB: Apenas CloudFront
- ECS: Apenas ALB + Redis + VPC Endpoints
- Redis: Apenas ECS

### Encryption
- **At Rest**: ElastiCache + Parameter Store
- **In Transit**: HTTPS/TLS em todas as conexões

## 🌐 URLs

### Produção
- **CloudFront**: `https://d1234567890.cloudfront.net`
- **ALB**: `http://poc-alb-1313969028.us-east-1.elb.amazonaws.com`

### Desenvolvimento
- **Local**: `http://localhost:8080`

## 📁 Estrutura do Projeto

```
spring-redis-poc/
├── src/main/java/com/poc/
│   ├── Application.java
│   ├── config/
│   │   ├── RedisConfig.java
│   │   └── SecurityConfig.java
│   ├── controller/
│   │   ├── HealthController.java
│   │   ├── RedisTestController.java
│   │   └── ParameterStoreController.java
│   └── service/
│       └── ParameterStoreService.java
├── src/main/resources/
│   └── application.yml
├── scripts/
│   ├── build-and-deploy.sh
│   ├── deploy.sh
│   ├── setup-parameter-store.sh
│   ├── test-parameter-store.sh
│   ├── test-redis-connection.sh
│   └── validate-security-setup.sh
├── Dockerfile
├── pom.xml
└── README.md
```

## 🔄 CI/CD

### GitHub Actions
```yaml
# .github/workflows/deploy.yml
name: Deploy to ECS
on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Configure AWS credentials
        uses: aws-actions/configure-aws-credentials@v4
        with:
          role-to-assume: ${{ secrets.AWS_ROLE_ARN }}
          aws-region: us-east-1
      - name: Deploy
        run: ./build-and-deploy.sh
```

### Variáveis Necessárias
```yaml
GitHub Secrets:

GitHub Variables:
  AWS_REGION: us-east-1
  ECS_CLUSTER: poc-cluster
  ECS_SERVICE: poc-spring-redis-service
```

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. Parameter Store Access Denied
```bash
# Verificar IAM role
aws sts get-caller-identity
aws ssm get-parameter --name "/poc/redis/endpoint"
```

#### 2. Redis Connection Failed
```bash
# Verificar security groups
aws ec2 describe-security-groups --group-ids sg-xxx
```

#### 3. ECS Task Failed
```bash
# Verificar logs
aws logs get-log-events --log-group-name "/ecs/spring-redis-poc" --log-stream-name "xxx"
```

### Health Checks
```bash
# Application health
curl http://localhost:8080/actuator/health

# Redis connectivity
curl http://localhost:8080/api/session/redis/test

# Parameter Store
curl http://localhost:8080/api/parameter-store/test
```

## 📚 Dependências Principais

```xml
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
        <version>3.1.5</version>
    </dependency>
    
    <!-- Redis -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-redis</artifactId>
    </dependency>
    
    <!-- Parameter Store -->
    <dependency>
        <groupId>io.awspring.cloud</groupId>
        <artifactId>spring-cloud-aws-starter-parameter-store</artifactId>
        <version>3.0.3</version>
    </dependency>
    
    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

## 🏷️ Tags

- **Environment**: POC
- **Technology**: Spring Boot, Redis, AWS
- **Deployment**: ECS Fargate
- **Architecture**: Serverless

## 📞 Suporte

Para questões técnicas:
1. Verificar logs no CloudWatch
2. Executar scripts de validação
3. Consultar documentação AWS
4. Abrir issue no repositório

---

**🎯 Aplicação pronta para produção com arquitetura serverless completa!** ✅
