# 🚀 Modern Redis Session Manager

A professional, modern Spring Boot application demonstrating advanced session management with Redis, designed for cloud-native deployment on AWS ECS with CloudFront distribution.

## ✨ Features

### 🎯 Core Functionality
- **Advanced Session Management** - Intelligent session handling with Redis backend
- **Real-time Health Monitoring** - Comprehensive Redis health checks and performance metrics
- **Circuit Breaker Pattern** - Resilient architecture with automatic failover
- **Modern UI Dashboard** - Beautiful, responsive web interface
- **RESTful APIs** - Complete API suite for session management

### 🛡️ Reliability & Observability
- **Circuit Breaker** - Resilience4j integration for fault tolerance
- **Metrics & Monitoring** - Prometheus metrics with Grafana dashboards
- **Health Checks** - Comprehensive health endpoints
- **Distributed Tracing** - Request tracing with Micrometer
- **Structured Logging** - JSON logging for cloud environments

### 🧪 Testing Excellence
- **TestContainers Integration** - Real Redis testing with containers
- **Performance Testing** - Automated Redis performance validation
- **Integration Tests** - Comprehensive test coverage
- **Load Testing Ready** - Built for performance validation

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────┐    ┌─────────────────┐
│   CloudFront    │────│     ELB      │────│   ECS Fargate   │
│   (CDN/Cache)   │    │ (Load Balancer)│    │   (Containers)  │
└─────────────────┘    └──────────────┘    └─────────────────┘
                                                      │
                                                      ▼
                                           ┌─────────────────┐
                                           │ ElastiCache     │
                                           │ (Redis Cluster) │
                                           └─────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.8+

### Local Development

1. **Clone and Setup**
```bash
git clone <repository-url>
cd modern-redis-app
```

2. **Start with Docker Compose**
```bash
docker-compose up -d
```

3. **Access the Application**
- **Dashboard**: http://localhost:8080
- **API**: http://localhost:8080/api/session
- **Health**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/prometheus
- **Grafana**: http://localhost:3000 (admin/admin)

### Manual Setup

1. **Start Redis**
```bash
docker run -d -p 6379:6379 redis:7.2-alpine
```

2. **Run Application**
```bash
mvn spring-boot:run
```

## 🔧 Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_REDIS_HOST` | Redis hostname | localhost |
| `SPRING_REDIS_PORT` | Redis port | 6379 |
| `SPRING_REDIS_PASSWORD` | Redis password | (empty) |
| `APP_CONTAINER_ID` | Container identifier | local-container |
| `APP_REGION` | AWS region | us-east-1 |

### AWS ECS Configuration

```yaml
# Task Definition Environment
SPRING_REDIS_HOST: your-elasticache-endpoint
SPRING_REDIS_PORT: 6379
APP_CONTAINER_ID: ${HOSTNAME}
APP_REGION: us-east-1
```

## 📊 Monitoring & Observability

### Health Endpoints
- `/actuator/health` - Application health
- `/actuator/health/redis` - Redis-specific health
- `/api/health/redis` - Custom Redis health check

### Metrics
- **Prometheus**: `/actuator/prometheus`
- **Custom Metrics**: Session creation time, Redis response time
- **JVM Metrics**: Memory, GC, threads
- **HTTP Metrics**: Request duration, error rates

### Dashboards
- **Application Dashboard**: Real-time session and Redis metrics
- **Grafana Dashboards**: Pre-configured monitoring dashboards

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Integration Tests with TestContainers
```bash
mvn test -Dtest=RedisIntegrationTest
```

### Performance Testing
```bash
# Built-in performance test via API
curl -X POST http://localhost:8080/api/redis/performance-test
```

## 🚀 Deployment

### AWS ECS Deployment

1. **Build and Push Image**
```bash
# Build image
docker build -t modern-redis-app .

# Tag for ECR
docker tag modern-redis-app:latest <account>.dkr.ecr.<region>.amazonaws.com/modern-redis-app:latest

# Push to ECR
docker push <account>.dkr.ecr.<region>.amazonaws.com/modern-redis-app:latest
```

2. **ECS Task Definition**
```json
{
  "family": "modern-redis-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "512",
  "memory": "1024",
  "containerDefinitions": [
    {
      "name": "modern-redis-app",
      "image": "<account>.dkr.ecr.<region>.amazonaws.com/modern-redis-app:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_REDIS_HOST",
          "value": "your-elasticache-endpoint"
        }
      ],
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 10,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

### Infrastructure as Code

The application includes Terraform modules for:
- ECS Cluster and Service
- ElastiCache Redis Cluster
- Application Load Balancer
- CloudFront Distribution
- Security Groups and IAM Roles

## 🎨 UI Features

### Modern Dashboard
- **Real-time Metrics** - Live session and Redis statistics
- **Interactive Controls** - Session attribute management
- **Health Monitoring** - Visual health indicators
- **Performance Testing** - Built-in performance validation
- **Responsive Design** - Mobile-friendly interface

### Keyboard Shortcuts
- `Ctrl+R` - Refresh session data
- `Ctrl+H` - Test Redis health
- `Ctrl+P` - Run performance test

## 🔒 Security Features

- **Non-root Container** - Runs as unprivileged user
- **Health Checks** - Container and application health validation
- **Circuit Breaker** - Automatic failure handling
- **Input Validation** - Request validation and sanitization
- **Secure Headers** - Security headers configuration

## 📈 Performance

### Optimizations
- **JVM Tuning** - Container-optimized JVM settings
- **Connection Pooling** - Lettuce connection pool configuration
- **Caching Strategy** - Intelligent session caching
- **Async Processing** - Non-blocking operations where possible

### Benchmarks
- **Session Creation**: < 50ms (p95)
- **Redis Operations**: < 10ms (p95)
- **Health Checks**: < 5ms (p95)
- **Memory Usage**: ~200MB baseline

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Add tests for new functionality
4. Ensure all tests pass
5. Submit a pull request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: Check the `/docs` directory
- **Issues**: GitHub Issues
- **Monitoring**: Built-in health checks and metrics

---

**Built with ❤️ for modern cloud-native applications**
