FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Clean any existing builds and compile
RUN mvn clean compile

# Verify no duplicate classes
RUN find . -name "*.class" -path "*/SessionController*" | wc -l

# Build application
RUN mvn package -DskipTests

# Verify JAR exists
RUN ls -la target/spring-redis-poc-1.0.0.jar

# Run application
EXPOSE 8080
CMD ["java", "-jar", "target/spring-redis-poc-1.0.0.jar"]
