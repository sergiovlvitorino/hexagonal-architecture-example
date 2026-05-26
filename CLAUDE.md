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

Increased from 6 to 77 tests (later grown to 161 — see Post-Static-Analysis section). Coverage: **98%+ instructions, 85%+ branches**. JaCoCo `check` goal enforces minimum 80% line coverage.

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

### CRUD Completion + OpenAPI

Performed in May 2026. Implemented findById, update, and delete operations across REST and GraphQL, added OpenAPI docs, PIT mutation testing, and addressed code review feedback.

CRUD operations added:
- **findById**: `UserRepositoryPort.findById(UUID)`, `UserRepositoryAdapter` implementation, `UserService.findById` (`@Transactional(readOnly=true)`), `UserNotFoundException` in `domain/exception/`, `GET /rest/user/{id}` endpoint, `findById(id: ID!): User!` GraphQL query
- **update**: `UpdateCommand` record (`@NotNull id`, `@NotBlank @SafeHtml @Size(min=5,max=100) name`), `UserService.update`, `PUT /rest/user/{id}`, `updateUser` GraphQL mutation
- **delete**: `DeleteCommand` record (`@NotNull id`), `UserService.delete` (uses `existsById` before `deleteById` to avoid loading the entity), `DELETE /rest/user/{id}` returns 204, `deleteUser` GraphQL mutation returns `Boolean!`

OpenAPI / Swagger:
- `springdoc-openapi-starter-webmvc-ui` added to `pom.xml`
- `GET /swagger-ui.html` and `GET /v3/api-docs` exposed
- `@Operation`, `@ApiResponse`, `@Parameter` on `UserRestController` endpoints

Validation and error handling:
- `@Validated` on `UserCommandHandler` class + `@Valid` on `handle(UpdateCommand)` parameter enables Bean Validation for method-level arguments
- `GlobalExceptionHandler` handles `ConstraintViolationException` returning 400 with field/message error list
- `GlobalExceptionHandler` handles `UserNotFoundException` returning 404

Mutation testing:
- PIT (`pitest-maven` + `pitest-junit5-plugin`) added to `pom.xml`, mutation score **97%** (36/37 killed)

Code review feedback (May 2026):
- `UserServiceFindUpdateDeleteTest.delete` uses `inOrder(repository)` to assert `findById` called before `deleteById`
- `findById(id: ID!): User!` declared non-null in schema (previously `User`, nullable)
- `GlobalExceptionHandler.handleConstraintViolation` added with unit test
- `@Validated` + `@Valid` wired on `UserCommandHandler.handle(UpdateCommand)`

Test count: **161 tests** total. Mutation score: 97%.

### Post-Static-Analysis Fixes (May 2026)

Performed in May 2026. Addressed 13 tasks from static analysis.

