FROM gradle:8.7-jdk21-alpine AS builder
WORKDIR /app
# Only copy dependency files first for caching
COPY build.gradle settings.gradle ./
# Copy the source code
COPY src/ src/
# Build the jar using the system gradle instead of the wrapper
RUN gradle bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
