This document explains recommended ways to run the UserService in different environments (local, dev, stage, prod).

Principles
- Use Spring profiles (application.properties + application-dev.properties, application-prd.properties) to separate environment configuration.
- Keep secrets out of source control. Inject them via environment variables, Docker secrets, or a secret manager (Vault, AWS Secrets Manager, etc.).
- Build immutable artifacts (the fat jar or container image) in CI, push them to a registry, and deploy the same artifact across environments.

Files added
- `Dockerfile` – runtime Docker image that expects the built jar under `target/`.
- `docker-compose.dev.yml` – compose file to bring up the app (built from Dockerfile) and a Postgres DB for local/dev testing.

Dev / Local (fast)
1. Run from IntelliJ: you already do this and it works (recommended for iterative development).
2. Build and run with Maven:
   mvn -DskipTests package
   java -jar target/userservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

3. Run with Docker Compose (matches dev environment):
   mvn -DskipTests package
   docker compose -f docker-compose.dev.yml up --build

This compose file uses environment variables to point the app to the `postgres` service and activates the `dev` profile, so it will load `application-dev.properties`.

Staging / Production (recommended approach)
- CI pipeline should:
  1. Check out code and run tests.
  2. Build the jar: `mvn -DskipTests package`.
  3. Build a container image in CI (docker build) and push to a container registry.
  4. Deploy the image to the environment (Kubernetes, ECS, or similar).

- Configuration in stage/prod:
  - Use the `prod` (or `prd`) Spring profile: set `SPRING_PROFILES_ACTIVE=prd`.
  - Provide DB URL, username and password via environment variables or provider-specific secrets.
  - Do NOT bake credentials into images.

Kubernetes notes (high level)
- Create Deployment for `userservice` with image from registry; use ConfigMap to set non-sensitive environment variables and Secrets for DB credentials and private keys.
- Use readiness and liveness probes pointing to `/actuator/health` (or a lightweight endpoint).
- Configure resource requests/limits and horizontal pod autoscaling.

Operational recommendations
- Expose Actuator endpoints as needed but protect them (use network restrictions and security).
- Configure logging to stdout/stderr and collect logs using your platform's logging solution.
- Add health, metrics (Micrometer), and tracing integrations for observability.

Example env variables the app expects (from codebase / best guess):
- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SPRING_PROFILES_ACTIVE

Troubleshooting
- If the Docker runtime fails due to Java version mismatches, build the jar in CI using a builder image that matches your JDK (or change the `eclipse-temurin` tag in the Dockerfile to the Java version you use).

Next steps / optional improvements
- Add a multi-stage Dockerfile to build the project inside the image (useful for CI-less builds).
- Add a `docker-compose.override.yml` for developer-specific mounts.
- Add Kubernetes manifests or Helm charts for stage/prod deployment.
- Add a small Makefile or scripts to standardize commands used by the team.
# Multi-stage build is omitted: this Dockerfile expects the Spring Boot jar to be present in target/
# Use `mvn -DskipTests package` before building the image, or configure your CI to produce the jar.

ARG JAR_FILE=target/userservice-0.0.1-SNAPSHOT.jar
FROM eclipse-temurin:25-jdk as runtime

WORKDIR /app

# Copy the built jar into the image
COPY ${JAR_FILE} /app/app.jar

EXPOSE 8080

# Use an unbuffered Java output
ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]

