# 0004 - Observabilidade: Prometheus, JSON logs e Correlation-Id

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

Sem métricas, logs estruturados e correlação entre requisições, diagnosticar incidentes em produção é inviável. Precisamos de instrumentação que respeite a fronteira hexagonal (sem vazar Micrometer para o domínio/aplicação).

## Decision Drivers

- Métricas de negócio (`users_created/updated/deleted`) consumíveis por Prometheus.
- Logs JSON em prod para ingestão por ELK/Loki.
- Rastreamento via `X-Correlation-Id` propagado por REST e GraphQL.
- Métricas internas isoladas (porta `9090`) para não expor publicamente.
- Aplicação não pode depender de Micrometer.

## Considered Options

1. Micrometer + Prometheus + Logback JSON + filter de correlation-id, integrados via eventos de aplicação.
2. Instrumentar diretamente `UserService` com Micrometer.
3. OpenTelemetry full (logs + metrics + traces).

## Decision Outcome

Escolhido: **Micrometer + Prometheus + logs JSON + correlation-id**, com decoupling via eventos:
- `UserService` publica `UserCreatedEvent` / `UserUpdatedEvent` / `UserDeletedEvent` via `ApplicationEventPublisher`.
- `UserMetricsEventListener` em `infrastructure/observability/` escuta com `@TransactionalEventListener(AFTER_COMMIT)` e incrementa counters.
- `logback-spring.xml`: console colorido em não-prod; `LoggingEventCompositeJsonEncoder` em prod, incluindo `mdc.correlationId`.
- `CorrelationIdFilter` (REST) e `GraphQlCorrelationInterceptor` (GraphQL) validam header `X-Correlation-Id` contra `^[a-zA-Z0-9\-]{1,64}$`, geram UUID quando ausente/inválido.
- Em prod: `management.server.port=9090`, expostos apenas `health`, `info`, `prometheus`.

## Pros and Cons of the Options

### Eventos + Listener
- Bom: domínio/aplicação não conhecem Micrometer.
- Bom: counters só incrementam após commit (sem inflação por rollback).
- Ruim: indireção adicional.

### Micrometer direto no Service
- Bom: simples.
- Ruim: quebra a regra hexagonal — aplicação dependendo de infra.

### OpenTelemetry full
- Bom: traces nativos.
- Ruim: escopo excessivo para projeto educacional.

## Consequences

- Novos eventos de domínio podem ser plugados a novas métricas sem tocar `UserService`.
- Endpoint Prometheus jamais deve ir para internet pública sem auth/proxy.
- Validação rígida do header previne log injection.
