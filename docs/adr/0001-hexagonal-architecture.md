# 0001 - Arquitetura Hexagonal (Ports & Adapters)

- Status: Accepted
- Date: 2026-05-26
- Deciders: Sergio Vitorino

## Context and Problem Statement

O projeto é educacional e precisa demonstrar separação clara entre regra de negócio e infraestrutura, permitindo trocar persistência ou camada de apresentação (REST/GraphQL) sem reescrever o domínio. Arquiteturas em camadas tradicionais tendem a acoplar a aplicação ao framework e ao ORM.

## Decision Drivers

- Testabilidade do domínio sem subir Spring/JPA.
- Isolar o modelo (`User`) de anotações JPA, permitindo POJO puro.
- Suportar múltiplos adaptadores de entrada (REST + GraphQL) e de saída (JPA, futuramente outros).
- Valor didático: explicitar a fronteira entre domínio e infraestrutura.

## Considered Options

1. Hexagonal / Ports & Adapters.
2. Arquitetura em camadas clássica (Controller -> Service -> Repository JPA).
3. Clean Architecture (Onion).

## Decision Outcome

Escolhido: **Hexagonal**. Estrutura:
- `domain/` — modelos POJO e portas (`UserRepositoryPort`).
- `application/` — orquestração (services, commands, validations).
- `ui/` — adaptadores de entrada (`rest`, `graphql`).
- `infrastructure/` — adaptadores de saída (`persistence`, `config`, `observability`, `web`, `seed`, `exception`).

Domínio não importa `jakarta.persistence` nem Spring Data. Mapeamento `User <-> UserEntity` ocorre em `UserRepositoryAdapter`.

## Pros and Cons of the Options

### Hexagonal
- Bom: domínio testável sem container.
- Bom: trocar JPA por outra tecnologia muda apenas o adaptador.
- Ruim: mais classes (entity + domain + mappers).

### Camadas clássicas
- Bom: menos código, familiar.
- Ruim: entidades JPA vazam para controllers e serviços.

### Clean Architecture
- Bom: similar a hexagonal com mais camadas formais.
- Ruim: overhead alto para o escopo educacional do projeto.

## Consequences

- Toda dependência de framework cruza a fronteira via porta.
- Validações de aplicação (`@SafeHtml`) ficam em `application/validation/`, nunca em `infrastructure/`.
- DTO `UserResponse` desacopla contrato de API do modelo de domínio.
