#!/bin/bash

echo "=== Teste do Circuit Breaker - Spring Redis App ==="
echo

# Verificar se a aplicação está rodando
echo "1. Verificando se a aplicação está rodando..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✅ Aplicação está rodando"
else
    echo "❌ Aplicação não está rodando. Iniciando..."
    cd /home/spring-redis-app
    nohup java -jar target/spring-redis-poc-1.0.0.jar > app.log 2>&1 &
    echo "Aguardando aplicação inicializar..."
    sleep 10
fi

echo
echo "2. Testando status do Circuit Breaker..."
curl -s http://localhost:8080/api/circuit-breaker/status | jq '.' || echo "Erro ao acessar status"

echo
echo "3. Testando operação Redis (deve funcionar se Redis estiver disponível)..."
curl -s -X POST "http://localhost:8080/api/circuit-breaker/test-redis/test-key?value=test-value" | jq '.' || echo "Erro no teste Redis"

echo
echo "4. Verificando métricas do Actuator..."
curl -s http://localhost:8080/actuator/circuitbreakers | jq '.' || echo "Erro ao acessar métricas"

echo
echo "5. Forçando Circuit Breaker para OPEN (simulando falha)..."
curl -s -X POST http://localhost:8080/api/circuit-breaker/force-circuit-open | jq '.' || echo "Erro ao forçar circuit open"

echo
echo "6. Testando operação com Circuit Breaker OPEN (deve usar fallback)..."
curl -s -X POST "http://localhost:8080/api/circuit-breaker/test-redis/fallback-test?value=fallback-value" | jq '.' || echo "Erro no teste fallback"

echo
echo "7. Verificando status após forçar OPEN..."
curl -s http://localhost:8080/api/circuit-breaker/status | jq '.' || echo "Erro ao verificar status"

echo
echo "8. Restaurando Circuit Breaker para CLOSED..."
curl -s -X POST http://localhost:8080/api/circuit-breaker/force-circuit-closed | jq '.' || echo "Erro ao restaurar circuit"

echo
echo "=== Teste concluído ==="
