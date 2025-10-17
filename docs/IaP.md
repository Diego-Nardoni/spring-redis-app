 # infrastructure as a Prompt (IaP)

Este arquivo contém a versão canônica e legível do Infrastructure as a Prompt (IaP) extraído de `PROMPT.md`.

Use este documento como referência humana antes de executar qualquer ação automatizada de provisionamento. O IaP descreve um fluxo idempotente orientado por prompts (MCP / Cloud Control API) e fornece checklist, parâmetros e comandos de verificação.

## Fluxo recomendado (resumido)

1. Revisar parâmetros e checklist listados neste arquivo.
2. Executar `explain()` no motor IaP para gerar um plano legível.
3. Revisar e aprovar o plano (revisão humana obrigatória).
4. Executar `create()` (ou `apply()`) para provisionar os recursos aprovados.
5. Executar `verify()` e os scripts de validação para confirmar recursos e conectividade.

> Sempre pare após `explain()` e obtenha confirmação humana antes de `create()`.

## Principais componentes (resumo)

- Rede: VPC com subnets públicas/privadas, NAT gateway (ou VPC endpoints), route tables e VPC endpoints (SSM, ECR, CloudWatch).
- ECS: Cluster Fargate, Task Definition, Service com ALB, target groups e autoscaling.
- Redis: ElastiCache Serverless para cache e sessão.
- Secrets & Storage: S3 para artefatos; Parameter Store (SSM) com criptografia KMS para segredos.
- Segurança: KMS, IAM com princípio de menor privilégio, GuardDuty, Inspector, Security Hub, WAF/CloudFront.
- Observabilidade: CloudWatch (logs/metrics), X-Ray (tracing), dashboards e alarmes.

## Checklist resumido (seleção)

1. Criar KMS Key com rotação habilitada.
2. Criar VPC com múltiplas AZs e subnets públicas/privadas.
3. Configurar Internet Gateway, NAT Gateway (quando necessário) e Route Tables.
4. Configurar VPC Endpoints: SSM, ECR (API e DKR), CloudWatch Logs/Events.
5. Criar Security Groups (ALB, app, redis) com mínimo acesso requerido.
6. Criar ECR repository com image scanning (scanOnPush=true) e lifecycle policy.
7. Criar ECS Cluster, Task Definition (task role + execution role) e Service com ALB.
8. Provisionar ElastiCache Serverless e restringir acesso por security group.
9. Armazenar segredos no Parameter Store (SSM) criptografados com KMS.
10. Instrumentar a aplicação com X-Ray e configurar CloudWatch Dashboards.

Para a lista completa (47 itens) e detalhes operacionais, consulte `PROMPT.md`.

## Comandos e validações úteis (exemplos)

- Plan/Explain (executar no motor IaP): gere o plano legível e revise.
- Apply/Create: após revisão humana, execute `create()` ou `apply()` no motor IaP.
- Verify: comandos AWS CLI de verificação (ajuste valores antes de executar):

```bash
aws kms list-keys --query 'Keys[?KeyId==`[DYNAMIC_KMS_KEY_ID]`]'
aws ec2 describe-vpcs --filters "Name=tag:Name,Values=[GitHubRepository]-vpc"
aws ecs list-clusters --query 'clusterArns[?contains(@,`[GitHubRepository]-cluster`)]'
aws elasticache describe-serverless-caches --serverless-cache-name [GitHubRepository]-serverless-cache
```

Exemplos de verificação pós-provisionamento:

1. Health endpoint da aplicação:

```bash
curl -s https://<ALB-DNS>/actuator/health/readiness
```

2. Endpoint de teste do Redis:

```bash
curl -s https://<ALB-DNS>/api/session/redis/test
```

3. Conferir métricas no CloudWatch e traces no X-Ray.

## Recomendações operacionais

- Sempre executar `explain()` e obter confirmação humana antes de `create()`.
- Use VPC Endpoints quando possível para reduzir custos com NAT e limitar exposição.
- Habilite image scanning no ECR e integre com Security Hub/Inspector.
- Restrinja políticas IAM usando o princípio do menor privilégio e audite com Access Analyzer.

## Referências

- Prompt completo original (detalhado): `PROMPT.md`.
- README do projeto: `../README.md`.

---

Versão final: documento resumido, canônico e pronto para revisão humana. Para o prompt completo e histórico de alterações, abra `PROMPT.md`.


