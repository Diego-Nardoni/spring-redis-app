# Spring Redis Application

## Descrição do Projeto
Este é um projeto Spring Boot demonstrando integração com Redis para gerenciamento de sessões e operações básicas.

## Estrutura do Projeto
- `src/main/java/com/poc/`: Contém os principais controladores e configurações
  - `Application.java`: Classe principal da aplicação Spring Boot
  - `HelloController.java`: Controlador de exemplo
  - `HomeController.java`: Controlador de página inicial
  - `SessionConfig.java`: Configuração de sessão
  - `SessionController.java`: Controlador de gerenciamento de sessão

## Pré-requisitos
- Java 8 ou superior
- Maven
- Redis (para funcionalidades de sessão)

## Configuração
Verifique `src/main/resources/application.properties` para configurações do projeto.

## Construção e Execução
```bash
# Compilar o projeto
mvn clean package

# Executar a aplicação
mvn spring-boot:run
```

## Tecnologias Utilizadas
- Spring Boot
- Redis
- Maven

## Licença
Projeto de demonstração - Uso livre

## Autor
Diego Nardoni
