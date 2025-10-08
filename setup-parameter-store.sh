#!/bin/bash

echo "🔧 Configurando AWS Parameter Store para Redis..."

# Criar parâmetro para endpoint Redis
aws ssm put-parameter \
  --name "/poc/redis/endpoint" \
  --value "poc-redis-01ndkd.serverless.use1.cache.amazonaws.com" \
  --type "String" \
  --description "Redis Serverless endpoint for POC" \
  --overwrite

# Criar parâmetro para porta Redis
aws ssm put-parameter \
  --name "/poc/redis/port" \
  --value "6379" \
  --type "String" \
  --description "Redis port for POC" \
  --overwrite

echo "✅ Parâmetros criados:"
echo "  - /poc/redis/endpoint"
echo "  - /poc/redis/port"
echo ""
echo "Para usar na aplicação, adicione ao application.yml:"
echo ""
echo "aws:"
echo "  paramstore:"
echo "    enabled: true"
echo "    prefix: /poc"
echo ""
echo "redis:"
echo "  endpoint: \${/poc/redis/endpoint}"
echo "  port: \${/poc/redis/port}"
