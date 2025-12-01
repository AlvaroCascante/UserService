# Replaced empty Dockerfile with a multi-stage build for Maven + Spring Boot
# Stage 1: build with Maven
FROM maven:3.9.4-eclipse-temurin-17 AS build
WORKDIR /workspace

# Copy only the files needed for dependency resolution first for better cache
COPY pom.xml .

# Copy source and build
COPY src ./src

# Build the jar (skip tests in CI by default; you can remove -DskipTests for stricter builds)
RUN mvn -B -DskipTests package

# Stage 2: runtime image
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Copy the fat jar produced by Maven. Adjust if your artifact name differs.
COPY --from=build /workspace/target/*.jar ./app.jar

# Optional Java opts
ENV JAVA_OPTS=""
# Default port; Railway injects PORT env at runtime. We still expose 8080 for clarity.
ENV PORT 8080
EXPOSE 8080

# Start the application and bind server.port to the PORT env var if provided
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dserver.port=${PORT:-8080} -jar /app/app.jar"]