- **`UpdateCommand` min=5**: `@Size(min=5,max=100)` on name. `V1__init.sql` CHECK constraint updated to `>= 5`. `PUT /rest/user/{id}` now uses `UpdateUserRequest` DTO (not `SaveCommand`) for request body.
- **GraphQL exception mapping**: `GraphQlExceptionResolver` (`infrastructure/graphql/`) extends `DataFetcherExceptionResolverAdapter`. Maps `UserNotFoundException` → `NOT_FOUND`, `DomainValidationException`/`IllegalArgumentException` → `BAD_REQUEST`, `ConstraintViolationException` → `BAD_REQUEST`. Covered by `GraphQlExceptionResolverIntegrationTest`.
- **CORS X-Correlation-Id**: `WebConfig.allowedHeaders` includes `"X-Correlation-Id"`. Covered by new test in `WebConfigCorsTest`.
- **MDC GraphQL TODO**: `GraphQlCorrelationInterceptor` has explicit TODO documenting that `doFirst`/`doFinally` run on the reactor thread; with virtual threads the data fetcher thread may not see MDC — solution is `ContextRegistry`/`ThreadLocalAccessor`.
- **`UserService.delete` uses `existsById`**: Added `existsById(UUID)` to `UserRepositoryPort`, `UserRepositoryAdapter`, and `UserService.delete`. No longer loads the entity unnecessarily. Test updated with `inOrder(existsById, deleteById)`.
- **`Initialize` readable names**: Seeds `"User-1-<6hex>"` through `"User-6-<6hex>"` (length >= 5, human-readable).
- **`RateLimitProperties.cacheMaximumSize`**: New field (default 100000), used by `RateLimitFilter`. Property `ratelimit.cache-maximum-size` in `application.properties`.
- **Dead code Relay removed**: Deleted `UserConnection`, `UserEdge`, `PageInfo` DTOs and `CursorCodec` + its test. Zero references remain.
- **`bucket4j-core` bump**: 8.14.0 does not exist in Maven Central — kept at 8.10.1.
- **`schema.graphqls` UUID comments**: `# ID! expects UUID string` comment added above `findById`, `updateUser`, `deleteUser`.
- **`logback-spring.xml` scan removed**: Root `<configuration>` no longer has `scan="true"` — no dynamic reload in any profile (including prod).

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
    exception/
      UserNotFoundException.java                 # Thrown by findById/update/delete when user not found (mapped to 404 / NOT_FOUND)
      DomainValidationException.java             # Domain validation error (mapped to 400 / BAD_REQUEST in REST and GraphQL)
    repository/UserRepositoryPort.java           # Output port interface (findAll, save, findById, existsById, deleteById) -- domain contract for persistence
  application/
    command/
      UserCommandHandler.java                    # @Validated, handles List/Save/Update/Delete commands; @Valid on handle(UpdateCommand). @Value maxPageSize via constructor
      user/
        ListCommand.java                         # Java Record with validation annotations, uses String userName (not entity)
        SaveCommand.java                         # Java Record with @SafeHtml, @Size(min=5,max=100), @NotEmpty
        UpdateCommand.java                       # Java Record: @NotNull id, @NotBlank @SafeHtml @Size(min=5,max=100) name
        DeleteCommand.java                       # Java Record: @NotNull UUID id
    dto/
      UserResponse.java                          # Java Record DTO (UUID id, String name), decouples API from domain model
      UpdateUserRequest.java                     # Java Record DTO used as PUT /rest/user/{id} request body (name-only); REST controller builds UpdateCommand from path id + body. Keeps UpdateCommand internal and the REST contract explicit (separate from SaveCommand)
    event/
      UserCreatedEvent.java                      # Java Record published by UserService after save
      UserUpdatedEvent.java                      # Java Record published by UserService after update
      UserDeletedEvent.java                      # Java Record published by UserService after delete
    validation/
      SafeHtml.java                              # Custom @SafeHtml constraint annotation
      SafeHtmlValidator.java                     # Jsoup XSS validator
    service/
      UserService.java                           # findAll, save, findById, update, delete. Depends on UserRepositoryPort. Publishes UserCreated/Updated/DeletedEvent via ApplicationEventPublisher. @Transactional, @Slf4j
  ui/
    rest/
      UserRestController.java                    # @RestController GET/POST /rest/user, GET/PUT/DELETE /rest/user/{id}; PUT consumes UpdateUserRequest; @Operation annotations; returns UserResponse
    graphql/
      controller/
        UserGraphQLController.java               # @Controller, @QueryMapping findAll/findById, @MutationMapping updateUser/deleteUser, pure delegator
  infrastructure/
    config/
      WebConfig.java                             # CORS configuration (origins via @Value); allowedHeaders includes X-Correlation-Id
      GraphQLConfig.java                         # GraphQL runtime wiring (instrumentation, depth/complexity limits)
      OpenApiConfig.java                         # springdoc OpenAPI metadata bean
      RateLimitConfig.java                       # Builds bucket4j buckets backed by Caffeine cache (sized via RateLimitProperties)
      RateLimitFilter.java                       # Servlet filter applying per-client rate limit using the configured cache
      RateLimitProperties.java                   # @ConfigurationProperties("ratelimit"): capacity, refill, cacheMaximumSize (default 100000)
      SecurityHeadersFilter.java                 # Adds HSTS, X-Content-Type-Options, X-Frame-Options, Referrer-Policy, CSP
    exception/
      GlobalExceptionHandler.java                # @RestControllerAdvice, @Slf4j, REST handlers (incl. ConstraintViolationException, UserNotFoundException, DomainValidationException)
    graphql/
      GraphQlExceptionResolver.java              # extends DataFetcherExceptionResolverAdapter; maps UserNotFoundException -> NOT_FOUND; DomainValidationException / IllegalArgumentException / ConstraintViolationException -> BAD_REQUEST
    seed/
      Initialize.java                            # Seeds 6 readable users "User-N-<6hex>" (@Profile("!prod"))
    observability/
      UserMetrics.java                           # Micrometer Counters: users_created_total, users_updated_total, users_deleted_total
      UserMetricsEventListener.java              # @TransactionalEventListener(AFTER_COMMIT) consuming application/event records, increments counters only after commit
    web/
      CorrelationIdFilter.java                   # OncePerRequestFilter, validates/propagates X-Correlation-Id header + MDC; static sanitize() reused by GraphQL interceptor
      GraphQlCorrelationInterceptor.java         # WebGraphQlInterceptor, propagates correlation-id in GraphQL requests (TODO: virtual-thread MDC via ContextRegistry)
    persistence/
      UserEntity.java                            # JPA entity (@Table "users"), UUID id, @Column(length=100) name, @Index on name, toDomain()/fromDomain() mappers
      UserRepository.java                        # JpaRepository<UserEntity, UUID> interface
      UserRepositoryAdapter.java                 # Implements UserRepositoryPort (findAll, save, findById, existsById, deleteById), encapsulates Example/ExampleMatcher, maps User<->UserEntity
