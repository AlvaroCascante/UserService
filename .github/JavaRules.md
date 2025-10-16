# JavaRules.md

## Default Java Coding Rules for Copilot

This document specifies the default set of rules for Copilot to use when generating or reviewing Java code in this repository.

### 1. Code Style
- Use 4 spaces for indentation, no tabs.
- Follow standard Java naming conventions (CamelCase for classes, camelCase for variables and methods).
- Place opening braces `{` on the same line as the declaration.
- Use meaningful names for classes, methods, and variables.

### 2. Documentation
- Add Javadoc comments for all public classes and methods.
- Use inline comments to explain complex logic.

### 3. Structure
- Organize code into appropriate packages by layer (e.g., controller, service, model, repository).
- Keep classes focused on a single responsibility.
- Create interfaces for services.
- Model layer would work as database representation, no DTO's are going to be used.
- For each Model create a ModelNameResponseView class that would contain these two classes
  - `public static class ModelNameList extends ResponseView.Always {}`
  - `public static class ModelNameDetail extends ModelNameList {}`
- Annotate fields in Model classes with `@@Column` even when the column name is the same.
- Annotate Model classes and rest controllers with `@JsonView`, this would make the works instead the DTO's.
- Annotate classes with Lombok annotations.
- Use `@Slf4j` for logging, add informational logs on API calls and error logs for exceptions.
- For rest controllers respond with `ResponseEntity<T>`.
- Include unit test cases for each method in the controller layer.

### 4. Exception Handling
- Use custom exceptions for domain-specific errors.
- Avoid catching generic `Exception` unless absolutely necessary.
- Always log exceptions with meaningful messages.

### 5. Dependency Management
- Use dependency injection for services and repositories.
- Avoid hardcoding configuration values; use properties files or environment variables.

### 6. Testing
- Write unit tests for all business logic using JUnit and Mockito.
- Use integration tests for database and API interactions.
- Ensure all tests pass before merging code.

### 7. Security
- Never log sensitive information (passwords, tokens, etc.).
- Use Spring Security for authentication and authorization.

### 8. Database
- Use JPA repositories for data access.
- Apply Flyway for database migrations.
- Prefer parameterized queries to avoid SQL injection.

### 9. Performance
- Avoid unnecessary object creation in loops.
- Use streams and lambdas where appropriate, but not at the expense of readability.

### 10. Miscellaneous
- Remove unused imports and code.
- Keep methods short and focused.
- Use Lombok annotations to reduce boilerplate, but document their usage.

---
