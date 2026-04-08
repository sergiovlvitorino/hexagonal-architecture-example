# hexagonal-architecture-example
This example shows how to implement the hexagonal architecture design pattern with REST and GraphQL.

![Java CI with Maven](https://github.com/sergiovlvitorino/hexagonal-architecture-example/workflows/Java%20CI%20with%20Maven/badge.svg)

[![codecov](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example)

## Tech Stack

* Java 21 (LTS)
* Spring Boot 3.5.9
* Spring for GraphQL (schema-first)
* Spring Data JPA + H2 (in-memory)
* Lombok 1.18.42
* Jsoup 1.22.1 (XSS prevention)
* JaCoCo 0.8.14 (code coverage)

## Architecture

```
src/main/java/
  domain/
    model/          User entity
    repository/     UserRepository (JPA)
  application/
    command/        UserCommandHandler, ListCommand, SaveCommand (Records)
    service/        UserService
  ui/
    rest/           UserRestController (GET/POST)
    graphql/        UserGraphQLController (@QueryMapping)
  infrastructure/
    validations/    @SafeHtml custom annotation + SafeHtmlValidator
    GlobalExceptionHandler, WebConfig, Initialize
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

## API Endpoints

### REST

* `GET /rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true` - List users (paginated, sorted)
* `GET /rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true&user.name=filter` - List users with name filter
* `POST /rest/user` - Create user (body: `{"name": "User Name"}`)

### GraphQL

* `POST /graphql` - GraphQL endpoint
* `GET /graphiql` - GraphiQL interactive interface

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

* **XSS prevention**: Custom `@SafeHtml` validator using Jsoup with `Safelist.none()`
* **Input validation**: Bean Validation on commands (`@Min`, `@Max`, `@NotBlank`, `@Size`), centralized `orderBy` whitelist in `UserCommandHandler`
* **Global exception handler**: `@RestControllerAdvice` with handlers for validation errors, illegal arguments, malformed requests, type mismatches, and generic exceptions
* **CORS**: Configurable origins via `cors.allowed-origins` property, restricted methods and headers
* **H2 console**: Disabled (`spring.h2.console.enabled=false`)
* **GraphQL introspection**: Disabled in production (`spring.graphql.schema.introspection.enabled=false`)
* **Seed data**: `Initialize` component only active in non-prod profiles (`@Profile("!prod")`)

## Authors

* **Sergio Vitorino** - (https://github.com/sergiovlvitorino)

See also the list of [contributors](https://github.com/sergiovlvitorino/hexagonal-architecture-example/contributors) who participated in this project.

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE.md](LICENSE.md) file for details