```

## Key Files

- `pom.xml` - Spring Boot 3.5.9 parent, Java 21, all dependencies (incl. `spring-boot-starter-actuator`, `spring-graphql-test`)
- `src/main/resources/application.properties` - GraphiQL enabled, introspection disabled, virtual threads enabled, H2 console disabled, CORS origins, pagination max page size, OSIV disabled, Hibernate batching, HikariCP tuning, Actuator config
- `src/main/resources/graphql/schema.graphqls` - GraphQL schema (Query: findAll with pagination)
- `src/main/java/.../domain/repository/UserRepositoryPort.java` - Output port interface (the hexagonal boundary)
- `src/main/java/.../infrastructure/persistence/UserEntity.java` - JPA entity with `toDomain()`/`fromDomain()` mappers
- `src/main/java/.../infrastructure/persistence/UserRepositoryAdapter.java` - Adapter implementing the output port
- `src/main/java/.../infrastructure/graphql/GraphQlExceptionResolver.java` - GraphQL error mapping (NOT_FOUND / BAD_REQUEST classifications)
- `src/main/java/.../infrastructure/config/RateLimitFilter.java` + `RateLimitProperties.java` - bucket4j-based rate limit, Caffeine cache sized by `ratelimit.cache-maximum-size`
- `src/main/java/.../infrastructure/config/SecurityHeadersFilter.java` - HSTS / CSP / X-Frame-Options / X-Content-Type-Options / Referrer-Policy
- `src/main/java/.../application/dto/UpdateUserRequest.java` - REST PUT request body for `/rest/user/{id}`
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

### PostgreSQL + Flyway (US-06)

Performed in May 2026. Added PostgreSQL support for production, Flyway schema management, and Testcontainers integration tests.

Changes:
- **`pom.xml`**: Added `org.postgresql:postgresql` (runtime), `org.flywaydb:flyway-core`, `org.flywaydb:flyway-database-postgresql`, `org.testcontainers:postgresql` (test), `org.testcontainers:junit-jupiter` (test), `org.springframework.boot:spring-boot-testcontainers` (test). Added Maven profile `it` with `maven-failsafe-plugin` to run ITs separately.
- **`src/main/resources/db/migration/V1__init.sql`**: Creates `users` table (UUID PK, name VARCHAR(100) NOT NULL) and `idx_user_name` index. Flyway manages schema in prod.
- **`src/main/resources/application-prod.properties`**: PostgreSQL datasource via env vars `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`; `ddl-auto=validate`; `flyway.enabled=true`.
- **`src/main/resources/application.properties`** (dev): `spring.flyway.enabled=false` — H2 uses `ddl-auto=update`, Flyway not needed in dev.
- **`src/test/resources/application-it.properties`**: `flyway.enabled=true`, `ddl-auto=validate` — used by Testcontainers IT.
- **`UserRepositoryPostgresIT.java`**: `@SpringBootTest` + `@Testcontainers` + `@ServiceConnection`. Tests: save, findById, findById not found, findAll with filter, deleteById, deleteById not found, Flyway V1 applied (`flyway_schema_history`).

Production environment variables required:
- `DB_URL` — JDBC URL, e.g. `jdbc:postgresql://host:5432/dbname`
- `DB_USER` — database username
- `DB_PASSWORD` — database password

