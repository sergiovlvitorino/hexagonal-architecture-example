# 0007 - Testes de contrato com Swagger Request Validator

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

Ter o YAML como spec (ADR-0006) é necessário, mas não suficiente: nada impede o controller de retornar payload divergente do contrato. Precisamos de uma trava executável no CI que falhe quando response real diverge de `users.yaml`.

## Decision Drivers

- Build break automático em divergência contrato↔implementação.
- Cobertura nos slice tests já existentes (`@WebMvcTest`), sem subir aplicação completa.
- Baixo overhead (sem brokers como Pact).
- Suporte a OpenAPI 3.

## Considered Options

1. `atlassian/swagger-request-validator-mockmvc` (2.40+) plugado nos `@WebMvcTest`.
2. Pact (consumer-driven contracts).
3. REST Assured + asserts manuais por endpoint.

## Decision Outcome

Escolhido: **swagger-request-validator-mockmvc 2.40+**.

Integração:
- Dependência `com.atlassian.oai:swagger-request-validator-mockmvc` em escopo `test`.
- Em cada `@WebMvcTest` de `UserRestController`, usar `OpenApiValidationMatchers.openApi().isValid("openapi/users.yaml")` como `ResultMatcher`.
- `MockMvc` valida automaticamente request e response contra o YAML; qualquer divergência (campo extra, tipo errado, status não declarado) falha o teste.
- Levels: `ERROR` para violações de schema; `IGNORE` controlado para itens em evolução, se necessário (preferencialmente vazio).

Cobertura mínima exigida:
- Todos os endpoints REST declarados no YAML devem ter ao menos 1 teste contract-validated.
- Cenários de erro (400, 404) validados contra os schemas de erro do YAML.

## Pros and Cons of the Options

### swagger-request-validator-mockmvc
- Bom: roda como parte dos slice tests existentes, zero infra extra.
- Bom: mensagens claras de divergência.
- Ruim: valida estrutura, não comportamento semântico (esperado).

### Pact
- Bom: contratos negociados entre consumer/provider.
- Ruim: overhead de broker; o projeto não tem consumidores externos conhecidos.

### REST Assured manual
- Bom: flexível.
- Ruim: validação espelhada à mão = drift garantido.

## Consequences

- Adicionar campo a uma response sem atualizar `users.yaml` quebra o build.
- Mudanças no YAML que não sejam refletidas pelos controllers também quebram (proteção bidirecional).
- Tempo de execução de testes praticamente inalterado (validação roda em memória).
- Junto com ADR-0006, fecha o ciclo SDD para REST: spec autoral + geração + verificação.

## Trade-off aceito: contract test 400 com payload HTML

O teste `post_400_withHtmlPayload_matchesSpec` simula rejeição de HTML pelo server usando um mock que lança `DomainValidationException` — não um payload HTML real chegando ao handler real.

Motivo: `pattern '^[^<>]*$'` em `SaveUserRequest` no YAML rejeita `<html>` já no validator de request do `swagger-request-validator`, impedindo que `openApi().isValid(SPEC)` seja usado diretamente com esse payload (o validator reportaria violação de request além da de response). Para isolar a validação da response 400, o teste usa um payload spec-inválido + mock que lança exceção.

Consequência: o teste cobre o shape da response 400 (`SingleError`) mas não o caminho de validação `@SafeHtml` end-to-end. Este caminho é coberto por `UserRestControllerTest` (que usa MockMvc sem validação de spec) e `SafeHtmlValidatorTest` (unitário). A combinação das três classes garante cobertura completa sem ambiguidade.
