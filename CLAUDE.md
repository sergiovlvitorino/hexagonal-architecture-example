# CLAUDE.md - Project Memory for AI Agents

This file documents the project history, architecture decisions, and key instructions to help AI agents understand the context of this codebase.

## Project Overview

Educational project demonstrating the **Hexagonal Architecture** (Ports & Adapters) pattern with REST and GraphQL APIs using Java and Spring Boot.

## Upgrade History

### Migration from Java 18 / Spring Boot 2.5.4 to Java 21 / Spring Boot 3.5.9

Performed in February 2026. Key changes:

1. **Java 18 to 21**: Adopted Java Records for `ListCommand` and `SaveCommand`, constructor injection across all components, `var` keyword usage, text blocks in tests.

2. **Spring Boot 2.5.4 to 3.5.9**: Namespace migration from `javax.*` to `jakarta.*` across all files. Updated `@LocalServerPort` import from `spring.boot.web.server` to `spring.boot.test.web.server`.

3. **SPQR (code-first GraphQL) to Spring for GraphQL (schema-first)**: Removed `graphql-spqr-spring-boot-starter` and `graphql-java` dependencies. Created `src/main/resources/graphql/schema.graphqls`. Rewrote `UserGraphQLController` using `@QueryMapping` and `@Argument`. Deleted `UserMapper.java` (no longer needed).

4. **Hibernate 5 to 6**: Removed deprecated `@GenericGenerator` and `@Type(type="uuid-binary")` from `User.java`, replaced with `@GeneratedValue(strategy = GenerationType.UUID)`. Added `@Table(name = "users")` because `user` is a reserved word in H2 2.x.

5. **Dependencies updated**: Lombok 1.18.42, Jsoup 1.22.1, JaCoCo 0.8.14, maven-compiler-plugin 3.15.0. Removed `--enable-preview` flags from compiler and surefire plugins.

6. **CI/CD**: Updated GitHub Actions (`actions/checkout@v4`, `actions/setup-java@v5` with `distribution: temurin`, JDK 21). Removed obsolete `.travis.yml`.

### Security Fixes

Authentication was explicitly excluded (educational project). Fixes applied:

- **H2 console disabled**: `spring.h2.console.enabled=false`
- **GraphQL introspection disabled**: `spring.graphql.schema.introspection.enabled=false`
- **SafeHtml validator improved**: Added `Jsoup.isValid(html, Safelist.none())` check for broader XSS coverage
- **GraphQL input validation**: `MAX_PAGE_SIZE = 1000`, `ALLOWED_ORDER_FIELDS = Set.of("id", "name")`, null checks
- **CORS configuration**: `WebConfig.java` reads origins from `app.cors.allowed-origins` property (default: `http://localhost:8080`)
- **Seed data protection**: `@Profile("!prod")` on `Initialize.java`
- **Constructor injection**: Converted all `@Autowired` field injection to constructor injection
- **Global exception handler**: `GlobalExceptionHandler.java` with `@RestControllerAdvice`

### Test Coverage Improvement

Increased from 6 to 31 tests. Coverage: **98% instructions, 85% branches**.

Tests added:
- **GraphQL** (10 total): normal query, userName filter, negative pageNumber, `@ParameterizedTest` for invalid pageSize values `{0, 5000}`, `@ParameterizedTest` for invalid orderBy values `{"email","createdAt","password"}`, `@ParameterizedTest` for valid orderBy values `{"id","name"}`
- **REST** (10 total): list asc/desc, filter by name, create valid user, XSS with `<html>`, XSS with `<img>`, short name, null name, long name, pagination
- **SafeHtmlValidator** (9 unit tests): null, plain text, `<script>`, `<img>`, `<html>`, `<div>`, encoded HTML entities, plain ampersand, empty string
- **GlobalExceptionHandler** (2 unit tests): IllegalArgumentException, generic Exception

Residual uncovered branches (4): null checks in `UserGraphQLController` for `pageNumber`, `pageSize`, `orderBy`, `asc` — unreachable via HTTP because the GraphQL schema declares these as non-null (`Int!`, `String!`, `Boolean!`).