Running integration tests (requires Docker):
```bash
mvn verify -Pit
```

## PostgreSQL + Flyway (US-06)

Performed in May 2026. Added production-ready persistence with PostgreSQL and Flyway migrations, plus Testcontainers integration tests.

Infrastructure changes:
- **PostgreSQL driver**: `postgresql` dependency (runtime scope) added to `pom.xml`
- **Flyway**: `flyway-core` + `flyway-database-postgresql` dependencies added. Migration `V1__init.sql` creates `users` table with `CONSTRAINT chk_name_min_length CHECK (length(name) >= 3)` and `idx_user_name` index
- **application-prod.properties**: datasource reads from env vars `${DB_URL}`, `${DB_USER}`, `${DB_PASSWORD}`. Flyway configured with `validate-on-migrate=true` and `clean-disabled=true` (baseline-on-migrate NOT set — default false to prevent masking schema deviations). HikariCP `socketTimeout=30`, JPA `query.timeout=10000`

Integration tests:
- **`UserRepositoryPostgresIT`**: `@SpringBootTest` + `@Testcontainers` + `@ServiceConnection`. Uses `postgres:16-alpine` container. Annotated with `@Transactional` for test isolation. Profiles `{"it","prod"}` so `Initialize` seed is suppressed via `@Profile("!prod")`. Tests: Flyway migration applied, save, findById, findById not found, findAll with filter, deleteById, deleteById not found, findAll order descending, index `idx_user_name` exists
- **Profile `it`**: `application-it.properties` overrides datasource placeholders so env vars are not required in test environment

Running integration tests:
```bash
mvn verify -Pit
```

Environment variables required in production:
- `DB_URL` — JDBC URL (e.g., `jdbc:postgresql://host:5432/dbname`)
- `DB_USER` — database username
- `DB_PASSWORD` — database password

CI:
- Job `integration-tests-postgres` in `.github/workflows/maven.yml` runs after `build` (`needs: build`). Uses `mvn verify -Pit -DskipTests`. GitHub Actions runners have Docker available for Testcontainers.

## Observabilidade (US-07)

Performed in May 2026. Added Prometheus metrics, structured JSON logs, and correlation-id propagation.

