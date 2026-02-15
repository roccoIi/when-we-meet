# ===== Builder Stage =====
FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

# Copy entire project (wrapper 포함)
COPY . .

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# Build (skip test)
RUN ./gradlew clean build -x test

# ===== Runtime Stage =====
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

RUN useradd -m -u 1000 appuser && chown -R appuser:appuser /app
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]