# 0002 - GraphQL Schema-First

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

A versão original usava `graphql-spqr` (code-first), gerando schema a partir de anotações Java. Isso acoplava o schema a detalhes de implementação e tornava difícil revisar o contrato sem ler código. Com a migração para Spring Boot 3 / Spring for GraphQL, precisamos decidir entre manter code-first ou adotar schema-first.

## Decision Drivers

- Contrato GraphQL como artefato revisável e versionável de forma independente.
- Suporte oficial e mantido pela equipe Spring.
- Validação estática do schema em tempo de build.
- Alinhamento com a prática Spec-Driven Development (ver ADR-0005).

## Considered Options

1. Schema-first com Spring for GraphQL e `schema.graphqls`.
2. Code-first via SPQR.
3. Code-first via Netflix DGS.

## Decision Outcome

Escolhido: **Schema-first**. O arquivo `src/main/resources/graphql/schema.graphqls` é a fonte da verdade. Controllers (`UserGraphQLController`) usam `@QueryMapping` / `@MutationMapping` / `@Argument` apenas para ligar resolvers ao schema. Nullability é declarada no schema (`User!`, `Int!`), não em Java.

## Pros and Cons of the Options

### Schema-first (Spring for GraphQL)
- Bom: contrato legível, revisável em PR.
- Bom: introspecção pode ser desligada em prod (`spring.graphql.schema.introspection.enabled=false`).
- Ruim: duplicação leve entre tipos do schema e classes Java.

### SPQR (code-first)
- Bom: zero duplicação.
- Ruim: projeto SPQR estagnado, schema implícito.

### Netflix DGS
- Bom: maduro, code-gen.
- Ruim: dependência externa adicional, fora do guarda-chuva Spring oficial.

## Consequences

- Mudanças no contrato exigem editar `schema.graphqls` primeiro, depois resolver.
- Validação de `pageNumber`, `pageSize`, `orderBy`, `asc` cobre apenas valores válidos pelo schema; checagens semânticas ficam no `UserCommandHandler`.
- `UserResponse` (DTO) é exposto, não a entidade.
