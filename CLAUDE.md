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
- **SafeHtml validator simplified**: Uses `Jsoup.clean()` with `Parser.unescapeEntities()` for XSS prevention
- **orderBy validation centralized**: `ALLOWED_ORDER_FIELDS = Set.of("id", "name")` in `UserCommandHandler` protects both REST and GraphQL
- **GraphQL input validation**: `maxPageSize` externalized via `@Value("${pagination.max-page-size:1000}")` in `UserCommandHandler` constructor, input validation centralized in handler
- **CORS configuration**: `WebConfig.java` origins externalized via `@Value("${cors.allowed-origins}")`
- **Seed data protection**: `@Profile("!prod")` on `Initialize.java`
- **Constructor injection**: Converted all `@Autowired` field injection to constructor injection
- **Global exception handler**: `GlobalExceptionHandler.java` with `@RestControllerAdvice`, handles `MethodArgumentNotValidException`, `IllegalArgumentException`, `NoResourceFoundException`, `HttpMessageNotReadableException`, `MethodArgumentTypeMismatchException`, and generic `Exception`

### Test Coverage Improvement

Increased from 6 to 77 tests. Coverage: **98%+ instructions, 85%+ branches**. JaCoCo `check` goal enforces minimum 80% line coverage.

Tests:
- **GraphQL** (7 slice -- `@GraphQlTest`): normal query, userName filter, negative pageNumber, pageSize exceeding limit, pageSize zero, invalid orderBy, orderBy by id
- **REST** (15 slice -- `@WebMvcTest`): list asc/desc, filter by name, orderBy id, invalid orderBy, create valid user, XSS with `<html>`, XSS with `<img>`, short name, null name, long name, min/max boundary names, pagination metadata
- **SafeHtmlValidator** (9 unit): null, plain text, `<script>`, `<img>`, `<html>`, `<div>`, encoded HTML entities, plain ampersand, empty string
- **GlobalExceptionHandler** (5 unit): MethodArgumentNotValidException, IllegalArgumentException, generic Exception, HttpMessageNotReadableException, MethodArgumentTypeMismatchException
- **UserService** (5 unit): paginated results, filter via Example, ascending sorting, descending sorting, save delegation
- **UserCommandHandler** (10 unit): list delegation, null user fallback, save with name, invalid orderBy, valid orderBy id, negative pageNumber, zero pageSize, exceeding max pageSize, blank orderBy, null asc
- **User** (8 unit): equals/hashCode by id, different id inequality, null id equality, reflexivity, null comparison, toString, constructors
- **UserEntity** (8 unit): toDomain, fromDomain, constructors, roundtrip, equals by id
- **UserRepositoryAdapter** (5 unit): findAll without filter, findAll with filter, mapping, save, save with id
- **Actuator** (2 integration): health endpoint returns UP, non-exposed endpoint returns 404
- **CORS** (3 integration): preflight allowed origin, GET allowed origin, GET disallowed origin

Residual uncovered branches (4): null checks in `UserGraphQLController` for `pageNumber`, `pageSize`, `orderBy`, `asc` - unreachable via HTTP because the GraphQL schema declares these as non-null (`Int!`, `String!`, `Boolean!`).

### Technical Debt Cleanup

Performed in April 2026. Addressed 11 technical debts and increased tests from 28 to 54 (now 77 after hexagonal refactoring and adapter/entity tests).

Code quality improvements:
- **CORS origin externalized**: `WebConfig.java` uses `@Value("${cors.allowed-origins}")` instead of hardcoded localhost
- **@Transactional added**: `UserService.findAll` is `@Transactional(readOnly = true)`, `save` is `@Transactional`
- **Validation centralized**: `@Max(1000)` on `ListCommand.pageSize`, `ALLOWED_ORDER_FIELDS` moved from GraphQL controller to `UserCommandHandler` (protects both REST and GraphQL)
- **Configuration externalized**: `pagination.max-page-size` and `cors.allowed-origins` in `application.properties`
- **Exception handling expanded**: Added handlers for `HttpMessageNotReadableException` and `MethodArgumentTypeMismatchException`
- **SafeHtmlValidator simplified**: Single `Jsoup.clean()` + `Parser.unescapeEntities()` replaces dual-call pattern
- **Optional pattern**: `UserCommandHandler` uses `Optional.ofNullable().orElseGet()` instead of ternary null check
- **SLF4J logging**: `@Slf4j` on `GlobalExceptionHandler` and `UserService`
- **JaCoCo threshold**: `check` goal enforces minimum 80% line coverage
- **User entity**: `@EqualsAndHashCode(of = "id")` instead of `@Data` (fixes JPA identity issues)
- **Initialize batch**: `saveAll()` instead of 6 individual `save()` calls
- **Test fixes**: `restTemplete` typo corrected, non-descriptive test names renamed

### Performance Improvements

Performed in April 2026. Optimized JPA, connection pool, HTTP caching, and API layer.