Changes:
- **`pom.xml`**: Added `io.micrometer:micrometer-registry-prometheus` (runtime) and `net.logstash.logback:logstash-logback-encoder:8.0`
- **`application.properties`**: Exposes `prometheus` endpoint — `management.endpoints.web.exposure.include=health,info,prometheus`, `management.endpoint.prometheus.enabled=true`, `management.metrics.tags.application=hexagonal-architecture-example`
- **`application-prod.properties`**: Same prometheus settings plus `management.server.port=9090` (internal port, not public 8080)
- **`UserMetrics`** (`infrastructure/observability/`): Spring `@Component` with three Micrometer Counters: `users_created_total`, `users_updated_total`, `users_deleted_total`. Counters are incremented by `UserMetricsEventListener` after transaction commit.
- **`UserMetricsEventListener`** (`infrastructure/observability/`): `@TransactionalEventListener(AFTER_COMMIT, fallbackExecution=true)` — listens for `UserCreatedEvent`, `UserUpdatedEvent`, `UserDeletedEvent` published by `UserService`. Counters only increment after successful commit, preventing inflation from rolled-back transactions.
- **`application/event/`**: `UserCreatedEvent`, `UserUpdatedEvent`, `UserDeletedEvent` — plain Java records. `UserService` publishes via Spring's `ApplicationEventPublisher` (standard Spring API, not infrastructure-specific).
- **`logback-spring.xml`**: Profile `!prod` (all non-prod profiles: dev, test, IT, default) — plain colorido console with `%X{correlationId}`. Profile `prod` — JSON via `LoggingEventCompositeJsonEncoder` with fields `@timestamp`, `level`, `logger`, `message`, `thread`, `mdc` (includes `correlationId`), `stack_trace`.
- **`CorrelationIdFilter`** (`infrastructure/web/`): `OncePerRequestFilter` at `Ordered.HIGHEST_PRECEDENCE`. Validates `X-Correlation-Id` header against `Pattern "^[a-zA-Z0-9\\-]{1,64}$"` — rejects null/blank/> 64 chars/invalid chars and generates a new UUID (prevents log injection). Static `sanitize()` method reused by `GraphQlCorrelationInterceptor`.
- **`GraphQlCorrelationInterceptor`** (`infrastructure/web/`): Implements `WebGraphQlInterceptor`. Delegates header validation to `CorrelationIdFilter.sanitize()`. Propagates correlation-id into MDC via `doFirst`/`doFinally` reactive hooks.

Tests added:
- `CorrelationIdFilterTest` (9 unit): header preserved, UUID generated when absent, MDC cleared after request, MDC cleared on chain exception, empty header generates UUID, MDC populated during chain, `\n` in header rejected, header > 64 chars rejected, chars outside `[a-zA-Z0-9-]` rejected
- `UserMetricsEventListenerTest` (3 unit): onUserCreated increments created counter, onUserUpdated increments updated counter, onUserDeleted increments deleted counter
- `ActuatorPrometheusIntegrationTest` (3 integration): 200 response, JVM metrics present, `users_total` counter after create

Architecture notes:
- `UserService` no longer imports `UserMetrics` — hexagonal boundary respected. The event-driven approach decouples application layer from infrastructure observability.
- `UserMetrics` is called only from `UserMetricsEventListener` (infra→infra), zero application-layer dependency on Micrometer.
- Micrometer 1.15.x + Prometheus client 1.x (OpenMetrics) exposes `users_created_total` as `users_total` (the client treats `_total` as a type suffix for the base name `users`).
- MDC reactive propagation in GraphQL (`doFirst`/`doFinally`) has known limitations with virtual threads: the data fetcher runs on a different thread and may not see MDC set in the reactor hooks. Solution is `ContextRegistry` + `MdcThreadLocalAccessor` (micrometer-context-propagation). TODO documented in `GraphQlCorrelationInterceptor`.
- Prometheus endpoint should NOT be exposed on the public internet without authentication. In production, `management.server.port=9090` isolates it on an internal port — or place behind a proxy with auth.

## Spec-Driven Development (SDD) Migration

Performed in May 2026. Adopted OpenAPI-first workflow for the REST layer: the spec at `src/main/resources/openapi/users.yaml` is the contract source-of-truth. Server interfaces and DTOs are generated at build time; contract tests fail the build when the implementation diverges from the spec.

