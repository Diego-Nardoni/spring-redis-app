#!/bin/bash

echo "=== TESTE BUILD DOCKER LOCAL ==="

# Build da imagem
echo "1. Construindo imagem Docker..."
docker build -t spring-redis-test .

if [ $? -eq 0 ]; then
    echo "✅ Build concluído com sucesso!"
    
    echo "2. Testando execução do container..."
    docker run --rm -d --name spring-redis-test -p 8080:8080 spring-redis-test
    
    sleep 10
    
    echo "3. Verificando se aplicação está rodando..."
    if curl -s http://localhost:8080/actuator/health > /dev/null; then
        echo "✅ Aplicação funcionando!"
    else
        echo "❌ Aplicação não respondeu"
        docker logs spring-redis-test
    fi
    
    docker stop spring-redis-test
else
    echo "❌ Erro no build Docker"
fi
