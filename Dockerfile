# ═══════════════════════════════════════════════════════════════════════════════
# Ecosalud Backend — Dockerfile multi-stage
#
# Stage 1 (builder): compila el proyecto con Maven y genera el fat-JAR.
# Stage 2 (runtime): imagen JRE mínima que solo ejecuta el JAR ya compilado.
#
# Resultado: imagen final ~120 MB en lugar de ~600 MB con una imagen JDK completa.
#
# Uso local:
#   docker build -t ecosalud-backend .
#   docker run -p 8080:8080 \
#     -e DATABASE_URL=jdbc:postgresql://host:5432/ecosalud \
#     -e DATABASE_USERNAME=postgres \
#     -e DATABASE_PASSWORD=secret \
#     -e JWT_SECRET=base64-secret \
#     -e FRONTEND_URL=http://localhost:5173 \
#     ecosalud-backend
# ═══════════════════════════════════════════════════════════════════════════════

# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Copiar primero solo el pom.xml para aprovechar la caché de capas de Docker.
# Las dependencias solo se re-descargan si pom.xml cambia.
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copiar el código fuente y compilar
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear directorio de uploads para modo disco local (desarrollo / sin Cloudinary).
# En producción con Cloudinary este directorio no se usa pero no causa error.
RUN mkdir -p /app/uploads

# Copiar el JAR desde el stage de build
COPY --from=builder /app/target/*.jar app.jar

# Puerto expuesto (configurable vía env var PORT en Railway)
EXPOSE 8080

# Opciones JVM optimizadas para contenedores:
#   -XX:+UseContainerSupport      → respeta los límites de CPU/memoria del contenedor
#   -XX:MaxRAMPercentage=75.0     → usa hasta el 75% de la RAM disponible para el heap
#   -Djava.security.egd=...       → seed de entropía más rápido (importante en Linux)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
