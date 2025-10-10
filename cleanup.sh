#!/bin/bash
# Script de limpeza definitiva

echo "Removendo arquivos problemáticos..."

# Remove diretórios com/example
find . -type d -path "*/com/example" -exec rm -rf {} + 2>/dev/null || true

# Remove arquivos HealthConfig
find . -name "HealthConfig.java" -delete 2>/dev/null || true

# Remove arquivos RedisConfigOptimized
find . -name "*RedisConfigOptimized*" -delete 2>/dev/null || true

# Remove do git cache
git rm -rf --cached src/main/java/com/example/ 2>/dev/null || true
git rm --cached src/main/java/com/poc/config/HealthConfig.java 2>/dev/null || true

echo "Limpeza concluída!"

# Verifica se ainda há arquivos problemáticos
echo "Verificando arquivos restantes..."
find . -name "*.java" | grep -E "(example|HealthConfig|RedisConfigOptimized)" || echo "Nenhum arquivo problemático encontrado!"
