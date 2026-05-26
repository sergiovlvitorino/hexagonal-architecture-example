# Hexagonal Architecture Example

Projeto educacional em Java/Spring Boot que demonstra a aplicacao pratica do padrao **Arquitetura Hexagonal (Ports & Adapters)** expondo um mesmo dominio via **REST** e **GraphQL**, com persistencia em PostgreSQL (prod) ou H2 (dev), observabilidade com Prometheus e seguranca enrijecida.

![Java CI with Maven](https://github.com/sergiovlvitorino/hexagonal-architecture-example/workflows/Java%20CI%20with%20Maven/badge.svg)
[![codecov](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/hexagonal-architecture-example)

## Visao Geral

O objetivo deste projeto e servir como **referencia didatica** para desenvolvedores que querem entender, na pratica, como:

- Isolar o dominio de detalhes de infraestrutura (JPA, HTTP, GraphQL).
- Expor o mesmo caso de uso em multiplas interfaces (REST e GraphQL) reaproveitando a camada de aplicacao.
- Aplicar boas praticas de seguranca, observabilidade, validacao e testes em um servico Spring Boot moderno.

Nao e um produto comercial. E um laboratorio de boas praticas com cobertura de testes acima de 98%, mutation score 97% e CI integrado.

## Funcionalidades

- **CRUD completo de User** via REST e GraphQL (`findAll`, `findById`, `create`, `update`, `delete`).
- **Paginacao e ordenacao** com whitelist de campos (`id`, `name`) protegendo contra injection em `orderBy`.
- **Filtro por nome** via `Example`/`ExampleMatcher` (case-insensitive, contains).
- **Validacao Bean Validation** nos comandos (`@NotBlank`, `@Size`, `@SafeHtml` custom para XSS).
- **OpenAPI / Swagger UI** documentando os endpoints REST.
- **Observabilidade**: metricas Prometheus (`users_created_total`, `users_updated_total`, `users_deleted_total`), logs JSON em prod, correlation-id propagado via header `X-Correlation-Id`.
- **Persistencia profissional**: PostgreSQL + Flyway em producao, H2 em desenvolvimento.

## Stack

- **Java 21** (LTS, com Records e virtual threads habilitadas)
- **Spring Boot 3.5.9** (Web MVC, Data JPA, Validation, Actuator)
- **Spring for GraphQL** (schema-first)
- **PostgreSQL 16** (producao) / **H2** (desenvolvimento)
- **Flyway** (migracoes de schema)
- **Springdoc OpenAPI** (Swagger UI)
- **Micrometer + Prometheus** (metricas)
- **Logstash Logback Encoder** (logs estruturados JSON)
- **Lombok**, **Jsoup** (XSS), **JaCoCo** (cobertura), **PIT** (mutation testing)
- **Testcontainers** (testes de integracao com PostgreSQL real)

## Como Rodar

### Pre-requisitos

- JDK 21
- Maven 3.8+
- Docker (apenas para testes de integracao com PostgreSQL)

### Desenvolvimento (perfil default, H2 in-memory)

```bash
git clone https://github.com/sergiovlvitorino/hexagonal-architecture-example
cd hexagonal-architecture-example
mvn spring-boot:run
```

A aplicacao sobe em `http://localhost:8080` com 6 usuarios de seed (componente `Initialize`, ativo apenas fora do perfil `prod`).

### Producao (perfil `prod`, PostgreSQL + Flyway)

Defina as variaveis de ambiente e ative o perfil:

```bash
export DB_URL=jdbc:postgresql://host:5432/dbname
export DB_USER=postgres
export DB_PASSWORD=secret
export SPRING_PROFILES_ACTIVE=prod
mvn spring-boot:run
```

Em producao:
- Flyway aplica `V1__init.sql` automaticamente.
- `ddl-auto=validate` garante que o schema bata com as entidades.
- Endpoint Prometheus expoe na porta interna `9090` (nao publica em 8080).
- Logs em formato JSON.
- GraphiQL e introspection GraphQL desativados.

### Testes

```bash
mvn test                 # 161 testes unitarios + slice
mvn test jacoco:report   # relatorio em target/site/jacoco/index.html
mvn verify -Pit          # testes de integracao com Testcontainers (requer Docker)
mvn org.pitest:pitest-maven:mutationCoverage  # mutation testing
```

## Endpoints Principais

### REST

| Metodo | Endpoint                | Descricao                                |
|--------|-------------------------|------------------------------------------|
| GET    | `/rest/user`            | Lista paginada (params: `pageNumber`, `pageSize`, `orderBy`, `asc`, `user.name`) |
| GET    | `/rest/user/{id}`       | Busca por ID (404 se nao existir)        |
| POST   | `/rest/user`            | Cria usuario (body: `{"name":"..."}`)    |
| PUT    | `/rest/user/{id}`       | Atualiza nome                            |
| DELETE | `/rest/user/{id}`       | Remove (204 No Content)                  |

### GraphQL

- `POST /graphql` -- endpoint unico para queries e mutations.
- `GET /graphiql` -- interface interativa (apenas dev).

Queries: `findAll`, `findById`. Mutations: `saveUser`, `updateUser`, `deleteUser`.

### Documentacao e Operacao

- `GET /swagger-ui.html` -- UI interativa OpenAPI.
- `GET /v3/api-docs` -- especificacao OpenAPI em JSON.
- `GET /actuator/health` -- health check.
- `GET /actuator/prometheus` -- metricas (porta 9090 em prod).

## Arquitetura

O projeto segue **Ports & Adapters**:

- **Domain** (`domain/`): POJOs puros (`User`), excecoes de negocio e a porta de saida `UserRepositoryPort`. Zero dependencia de Spring, JPA ou HTTP.
- **Application** (`application/`): casos de uso (`UserService`), comandos (`SaveCommand`, `UpdateCommand`...), DTOs e validacao customizada.
- **UI / Adapters de entrada** (`ui/`): `UserRestController` e `UserGraphQLController` traduzem HTTP/GraphQL em chamadas de aplicacao.
- **Infrastructure / Adapters de saida** (`infrastructure/`): `UserRepositoryAdapter` (JPA), `UserEntity`, configuracao, observabilidade, filtros web e seed.

Detalhes completos (camadas, decisoes de design, historico de refatoracoes) em [`CLAUDE.md`](CLAUDE.md).

## User Stories Entregues

| ID    | Entrega                                                                                  |
|-------|------------------------------------------------------------------------------------------|
| US-01 | Listagem paginada de usuarios via REST e GraphQL com filtro por nome e ordenacao         |
| US-02 | Criacao de usuario com validacao Bean Validation e protecao XSS via `@SafeHtml`          |
| US-03 | Busca por ID com `UserNotFoundException` mapeada para 404                                |
| US-04 | Atualizacao e remocao de usuario (REST + GraphQL) com OpenAPI/Swagger documentando tudo  |
| US-05 | Hardening de seguranca: headers HTTP, rate limit, GraphQL depth limit, perfil `prod`     |
| US-06 | Persistencia em PostgreSQL com Flyway + testes de integracao Testcontainers              |
| US-07 | Observabilidade: metricas Prometheus, logs JSON, correlation-id em REST e GraphQL        |

## Qualidade

- **161 testes** (unitarios, slice `@WebMvcTest`/`@GraphQlTest`, integracao `@SpringBootTest` + Testcontainers).
- **Cobertura JaCoCo**: acima de 98% instrucoes e 85% branches (gate de 80% no build).
- **Mutation score (PIT)**: 97%.
- **CI**: GitHub Actions com build, testes, cobertura, mutation testing e OWASP Dependency Check.

## Licenca

GPL-3.0 -- veja [LICENSE.md](LICENSE.md).

## Autor

**Sergio Vitorino** -- [github.com/sergiovlvitorino](https://github.com/sergiovlvitorino)