Changes:
- **`pom.xml`**: Added `openapi-generator-maven-plugin` 7.10.0 bound to `generate-sources`. Configs: `interfaceOnly=true`, `useSpringBoot3=true`, `useJakartaEe=true`, `useTags=true`, `skipDefaultInterface=true`, `delegatePattern=false`, `openApiNullable=false`, `useBeanValidation=true`, `documentationProvider=none`, `annotationLibrary=none`. Output: `target/generated-sources/openapi`. Added `io.swagger.core.v3:swagger-annotations` (runtime/compile). Added `com.atlassian.oai:swagger-request-validator-mockmvc:2.40.0` (test scope). Added `<excludedClasses>com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.*</excludedClasses>` to the PIT plugin so generated code is not mutated.
- **`src/main/resources/openapi/users.yaml`**: OpenAPI 3.0.3 spec describing the 5 endpoints (`GET /rest/user`, `GET /rest/user/{id}`, `POST /rest/user`, `PUT /rest/user/{id}`, `DELETE /rest/user/{id}`). Schemas: `UserResponse`, `SaveUserRequest`, `UpdateUserRequest`, `PagedUserResponse` (matches Spring `PageImpl` JSON shape to avoid breaking change), `ErrorResponse` (flexible: supports `{error: "..."}` and `{errors: [{field, message}]}` shapes used by `GlobalExceptionHandler`). Validation: `minLength: 5`, `maxLength: 100`, regex `^[^<>]*$` on write DTOs (approximates `@SafeHtml`); `format: uuid` for ids.
- **Generated code** (in `target/generated-sources/openapi`): interface `com.sergiovitorino.hexagonalarchitectureexample.ui.rest.generated.api.UsersApi` and DTOs `SaveUserRequest`, `UpdateUserRequest`, `UserResponse`, `PagedUserResponse`, `ErrorResponse`, `ErrorResponseErrorsInner` in `...ui.rest.generated.dto`.
- **`UserRestController` refactored**: now `implements UsersApi`. Methods `@Override` the generated interface signatures. DTO mapping `User` (domain) → `generated.UserResponse` and `Page<User>` → `generated.PagedUserResponse` is done in two private static helpers. Bean Validation runs on generated DTOs via `@Validated` on `UsersApi`. `@SafeHtml` remains on `SaveCommand`/`UpdateCommand` as defense-in-depth (server-side, decoupled from the wire contract).
- **`application/dto/UpdateUserRequest.java` removed**: replaced by the generated `ui.rest.generated.dto.UpdateUserRequest`. `application/dto/UserResponse.java` is kept because the GraphQL controller still uses it (GraphQL is out of SDD scope).
- **`UserRestContractTest`** (`src/test/java/.../ui/rest/test/`, 9 tests): `@WebMvcTest(UserRestController.class)` + `OpenApiValidationMatchers.openApi().isValid("openapi/users.yaml")`. Validates every response (200/201/204/400/404) against the spec for all 5 endpoints.

Architecture notes:
- **Hexagonal boundary preserved**: the controller bridges generated DTOs (wire format) and application Commands (`SaveCommand`, `UpdateCommand`, `DeleteCommand`, `ListCommand`). The application/domain layers have zero dependency on generated code.
- **Defense-in-depth validation**: same constraints expressed twice — once in the spec (`pattern`, `minLength`, `maxLength`) and once in the Commands (`@SafeHtml`, `@Size`, `@NotBlank`). The Bean Validation on generated DTOs catches violations first; Commands re-validate at the application boundary so non-REST callers (GraphQL) are also protected.
- **`UserResponse.name` has no `minLength`** in the spec — only `maxLength: 100`. Rationale: the contract for stored data must not propagate the write-side constraint `minLength: 5` because that would make the spec lie about data already persisted under different rules. `SaveUserRequest`/`UpdateUserRequest` enforce `minLength: 5` on write.
- **PIT excludes** `ui.rest.generated.*` so mutation score is not diluted by code we don't own.

How to regenerate sources:
- `mvn generate-sources` (runs automatically before `compile`). To force a regeneration after editing `users.yaml`, run `mvn clean compile`.

Versioning policy for `users.yaml`:
- `info.version` follows SemVer. **Major bump** for breaking changes (field removed or renamed in response, response shape changed, endpoint removed, status code changed). **Minor bump** for non-breaking additions (new optional field, new endpoint). **Patch bump** for editorial corrections (description text, examples). When bumping major, also update the server URL if the API has a version path prefix.

Test count: **171 tests** total (was 161 before SDD; +9 contract tests original, +1 `post_400_withHtmlPayload_matchesSpec`). All green. JaCoCo line coverage threshold (80%) passes.

