#!/bin/bash

echo "🔧 Testando Parameter Store Implementation"
echo "=========================================="
echo ""

echo "1. Verificando parâmetros no AWS Parameter Store:"
echo "   - /poc/redis/endpoint"
echo "   - /poc/redis/port"
echo ""

# Verificar parâmetros
REDIS_ENDPOINT=$(aws ssm get-parameter --name "/poc/redis/endpoint" --query "Parameter.Value" --output text 2>/dev/null)
REDIS_PORT=$(aws ssm get-parameter --name "/poc/redis/port" --query "Parameter.Value" --output text 2>/dev/null)

if [ "$REDIS_ENDPOINT" != "" ]; then
    echo "✅ Parameter /poc/redis/endpoint: $REDIS_ENDPOINT"
else
    echo "❌ Parameter /poc/redis/endpoint não encontrado"
fi

if [ "$REDIS_PORT" != "" ]; then
    echo "✅ Parameter /poc/redis/port: $REDIS_PORT"
else
    echo "❌ Parameter /poc/redis/port não encontrado"
fi

echo ""
echo "2. Iniciando aplicação Spring Boot..."
echo "   A aplicação irá:"
echo "   - Buscar endpoint Redis do Parameter Store"
echo "   - Usar fallback se Parameter Store falhar"
echo "   - Conectar no Redis usando a configuração obtida"
echo ""

echo "3. Endpoints de teste disponíveis:"
echo "   - http://localhost:8080/actuator/health"
echo "   - http://localhost:8080/api/parameter-store/test"
echo "   - http://localhost:8080/api/parameter-store/redis-config"
echo ""

# Executar aplicação
cd /home/novo-proj/spring-redis-poc
java -jar target/spring-redis-poc-1.0.0.jar
