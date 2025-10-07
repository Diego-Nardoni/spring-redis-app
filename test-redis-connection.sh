#!/bin/bash

echo "🔍 Testando conexão com Redis Serverless..."
echo "Endpoint: poc-redis-01ndkd.serverless.use1.cache.amazonaws.com:6379"
echo ""

# Testar se o endpoint responde
if command -v nc &> /dev/null; then
    echo "📡 Testando conectividade de rede..."
    if nc -z poc-redis-01ndkd.serverless.use1.cache.amazonaws.com 6379; then
        echo "✅ Conectividade de rede OK"
    else
        echo "❌ Falha na conectividade de rede"
        exit 1
    fi
else
    echo "⚠️  netcat não disponível, pulando teste de rede"
fi

echo ""
echo "🚀 Iniciando aplicação Spring Boot para teste..."
echo "A aplicação irá usar o endpoint Redis configurado:"
echo "  - Host: poc-redis-01ndkd.serverless.use1.cache.amazonaws.com"
echo "  - Port: 6379"
echo ""
echo "Endpoints de teste disponíveis:"
echo "  - http://localhost:8080/actuator/health"
echo "  - http://localhost:8080/api/session/redis/test"
echo "  - http://localhost:8080/api/test/architecture"
echo ""

# Executar aplicação
cd /home/novo-proj/spring-redis-poc
java -jar target/spring-redis-poc-1.0.0.jar