- **Open Session in View disabled**: `spring.jpa.open-in-view=false` prevents Hibernate session from staying open during JSON serialization, avoiding accidental lazy loading in the presentation layer
- **Hibernate batching**: `batch_size=25`, `order_inserts=true`, `order_updates=true` — groups INSERT/UPDATE statements into single JDBC batches instead of individual roundtrips
- **DTO `UserResponse`**: New Java Record `UserResponse(UUID id, String name)` in `application.dto` package. Both REST and GraphQL controllers now return `UserResponse` via `Page.map(UserResponse::from)` instead of exposing the `User` JPA entity directly. Decouples the API contract from the persistence model
- **Index on `name` column**: `@Index(name = "idx_user_name", columnList = "name")` added to `@Table` on `UserEntity.java`. Benefits both filtering (`ExampleMatcher` with `withIgnoreCase()`) and sorting (`ORDER BY name`)
- **HikariCP tuning**: `maximum-pool-size=20`, `minimum-idle=5`, `idle-timeout=300000`, `connection-timeout=20000`. Sized for concurrent load with virtual threads enabled
- **HTTP cache headers**: `GET /rest/user` returns `Cache-Control: max-age=30` via `ResponseEntity` with `CacheControl.maxAge(30, TimeUnit.SECONDS)`. Allows clients and proxies to cache responses for 30 seconds

### Hexagonal Architecture Refactoring

Performed in April 2026. Completed the Ports & Adapters pattern, decoupled domain from infrastructure, migrated to slice tests, and added observability.

Hexagonal architecture completion:
- **Domain model purified**: `User.java` is now a plain POJO (no JPA annotations). All persistence annotations moved to `UserEntity.java` in `infrastructure/persistence/`
- **Output port created**: `UserRepositoryPort` interface in `domain/repository/` defines the contract for persistence operations (`findAll`, `save`)
- **Adapter implemented**: `UserRepositoryAdapter` in `infrastructure/persistence/` implements `UserRepositoryPort`, encapsulates `Example`/`ExampleMatcher` logic, and maps between `User` (domain) and `UserEntity` (JPA)
- **Repository relocated**: `UserRepository` (Spring Data JPA) moved from `domain/repository/` to `infrastructure/persistence/` -- domain layer has zero JPA dependencies
- **Service decoupled**: `UserService` depends only on `UserRepositoryPort`, never on JPA directly

Application layer improvements:
- **ListCommand decoupled**: Uses `String userName` instead of `User` entity reference
- **Validation centralized**: `UserCommandHandler` validates `pageNumber`, `pageSize`, `orderBy`, `asc` with explicit null/range checks. `maxPageSize` injected via `@Value` in constructor (removed from GraphQL controller)
- **GraphQL controller simplified**: `UserGraphQLController` is now a pure delegator -- no validation logic, no `@Value` fields
- **Constructor injection complete**: All `@Value` annotations moved from field to constructor injection (`UserCommandHandler`, `WebConfig`). Zero field injection in the entire project

Infrastructure improvements:
- **Database constraint**: `@Column(length = 100)` on `UserEntity.name`, aligned with `@Size(max = 100)` on `SaveCommand`
- **Observability**: Spring Boot Actuator added, exposing only `health` and `info` endpoints with `show-details=never`
- **404 handling**: `NoResourceFoundException` handler added to `GlobalExceptionHandler` (previously returned 500)
- **Package reorganization**: `infrastructure/` subpackages: `config/`, `exception/`, `seed/`, `persistence/`. `SafeHtml` and `SafeHtmlValidator` moved from `infrastructure/validation/` to `application/validation/` so that `SaveCommand` (application layer) can reference them without violating the hexagonal dependency direction

Test improvements:
- **Slice tests**: REST tests migrated from `@SpringBootTest` to `@WebMvcTest` with `MockMvc` and `@MockitoBean`. GraphQL tests migrated to `@GraphQlTest` with `GraphQlTester` and `@MockitoBean`
- **DTO contract**: REST and GraphQL tests deserialize `UserResponse` instead of `User` (validates the real API contract)
- **CORS tests**: 3 integration tests (preflight allowed, GET allowed, GET disallowed origin)
- **Actuator tests**: 2 integration tests (health endpoint UP, non-exposed endpoint returns 404)
- **UserCommandHandler tests expanded**: From 5 to 10 tests covering all validation branches (negative pageNumber, zero pageSize, exceeding max pageSize, blank orderBy, null asc)
- **Dependency**: `spring-graphql-test` added to `pom.xml` (scope test)

## Architecture

