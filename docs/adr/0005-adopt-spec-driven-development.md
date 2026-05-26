# 0005 - Adoção de Spec-Driven Development (SDD)

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

O projeto vem sendo evoluído com forte uso de agentes de IA (programação agêntica). Sem um contrato externo verificável, agentes (e humanos) tendem a inferir comportamento a partir de implementação, gerando drift de contrato, alucinações em refactors e ciclos longos de revisão. Precisamos de uma fonte de verdade que seja independente da implementação e checada no build.

## Decision Drivers

- Reduzir alucinação de agentes em refactors: especificação como ground truth.
- Detectar divergência contrato↔implementação no CI (build break).
- Versionar contrato como artefato de primeira classe.
- Diminuir loops de code review focando-os no contrato.
- Acelerar geração de stubs, DTOs e testes de contrato.

## Considered Options

1. Adotar Spec-Driven Development: REST (OpenAPI), GraphQL (schema.graphqls), Banco (migrações Flyway), todos como specs versionadas.
2. Manter abordagem code-first com documentação gerada (`springdoc` introspectando controllers).
3. Adotar SDD apenas para REST.

## Decision Outcome

Escolhido: **SDD para todas as superfícies de contrato**:
- GraphQL: `schema.graphqls` (já vigente, ADR-0002).
- REST: OpenAPI YAML manual em `src/main/resources/openapi/users.yaml` (detalhado em ADR-0006).
- Banco: migrações Flyway versionadas (ADR-0003).
- Validação de conformidade no build via testes de contrato (ADR-0007).

## Pros and Cons of the Options

### SDD completo
- Bom: ground truth externo, agentes consultam spec em vez de adivinhar.
- Bom: divergência quebra o build, não chega a produção.
- Bom: contrato revisável de forma independente da implementação.
- Ruim: boilerplate inicial (geração de DTOs, plugins Maven).
- Ruim: curva de aprendizado de OpenAPI 3 para quem só conhece springdoc.

### Code-first (springdoc por introspecção)
- Bom: zero esforço de manutenção da spec.
- Ruim: implementação é a verdade; spec apenas reflete bugs.
- Ruim: agentes não têm contrato estável para se basear.

### SDD apenas REST
- Bom: ganho parcial com menos esforço.
- Ruim: inconsistência metodológica entre superfícies.

## Consequences

- Trade-off aceito: aceitamos overhead de geração em troca de robustez no fluxo agêntico.
- Toda alteração de comportamento de contrato exige atualização da spec primeiro.
- ADRs subsequentes (0006, 0007) detalham implementação técnica do SDD para REST.
- Projeto educacional ganha valor didático adicional ao demonstrar SDD na prática.
