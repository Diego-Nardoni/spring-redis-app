#!/bin/bash

echo "=== VALIDAÇÃO SECURITY GATE E PARAMETER STORE ==="
echo "Diretório: $(pwd)"
echo "Data: $(date)"
echo

# 1. Verificar se estamos no diretório correto
if [[ ! -f "pom.xml" ]] || [[ ! -d "src/main/java/com/poc" ]]; then
    echo "❌ ERRO: Não estamos no diretório correto da aplicação"
    exit 1
fi

echo "✅ Diretório correto: /home/novo-proj/spring-redis-poc"

# 2. Verificar dependências de segurança no pom.xml
echo
echo "=== VERIFICANDO DEPENDÊNCIAS DE SEGURANÇA ==="
if grep -q "spring-boot-starter-security" pom.xml; then
    echo "✅ Spring Security dependency encontrada"
else
    echo "❌ Spring Security dependency NÃO encontrada"
fi

if grep -q "spring-cloud-aws-starter-parameter-store" pom.xml; then
    echo "✅ Parameter Store dependency encontrada"
else
    echo "❌ Parameter Store dependency NÃO encontrada"
fi

if grep -q "software.amazon.awssdk" pom.xml; then
    echo "✅ AWS SDK dependency encontrada"
else
    echo "❌ AWS SDK dependency NÃO encontrada"
fi

# 3. Verificar configuração de segurança
echo
echo "=== VERIFICANDO CONFIGURAÇÃO DE SEGURANÇA ==="
if [[ -f "src/main/java/com/poc/config/SecurityConfig.java" ]]; then
    echo "✅ SecurityConfig.java existe"
    if grep -q "@EnableWebSecurity" src/main/java/com/poc/config/SecurityConfig.java; then
        echo "✅ @EnableWebSecurity annotation encontrada"
    else
        echo "❌ @EnableWebSecurity annotation NÃO encontrada"
    fi
else
    echo "❌ SecurityConfig.java NÃO existe"
fi

# 4. Verificar Parameter Store Service
echo
echo "=== VERIFICANDO PARAMETER STORE SERVICE ==="
if [[ -f "src/main/java/com/poc/service/ParameterStoreService.java" ]]; then
    echo "✅ ParameterStoreService.java existe"
    if grep -q "SsmClient" src/main/java/com/poc/service/ParameterStoreService.java; then
        echo "✅ SsmClient configurado"
    else
        echo "❌ SsmClient NÃO configurado"
    fi
else
    echo "❌ ParameterStoreService.java NÃO existe"
fi

# 5. Verificar configuração do application.yml
echo
echo "=== VERIFICANDO APPLICATION.YML ==="
if [[ -f "src/main/resources/application.yml" ]]; then
    echo "✅ application.yml existe"
    if grep -q "parameterstore:" src/main/resources/application.yml; then
        echo "✅ Parameter Store configurado no application.yml"
    else
        echo "❌ Parameter Store NÃO configurado no application.yml"
    fi
    if grep -q "/poc/redis/endpoint" src/main/resources/application.yml; then
        echo "✅ Redis endpoint configurado via Parameter Store"
    else
        echo "❌ Redis endpoint NÃO configurado via Parameter Store"
    fi
else
    echo "❌ application.yml NÃO existe"
fi

# 6. Verificar parâmetros no AWS Parameter Store
echo
echo "=== VERIFICANDO PARÂMETROS NO AWS PARAMETER STORE ==="
if aws ssm get-parameter --name "/poc/redis/endpoint" --region us-east-1 >/dev/null 2>&1; then
    echo "✅ Parâmetro /poc/redis/endpoint existe no Parameter Store"
else
    echo "❌ Parâmetro /poc/redis/endpoint NÃO existe no Parameter Store"
fi

if aws ssm get-parameter --name "/poc/redis/port" --region us-east-1 >/dev/null 2>&1; then
    echo "✅ Parâmetro /poc/redis/port existe no Parameter Store"
else
    echo "❌ Parâmetro /poc/redis/port NÃO existe no Parameter Store"
fi

# 7. Verificar build da aplicação
echo
echo "=== VERIFICANDO BUILD DA APLICAÇÃO ==="
if mvn compile -q >/dev/null 2>&1; then
    echo "✅ Aplicação compila sem erros"
else
    echo "❌ Aplicação NÃO compila - há erros"
fi

echo
echo "=== VALIDAÇÃO CONCLUÍDA ==="