```
src/main/java/com/sergiovitorino/hexagonalarchitectureexample/
  Start.java                                     # @SpringBootApplication entry point
  domain/
    model/User.java                              # Pure POJO domain model, UUID id, String name, @EqualsAndHashCode(of = "id"), no JPA annotations
    repository/UserRepositoryPort.java           # Output port interface (findAll, save) -- domain contract for persistence
  application/
    command/
      UserCommandHandler.java                    # Handles ListCommand and SaveCommand, validates all inputs, @Value maxPageSize via constructor
      user/
        ListCommand.java                         # Java Record with validation annotations, uses String userName (not entity)
        SaveCommand.java                         # Java Record with @SafeHtml
    dto/
      UserResponse.java                          # Java Record DTO (UUID id, String name), decouples API from domain model
    validation/
      SafeHtml.java                              # Custom @SafeHtml constraint annotation
      SafeHtmlValidator.java                     # Jsoup XSS validator
    service/
      UserService.java                           # findAll (paginated, sorted, filtered), save. Depends on UserRepositoryPort. @Transactional, @Slf4j
  ui/
    rest/
      UserRestController.java                    # @RestController, GET/POST /rest/user, CacheControl on GET, returns UserResponse
    graphql/
      controller/
        UserGraphQLController.java               # @Controller, @QueryMapping findAll, pure delegator (no validation), returns UserResponse
  infrastructure/
    config/
      WebConfig.java                             # CORS configuration (origins from @Value via constructor)
    exception/
      GlobalExceptionHandler.java                # @RestControllerAdvice, @Slf4j, 6 exception handlers (incl. NoResourceFoundException)
    seed/
      Initialize.java                            # Seeds 6 random users (@Profile("!prod"))
    persistence/
      UserEntity.java                            # JPA entity (@Table "users"), UUID id, @Column(length=100) name, @Index on name, toDomain()/fromDomain() mappers
      UserRepository.java                        # JpaRepository<UserEntity, UUID> interface
      UserRepositoryAdapter.java                 # Implements UserRepositoryPort, encapsulates Example/ExampleMatcher, maps User<->UserEntity
```

## Key Files

- `pom.xml` - Spring Boot 3.5.9 parent, Java 21, all dependencies (incl. `spring-boot-starter-actuator`, `spring-graphql-test`)
- `src/main/resources/application.properties` - GraphiQL enabled, introspection disabled, virtual threads enabled, H2 console disabled, CORS origins, pagination max page size, OSIV disabled, Hibernate batching, HikariCP tuning, Actuator config
- `src/main/resources/graphql/schema.graphqls` - GraphQL schema (Query: findAll with pagination)
- `src/main/java/.../domain/repository/UserRepositoryPort.java` - Output port interface (the hexagonal boundary)
- `src/main/java/.../infrastructure/persistence/UserEntity.java` - JPA entity with `toDomain()`/`fromDomain()` mappers
- `src/main/java/.../infrastructure/persistence/UserRepositoryAdapter.java` - Adapter implementing the output port
- `.github/workflows/maven.yml` - CI pipeline (checkout@v4, setup-java@v5, temurin JDK 21)
- `system.properties` - `java.runtime.version=21` (for Heroku-like deployments)

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

- The `user` table is named `users` in `UserEntity` (`@Table(name = "users")`) because `user` is a reserved word in H2 2.x.
- `User.java` (domain) is a pure POJO with no JPA annotations. `UserEntity.java` (infrastructure) is the JPA entity. They are mapped via `toDomain()` and `fromDomain()` methods in `UserEntity`.
- `UserService` and `UserCommandHandler` depend only on `UserRepositoryPort` (domain interface), never on Spring Data or JPA classes. The `UserRepositoryAdapter` bridges the gap.
- `Initialize.java` inserts seed data only when the `prod` profile is NOT active.
- GraphQL errors from `IllegalArgumentException` are handled by Spring GraphQL's error resolver (returns `INTERNAL_ERROR` classification), not by the `GlobalExceptionHandler`.
- The `PageImpl` serialization warning is benign - Spring Data recommends `PagedModel` for stable JSON but the current format works for this educational project.
- Virtual threads are enabled via `spring.threads.virtual.enabled=true`.
- Open Session in View is disabled (`spring.jpa.open-in-view=false`). Adding lazy-loaded relationships requires loading them within `@Transactional` boundaries in the adapter to avoid `LazyInitializationException`.
- `GET /rest/user` returns `Cache-Control: max-age=30`. Clients may see data up to 30 seconds stale. Adjust or remove if real-time data is needed.
- `UserResponse` DTO is the API contract. New fields added to `User` domain model must be explicitly added to both `UserEntity` (persistence) and `UserResponse` (API) to be stored and exposed.
- `@Column(length = 100)` on `UserEntity.name` is aligned with `@Size(max = 100)` on `SaveCommand`. If you change one, update the other.
- Spring Boot Actuator exposes only `health` and `info` endpoints. Adding new endpoints requires updating `management.endpoints.web.exposure.include` in `application.properties`.
- `SafeHtml` and `SafeHtmlValidator` are in `application/validation/` (not in `infrastructure/`) so that `SaveCommand` (application layer) can use them without violating the hexagonal dependency direction (application must not depend on infrastructure).

## Git Conventions

- Main branch: `master`
- Git user: Sergio Vitorino <sergiovlvitorino@gmail.com>
- Commit messages: descriptive, in English
- Co-authored commits with AI agents include the `Co-Authored-By` trailer