### Code Quality Improvements

Applied in February 2026 after project analysis:

- **Initialize.java**: replaced 6 repetitive `repository.save()` calls with `IntStream.range(0, SEED_USER_COUNT).mapToObj(...).forEach(repository::save)`
- **WebConfig.java**: externalized CORS allowed origins via `@Value("${app.cors.allowed-origins:http://localhost:8080}")` — no longer hardcoded
- **UserCommandHandler.java**: replaced null-check ternary `command.user() == null ? new User() : command.user()` with `Optional.ofNullable(command.user()).orElseGet(User::new)`
- **application.properties**: added explanatory comments for each property; added `app.cors.allowed-origins` property
- **schema.graphqls**: added full SDL documentation (descriptions for Query, UserPage, User, and all fields)
- **maven.yml**: added Maven dependency cache (`actions/cache@v4`) and Codecov coverage upload (`codecov/codecov-action@v5`); changed `mvn package` to `mvn verify jacoco:report`
- **UserGraphQLControllerTest**: consolidated duplicate error scenario tests into `@ParameterizedTest` with `@ValueSource`

## Architecture

```
src/main/java/com/sergiovitorino/hexagonalarchitectureexample/
  Start.java                                     # @SpringBootApplication entry point
  domain/
    model/User.java                              # JPA entity (@Table "users"), UUID id, String name
    repository/UserRepository.java               # JpaRepository interface
  application/
    command/
      UserCommandHandler.java                    # Handles ListCommand and SaveCommand; uses Optional for null filter
      user/
        ListCommand.java                         # Java Record with validation annotations
        SaveCommand.java                         # Java Record with @SafeHtml
    service/
      UserService.java                           # findAll (paginated, sorted, filtered via Example), save
  ui/
    rest/
      UserRestController.java                    # @RestController, GET/POST /rest/user
    graphql/
      controller/
        UserGraphQLController.java               # @Controller, @QueryMapping findAll, manual validation
  infrastructure/
    Initialize.java                              # Seeds 6 random users via IntStream (@Profile("!prod"))
    GlobalExceptionHandler.java                  # @RestControllerAdvice
    WebConfig.java                               # CORS — origins from app.cors.allowed-origins property
    validations/
      SafeHtml.java                              # Custom constraint annotation
      SafeHtmlValidator.java                     # Jsoup-based XSS validator (Safelist.none() + text equality)
```

## Key Files

- `pom.xml` — Spring Boot 3.5.9 parent, Java 21, all dependencies
- `src/main/resources/application.properties` — GraphiQL enabled, introspection disabled, virtual threads, H2 console disabled, CORS origins
- `src/main/resources/graphql/schema.graphqls` — GraphQL schema with SDL documentation
- `.github/workflows/maven.yml` — CI pipeline with Maven cache and Codecov upload
- `system.properties` — `java.runtime.version=21` (for Heroku-like deployments)

## Build & Run

```bash
# Run the application
mvn spring-boot:run

# Run tests
mvn test

# Run tests with coverage report
mvn test jacoco:report
# Report at: target/site/jacoco/index.html
```

## Known Considerations

- The `user` table is named `users` in the entity (`@Table(name = "users")`) because `user` is a reserved word in H2 2.x.
- `Initialize.java` inserts seed data only when the `prod` profile is NOT active.
- GraphQL errors from `IllegalArgumentException` are handled by Spring GraphQL's error resolver (returns `INTERNAL_ERROR` classification), not by the `GlobalExceptionHandler`.
- The `PageImpl` serialization warning is benign — Spring Data recommends `PagedModel` for stable JSON but the current format works for this educational project.
- Virtual threads are enabled via `spring.threads.virtual.enabled=true`.
- CORS allowed origins are configured via `app.cors.allowed-origins` in `application.properties` (supports comma-separated values for multiple origins).

## Git Conventions

- Main branch: `master`
- Active branch: `security-fixes-and-test-coverage`
- Git user: Sergio Vitorino <sergiovlvitorino@gmail.com>
- Commit messages: descriptive, in English
- Co-authored commits with AI agents include the `Co-Authored-By` trailer
