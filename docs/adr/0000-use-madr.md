# 0000 - Adoção do template MADR para ADRs

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

Decisões arquiteturais relevantes vinham sendo registradas exclusivamente em `CLAUDE.md` e em mensagens de commit. Esse formato dificulta rastreabilidade histórica, comparação entre alternativas consideradas e leitura por novos colaboradores (humanos ou agentes de IA). Precisamos de um formato leve, versionável e padronizado para ADRs.

## Decision Drivers

- Rastreabilidade e versionamento das decisões junto ao código.
- Formato leve, em Markdown, sem dependências externas.
- Estrutura previsível, facilitando consumo por agentes de IA (programação agêntica).
- Adoção amplamente reconhecida pela comunidade.

## Considered Options

1. MADR (Markdown Any Decision Records).
2. Nygard ADR original (formato mais enxuto, sem seções de drivers/opções).
3. Manter apenas `CLAUDE.md` + commits.

## Decision Outcome

Escolhido: **MADR**. Estrutura padrão: Status, Context, Decision Drivers, Considered Options, Decision Outcome, Pros and Cons, Consequences. Arquivos numerados sequencialmente em `docs/adr/NNNN-titulo-kebab-case.md`.

## Pros and Cons of the Options

### MADR
- Bom: seções explícitas para alternativas e trade-offs.
- Bom: amplamente adotado, fácil onboarding.
- Ruim: levemente mais verboso que Nygard.

### Nygard original
- Bom: extremamente conciso.
- Ruim: não documenta alternativas descartadas — perdemos contexto.

### Apenas CLAUDE.md + commits
- Bom: zero overhead.
- Ruim: não escala, sem histórico estruturado, ilegível para agentes.

## Consequences

- Toda decisão arquitetural relevante passa a exigir um ADR.
- `CLAUDE.md` permanece como índice/contexto operacional, não substitui ADRs.
- ADRs são imutáveis após `Accepted`; mudanças geram novo ADR com status `Supersedes #N`.
