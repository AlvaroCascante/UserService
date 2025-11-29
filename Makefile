# Makefile for common local developer tasks

.PHONY: package run docker-build docker-run compose-up clean

package:
	mvn -DskipTests package

run: package
	java -jar target/userservice-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev

docker-build:
	docker build -f Dockerfile.multi -t userservice:dev .

docker-run:
	docker run --rm -p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=dev \
		-e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/user_service_dev \
		-e SPRING_DATASOURCE_USERNAME=postgres \
		-e SPRING_DATASOURCE_PASSWORD=postgres \
		userservice:dev

compose-up:
	docker compose -f docker-compose.dev.yml up --build

clean:
	mvn -q clean

