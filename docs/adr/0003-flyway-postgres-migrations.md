# 0003 - Flyway + PostgreSQL para migrações de schema

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

Em dev, H2 com `ddl-auto=update` resolve. Em produção, precisamos de versionamento de schema confiável, auditável e reproduzível, sem permitir que o Hibernate altere DDL automaticamente.

## Decision Drivers

- Schema versionado junto ao código.
- Idempotência e replay em qualquer ambiente.
- Bloqueio explícito a operações destrutivas em prod.
- Validação da entidade JPA contra o schema real.

## Considered Options

1. Flyway com `ddl-auto=validate` em prod.
2. Liquibase.
3. `ddl-auto=update` em prod (Hibernate gerenciando DDL).

## Decision Outcome

Escolhido: **Flyway**. Configuração em `application-prod.properties`:
- `spring.flyway.enabled=true`
- `spring.jpa.hibernate.ddl-auto=validate` (Hibernate só verifica, nunca altera).
- `spring.flyway.validate-on-migrate=true`
- `spring.flyway.clean-disabled=true` (impede `flyway clean` acidental).
- `baseline-on-migrate` NÃO setado (default `false`) para não mascarar divergências.

Migração inicial: `src/main/resources/db/migration/V1__init.sql` cria `users` (UUID PK, `name VARCHAR(100) NOT NULL`, `CHECK length(name) >= 3`) e `idx_user_name`.

Em dev (H2): `spring.flyway.enabled=false`, `ddl-auto=update`.

Testes de integração: Testcontainers (`postgres:16-alpine`) + profile `it` validam Flyway real.

## Pros and Cons of the Options

### Flyway
- Bom: SQL puro, fácil revisão.
- Bom: integração nativa com Spring Boot.
- Ruim: refactors de schema exigem novas migrações (V2, V3...).

### Liquibase
- Bom: XML/YAML/JSON com rollback declarativo.
- Ruim: mais verboso, curva maior.

### `ddl-auto=update` em prod
- Bom: zero esforço.
- Ruim: imprevisível, perigoso, sem auditoria.

## Consequences

- Toda alteração de schema vai como `V{N}__descricao.sql`.
- Entidade JPA divergente do schema quebra a aplicação no startup (intencional).
- `flyway clean` desabilitado: dropar schema requer ação manual no banco.
