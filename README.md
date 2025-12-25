# User Service Spring Boot Project

Spring Boot projects with a pre-configured set of essential dependencies and features. It is designed to help you quickly bootstrap robust, production-ready applications.

## Project Information

- **Group:** `com.quetoquenana`
- **Artifact:** `user-service`
- **Name:** `User Service`
- **Description:** User Service Spring Boot project
- **Package name:** `com.quetoquenana.template`

## Included Dependencies

This template includes the following dependencies by default (as listed in `pom.xml`):

- **Spring Boot Actuator** (`spring-boot-starter-actuator`): Monitoring and management endpoints.
- **Spring Data JPA** (`spring-boot-starter-data-jpa`): Simplifies data access and ORM with JPA.
- **Spring Security** (`spring-boot-starter-security`): Authentication and authorization capabilities.
- **Spring Web** (`spring-boot-starter-web`): Build RESTful web services and web applications.
- **Spring Boot Test** (`spring-boot-starter-test`, test scope): Testing support for Spring Boot applications.
- **Testcontainers** (`testcontainers`, test scope): Containerized integration testing with real databases (PostgreSQL).
- **Spring HATEOAS** (`spring-boot-starter-hateoas`): Hypermedia-driven REST APIs.
- **Flyway Core** (`flyway-core`): Database schema migrations.
- **Flyway PostgreSQL Database Support** (`flyway-database-postgresql`): Flyway support for PostgreSQL.
- **Spring Boot DevTools** (`spring-boot-devtools`, runtime, optional): Rapid development and automatic restarts.
- **Spring Boot Docker Compose** (`spring-boot-docker-compose`, runtime, optional): Run dependent services with Docker Compose.
- **PostgreSQL Driver** (`postgresql`, runtime): Connects to PostgreSQL databases.
- **Lombok** (`lombok`, optional): Simplifies Java code with annotations for boilerplate reduction.
- **Spring Security Test** (`spring-security-test`, test scope): Testing support for Spring Security.
- **Spring Boot Validation** (`spring-boot-starter-validation`): Validation support for DTOs and request payloads.

## Security Configuration

This template uses Spring Security with the following configuration:

- **HTTP Basic Authentication** is enabled for login endpoint
- **JWT** is used for securing API endpoints.
- **Configuration Location:** See `src/main/java/com/quetoquenana/template/config/SecurityConfig.java` for details.

## Internationalization (i18n) Support

This template is ready for multi-language support using Spring Boot's internationalization features:

- **Message Resource Files:**
  - `src/main/resources/messages.properties` (default, English)
  - `src/main/resources/messages_es.properties` (Spanish)
- **Configuration:**
  - See `src/main/java/com/quetoquenana/template/config/I18nConfig.java` for MessageSource and LocaleResolver beans.
- **Usage:**
  - Inject `MessageSource` into your services or controllers and use `messageSource.getMessage("welcome.message", null, locale)` to retrieve localized messages.
  - The default locale is English; you can switch locales using the `SessionLocaleResolver` or by customizing locale resolution.
  - Rest controllers use the `Accept-Language` header to set the locale.
- **Adding More Languages:**
  - Create additional files like `messages_fr.properties` for French, etc.

Example usage in a service/controller:

```java
@Autowired
private MessageSource messageSource;

public String getWelcomeMessage(Locale locale) {
    return messageSource.getMessage("welcome.message", null, locale);
}
```

## Testing

This project includes both unit and integration tests:

- **Unit Tests:** Fast tests that mock dependencies and verify business logic in isolation.
- **Integration Tests:** Use [Testcontainers](https://www.testcontainers.org/) to spin up real PostgreSQL containers for end-to-end testing of the application, including database interactions and REST endpoints.
- **Test Structure:**
  - Unit tests are located in `src/test/java/com/quetoquenana/userservice/controller/` and other relevant packages.
  - Integration tests use `@SpringBootTest` or `@Testcontainers` and are named with the `IT` suffix (e.g., `PersonControllerIT`).

To run all tests:

```bash
./mvnw test
```

Testcontainers will automatically start and stop containers as needed during integration tests.

## Getting Started

1. **Clone this repository** and update the project information as needed.
2. **Configure your database** in the properties files (`application-dev.properties`, `application-prd.properties`).
3. **Run the application:**

   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the API:**
    - The API will be available at `http://localhost:8080/api/`.
    - Swagger UI is available at `http://localhost:8080/swagger-ui.html` for API documentation and testing.
   
5. **Test with Postman:**
   - Import the collection from the `postman/collections` folder and run example requests.

## Customization

- Update the `pom.xml` to add or remove dependencies as needed.
- Modify the package structure and application properties to fit your requirements.
- Extend the execution tracking feature or add new features as needed.

## Use of DTOs for API Requests

This project uses Data Transfer Objects (DTOs) to handle incoming API requests. Instead of exposing or accepting entity/model classes directly in controller endpoints, dedicated DTO classes are used for create and update operations (e.g., `PersonCreateRequest`, `PersonUpdateRequest`, `ProfileCreateRequest`, `ProfileUpdateRequest`, etc.).

**Benefits:**
- Only the intended fields are exposed and accepted from clients, improving security.
- Validation annotations (such as `@NotNull`, `@NotBlank`) are applied directly to DTO fields for robust input validation.
- The service layer is updated to accept these DTOs, and conversion logic is handled in the model or service classes.

**Example:**
When creating or updating a person or profile, the API expects a DTO payload rather than the full entity object. This ensures only updatable fields are processed and sensitive/internal fields are not exposed.

## Exception handling

This project centralizes API error handling with a global Spring MVC exception handler (a class annotated with `@ControllerAdvice`) that converts exceptions into consistent JSON error responses.

### Overview
- A single `@ControllerAdvice` class handles uncaught exceptions, specific domain exceptions, and validation failures.
- Handled exceptions are mapped to appropriate HTTP status codes (4xx for client errors, 5xx for server errors).
- Validation errors from Jakarta Bean Validation (`@Valid`) are returned as structured field errors.

### Error response format
The API returns a standardized JSON error payload. Example:
```json
{
  "timestamp": "2025-11-02T12:34:56.789Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/persons",
  "errors": [
    {
      "field": "email",
      "rejectedValue": "bad-email",
      "message": "must be a well-formed email address"
    }
  ]
}
```

### Generate RSA keys for JWT Signing
To generate RSA keys for JWT signing, you can use the following OpenSSL commands:
```
# Generate a 2048-bit RSA private key
openssl genpkey -algorithm RSA -out user_service_private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl pkcs8 \
  -topk8 \
  -inform PEM \
  -outform DER \
  -nocrypt \
  -in user_service_private_key.pem \
  -out user_service_private_key.der

# Extract the public key from the private key
openssl rsa \
  -pubin \
  -inform PEM \
  -outform DER \
  -in user_service_public_key.pem \
  -out user_service_public_key.der

# To verify the generated keys, you can use:
openssl pkcs8 -inform DER -in user_service_private_key.der -noout -topk8

openssl rsa -pubin -inform DER -in user_service_public_key.der -noout

# To get the Base64 encoded versions of the keys (without line breaks), use:
base64 < user_service_private_key.der | tr -d '\n'
base64 < user_service_public_key.der | tr -d '\n'

# Clean up the PEM files if not needed
rm user_service_private_key.pem user_service_public_key.pem

#Export the Base64 encoded keys as environment variables
```

## License

This template is provided as-is for bootstrapping new Spring Boot projects.
