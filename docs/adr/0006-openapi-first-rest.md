# 0006 - REST Contract-First com OpenAPI

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

A camada REST hoje é code-first: `springdoc` introspecciona controllers e gera `/v3/api-docs`. Isso significa que bugs no controller viram "contrato". Em coerência com a adoção de SDD (ADR-0005), o contrato REST precisa ser autoria humana, versionado, e fonte da implementação — não consequência dela.

## Decision Drivers

- Coerência com SDD: spec antes do código.
- DTOs gerados automaticamente, eliminando drift manual.
- Manter validações customizadas (`@SafeHtml`) que não têm equivalente OpenAPI.
- Não quebrar a separação hexagonal: Commands de aplicação permanecem como estão.
- Mutation testing (PIT) não pode penalizar código gerado.

## Considered Options

1. OpenAPI YAML manual + `openapi-generator-maven-plugin` (generator `spring`, `interfaceOnly=true`).
2. Manter springdoc code-first.
3. Gerar a partir de anotações + exportar (híbrido).

## Decision Outcome

Escolhido: **OpenAPI YAML manual + geração de interfaces e DTOs**.

Configuração:
- Spec: `src/main/resources/openapi/users.yaml`.
- Plugin: `openapi-generator-maven-plugin`, generator `spring`, com:
  - `interfaceOnly=true` (gera apenas APIs como interfaces; controllers implementam).
  - `useSpringBoot3=true`.
  - `useJakartaEe=true`.
  - `useTags=true`.
- Pacote de saída: `...ui.rest.generated.api` (interfaces) e `...ui.rest.generated.dto` (DTOs).

Regras de fronteira:
- DTOs gerados (`UserDto`, `CreateUserRequestDto`, etc.) vivem apenas em `ui.rest.generated.dto`.
- **NÃO substituem** `SaveCommand` e `UpdateCommand` (application/command/user) — controllers mapeiam `DTO -> Command` antes de invocar `UserCommandHandler`.
- `@SafeHtml` permanece no Command (custom validator não é representável em OpenAPI; apenas `pattern` aproximaria, sem equivalência semântica).

CI:
- PIT exclui `**/generated/**` para não inflar/falsear o score de mutação.
- JaCoCo também ignora pacote gerado.

## Pros and Cons of the Options

### OpenAPI YAML manual + generator
- Bom: contrato autoral, revisável em PR independentemente do código.
- Bom: stubs/DTOs sempre alinhados.
- Ruim: dois modelos (DTO gerado + Command de aplicação) — mapeamento explícito.

### springdoc code-first
- Bom: zero spec para manter.
- Ruim: incompatível com SDD.

### Híbrido
- Bom: aproveita anotações existentes.
- Ruim: ambiguidade sobre quem é a fonte da verdade.

## Consequences

- Adicionar endpoint REST = editar YAML, rodar build, implementar interface gerada no controller.
- Mudança no Command de aplicação não vaza para o contrato sem alteração explícita do YAML.
- Validações customizadas continuam vivas na camada de aplicação, garantidas pelo handler.
- Tamanho do JAR cresce marginalmente com classes geradas (aceitável).

## Coexistência com springdoc-openapi

`springdoc-openapi-starter-webmvc-ui` permanece no `pom.xml` exclusivamente para servir o shell do Swagger UI em `/swagger-ui.html`. A geração de `api-docs` a partir de anotações está **desativada** (`springdoc.api-docs.enabled=false`). O Swagger UI é configurado via `springdoc.swagger-ui.url=/openapi/users.yaml` para carregar diretamente o arquivo estático `users.yaml` servido por `WebConfig.addResourceHandlers`. Dessa forma, o springdoc não é fonte da verdade — é apenas um servidor de UI.

## Política de versionamento da spec

`info.version` em `users.yaml` segue SemVer:
- **Major**: breaking change (campo removido/renomeado em response, shape alterada, endpoint removido, status code mudado).
- **Minor**: adição não-breaking (novo campo opcional, novo endpoint).
- **Patch**: correção editorial (descrição, exemplos, comentários).
