# Etapa 1: Build con Maven
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cachear dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar código fuente
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime ligero
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S erpgroup && adduser -S erpuser -G erpgroup
USER erpuser:erpgroup

COPY --from=builder /app/target/api-gateway-*.jar app.jar

ENV PORT=8080
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