Accepted technical debt:
- **`ErrorResponse` com `oneOf`**: o spec define `ErrorResponse` como `oneOf: [SingleError, ValidationErrors]`. O código gerado representa `ErrorResponse` como um objeto genérico (sem discriminador). A validação efetiva do shape é feita pelo contract test via jsonPath, não pelo tipo gerado.
- **`pattern '^[^<>]*$'` em `SaveUserRequest`/`UpdateUserRequest` é uma aproximação fraca de `@SafeHtml`**: o pattern impede `<` e `>` literais mas não bloqueia entidades HTML codificadas (ex: `&lt;script&gt;`). A validação forte permanece via `@SafeHtml` no Command de aplicação (Jsoup + `Parser.unescapeEntities`), que decodifica entidades antes de verificar. O pattern no spec serve apenas como defense-in-depth na camada do cliente/spec; a proteção real é server-side.
- **Contract test 400 com HTML usa mock em vez de payload real**: o teste `post_400_withHtmlPayload_matchesSpec` simula rejeição via `DomainValidationException` mockada em vez de enviar `<html>` real ao handler. Motivo: o `swagger-request-validator` rejeita o request antes de observar a response quando `pattern` é violado. Detalhes em ADR-0007.

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
- Spring Boot Actuator exposes `health`, `info`, and `prometheus` endpoints. Adding new endpoints requires updating `management.endpoints.web.exposure.include` in `application.properties`. In production, management runs on port 9090 (not 8080) to isolate metrics from public traffic.
- `SafeHtml` and `SafeHtmlValidator` are in `application/validation/` (not in `infrastructure/`) so that `SaveCommand` (application layer) can use them without violating the hexagonal dependency direction (application must not depend on infrastructure).
- **PUT `/rest/user/{id}` uses `UpdateUserRequest` (name-only) instead of `SaveCommand`**: keeps the REST request body explicit about what is mutable on update and avoids overloading the create command. The REST controller composes `UpdateCommand(pathId, body.name())` before delegating to `UserCommandHandler`. Validation on `UpdateCommand` (`@NotNull id`, `@NotBlank @SafeHtml @Size(min=5,max=100) name`) still runs at the handler boundary via `@Validated` + `@Valid`. Accepted DRY trade-off: a small body record duplicates one field but decouples the API contract from the command shape.
- **`UserService.delete` uses `UserRepositoryPort.existsById(UUID)`** (not `findById`) to verify existence before issuing the `deleteById` call -- avoids loading and mapping the entity. Tests assert call order via `inOrder(existsById, deleteById)`.
- **Validation reach**: `@Valid` is wired on `UserCommandHandler.handle(SaveCommand)` and `handle(UpdateCommand)`; combined with `@Validated` at class level, this enforces `SafeHtml`, `@Size`, `@NotBlank`, `@NotNull` for both REST and GraphQL entry points. Bean validation failures surface as `ConstraintViolationException` (handled in `GlobalExceptionHandler` -> 400 and in `GraphQlExceptionResolver` -> `BAD_REQUEST`).
- **Dois tipos `UserResponse` em pacotes distintos, sem conflito**: `application.dto.UserResponse` (record Java mantido para o controller GraphQL) e `ui.rest.generated.dto.UserResponse` (gerado pelo `openapi-generator` para o controller REST). São tipos completamente distintos em pacotes diferentes; não há ambiguidade de compilação porque cada controller importa o seu. GraphQL não usa o tipo gerado (GraphQL está fora do escopo SDD). Ao adicionar um campo ao domínio `User`, é necessário atualizar `users.yaml` (REST), `application.dto.UserResponse` (GraphQL) e `UserEntity` (persistência) separadamente.
- **GraphQL error mapping is centralized in `infrastructure/graphql/GraphQlExceptionResolver`** (not in `GlobalExceptionHandler`, which is REST-only via `@RestControllerAdvice`). Adding a new domain exception requires updating both resolvers if it must be visible on both transports.
- **Rate limit cache size is configurable** via `ratelimit.cache-maximum-size` (`RateLimitProperties.cacheMaximumSize`, default 100000). `bucket4j-core` is pinned at `8.10.1` -- newer versions referenced in earlier drafts (e.g. 8.14.0) are not published to Maven Central.

## Git Conventions

- Main branch: `master`
- Git user: Sergio Vitorino <sergiovlvitorino@gmail.com>
- Commit messages: descriptive, in English
- Co-authored commits with AI agents include the `Co-Authored-By` trailer
