# Etapa 1: Build con Maven
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Cachear dependencias de Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Compilar código fuente empaquetando JAR ejecutable
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime ligero de producción
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear usuario sin privilegios para seguridad empresarial
RUN addgroup -S erpgroup && adduser -S erpuser -G erpgroup
USER erpuser:erpgroup

# Copiar el artefacto compilado
COPY --from=builder /app/target/auth-service-*.jar app.jar

# Variables de entorno y puerto
ENV PORT=8081
EXPOSE 8081

# Sonda de salud y arranque
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
