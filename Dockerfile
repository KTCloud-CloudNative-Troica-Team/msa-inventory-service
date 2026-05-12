# syntax=docker/dockerfile:1.7

# ===== Stage 1: build =====
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY inventory/build.gradle.kts inventory/
COPY inventory-service/build.gradle.kts inventory-service/
COPY inventory-event/build.gradle.kts inventory-event/
RUN chmod +x gradlew

# GitHub Packages 인증 (common-libs 의존성 해결). JitPack client-redis는 인증 불필요.
ARG GPR_USER
ARG GPR_TOKEN
RUN if [ -n "$GPR_USER" ] && [ -n "$GPR_TOKEN" ]; then \
      mkdir -p /root/.gradle && \
      echo "gpr.user=$GPR_USER" > /root/.gradle/gradle.properties && \
      echo "gpr.token=$GPR_TOKEN" >> /root/.gradle/gradle.properties ; \
    fi
RUN ./gradlew dependencies --no-daemon || true

COPY inventory/src inventory/src
COPY inventory-service/src inventory-service/src
COPY inventory-event/src inventory-event/src
RUN ./gradlew :inventory-service:bootJar --no-daemon -x test

# ===== Stage 2: extract layers =====
FROM eclipse-temurin:21-jre-alpine AS layers
WORKDIR /app
COPY --from=build /app/inventory-service/build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ===== Stage 3: runtime =====
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
WORKDIR /app

COPY --from=layers /app/dependencies/ ./
COPY --from=layers /app/spring-boot-loader/ ./
COPY --from=layers /app/snapshot-dependencies/ ./
COPY --from=layers /app/application/ ./

EXPOSE 8003 9003
ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
