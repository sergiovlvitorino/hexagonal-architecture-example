# hexagonal-architecture-example
This example shows how to implement the hexagonal architecture design pattern with REST and GraphQL.

![Java CI with Maven](https://github.com/sergiovlvitorino/hexagonal-architecture-example/workflows/Java%20CI%20with%20Maven/badge.svg)

[![codecov](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example)

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 LTS | Language |
| Spring Boot | 3.5.9 | Framework |
| Spring for GraphQL | (managed) | Schema-first GraphQL |
| Spring Data JPA + H2 | (managed) | Persistence (in-memory) |
| Lombok | 1.18.42 | Boilerplate reduction |
| Jsoup | 1.22.1 | XSS prevention |
| JaCoCo | 0.8.14 | Code coverage |

## Architecture

This project follows the **Hexagonal Architecture** (Ports & Adapters) pattern:

```
src/main/java/
  domain/
    model/          User entity (JPA, UUID primary key)
    repository/     UserRepository (JPA port)
  application/
    command/        UserCommandHandler — orchestrates use cases
                    ListCommand, SaveCommand — Java Records
    service/        UserService — business logic (paginate, sort, filter, save)
  ui/
    rest/           UserRestController — GET/POST /rest/user
    graphql/        UserGraphQLController — @QueryMapping findAll
  infrastructure/
    validations/    @SafeHtml annotation + SafeHtmlValidator (Jsoup)
    GlobalExceptionHandler — @RestControllerAdvice
    WebConfig       — CORS (origins configurable via application.properties)
    Initialize      — seed data (@Profile("!prod"))
```

## Getting Started

### Prerequisites
* JDK 21
* Maven 3.8+

### Running
```bash
git clone https://github.com/sergiovlvitorino/hexagonal-architecture-example
cd hexagonal-architecture-example
mvn spring-boot:run
```

### Running tests
```bash
mvn test
```

### Test coverage report
```bash
mvn test jacoco:report
```
The report is generated at `target/site/jacoco/index.html`.

**Current coverage: 98% instructions, 85% branches — 31 tests.**

## API Endpoints

### REST

| Method | Endpoint | Description |
|---|---|---|
| GET | `/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true` | List users (paginated, sorted) |
| GET | `/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true&user.name=filter` | List users with name filter |
| POST | `/rest/user` | Create user — body: `{"name": "User Name"}` |

**Validation rules (POST):**
- `name` is required, between 5 and 100 characters
- HTML tags are rejected (`@SafeHtml` with Jsoup `Safelist.none()`)

### GraphQL

* `POST /graphql` — GraphQL endpoint
* `GET /graphiql` — GraphiQL interactive interface

#### Schema

```graphql
type Query {
  findAll(
    pageNumber: Int!   # zero-based page index (min: 0)
    pageSize:   Int!   # items per page (range: 1–1000)
    orderBy:    String! # allowed: "id" or "name"
    asc:        Boolean! # true = ascending
    userName:   String  # optional exact-match filter
  ): UserPage!
}
```

#### Example query

```graphql
{
  findAll(pageNumber: 0, pageSize: 10, orderBy: "name", asc: true) {
    content { id name }
    totalElements
    totalPages
  }
}
```

## Security

| Measure | Implementation |
|---|---|
| XSS prevention | Custom `@SafeHtml` validator — Jsoup `Safelist.none()` + text equality check |
| Input validation (REST) | Bean Validation (`@NotEmpty`, `@Size`, `@SafeHtml`) |
| Input validation (GraphQL) | Manual: `pageSize` ≤ 1000, `orderBy` whitelist `{id, name}` |
| Global exception handler | `@RestControllerAdvice` — structured JSON errors, no stack traces exposed |
| CORS | Configurable via `app.cors.allowed-origins` in `application.properties` |
| H2 console | Disabled — `spring.h2.console.enabled=false` |
| GraphQL introspection | Disabled — `spring.graphql.schema.introspection.enabled=false` |
| Seed data | Active only in non-prod profiles — `@Profile("!prod")` |
| Dependency injection | Constructor injection throughout — no `@Autowired` field injection |

## Configuration

Key properties in `src/main/resources/application.properties`:

```properties
# CORS: override per environment (comma-separated)
app.cors.allowed-origins=http://localhost:8080

# Enable virtual threads (Java 21+)
spring.threads.virtual.enabled=true

# Enable GraphQL interactive UI
spring.graphql.graphiql.enabled=true
```

## Authors

* **Sergio Vitorino** — (https://github.com/sergiovlvitorino)

See also the list of [contributors](https://github.com/sergiovlvitorino/hexagonal-architecture-example/contributors) who participated in this project.

## License

This project is licensed under the GPL-3.0 License — see the [LICENSE.md](LICENSE.md) file for details
