# BIP Beneficios

Solucao fullstack em camadas para o desafio BIP: banco de dados, modulo EJB, **bip-beneficios-backend** e **bip-beneficios-frontend**, testes e CI.

## Arquitetura

```mermaid
flowchart LR
  db[(H2 DB)] --> ejb[EJB Module]
  ejb --> api[bip-beneficios-backend]
  api --> swagger[Swagger UI]
  api --> app[bip-beneficios-frontend]
```

## Stack

- Java 11, Maven 3+
- Spring Boot 2.7, Spring Data JPA, H2
- EJB/JPA no modulo `ejb-module`
- Angular 21
- Swagger/OpenAPI via Springdoc

## Organizacao

```text
bip-beneficios/
  bip-beneficios-backend/   # API Spring Boot
  bip-beneficios-frontend/  # App Angular
  ejb-module/
  db/
```

No backend (`bip-beneficios-backend`), o modulo de beneficios segue:

- `controller`
- `service`
- `repository`
- `dto`
- `model`

Configuracao: `bip-beneficios-backend/src/main/resources/application.yml`

## Como Rodar

### Backend

```powershell
cd d:\bip-beneficios
mvn test
mvn -pl bip-beneficios-backend spring-boot:run
```

A API sobe em `http://localhost:8080`.

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Frontend

```powershell
cd d:\bip-beneficios\bip-beneficios-frontend
npm install
npm start
```

O Angular sobe em `http://localhost:4200` e usa `proxy.conf.json` para chamar o backend em `http://localhost:8080`.

## Banco de Dados

- `db/schema.sql`
- `db/seed.sql`

Para desenvolvimento local, o backend carrega equivalentes em `bip-beneficios-backend/src/main/resources/schema.sql` e `data.sql`.

## Endpoints Principais

- `GET /api/v1/beneficios`
- `GET /api/v1/beneficios/{id}`
- `POST /api/v1/beneficios`
- `PUT /api/v1/beneficios/{id}`
- `DELETE /api/v1/beneficios/{id}`
- `POST /api/v1/beneficios/transferencias`

## Testes

| Camada | O que cobre |
|--------|-------------|
| `ejb-module` | Saldo, origem=destino, ordem de lock, benefício inativo |
| `bip-beneficios-backend` | Service unitário (CRUD/transferência) + integração MockMvc (CRUD, 404, validação, transferência) |
| `bip-beneficios-frontend` | Componente, validações, máscara BRL, fluxos de modal |

```powershell
cd d:\bip-beneficios
mvn test

cd d:\bip-beneficios\bip-beneficios-frontend
npm test
```

## Correção do bug no EJB

O `BeneficioEjbService` original permitia transferências sem validar saldo nem concorrência. A implementação atual:

- Valida origem/destino, valor positivo e benefícios **ativos**
- Verifica **saldo suficiente** antes de debitar
- Usa `PESSIMISTIC_WRITE` e bloqueia registros sempre na **ordem crescente de ID** (evita deadlock)
- Exceções anotadas com `@ApplicationException(rollback = true)` para reverter a transação

## Critérios do desafio (mapa)

| Critério | Peso | Onde está |
|----------|------|-----------|
| Arquitetura em camadas | 20% | `db` → `ejb-module` → `bip-beneficios-backend` (controller/service/repository/dto/model) → Angular por feature |
| Correção EJB | 20% | `BeneficioEjbService`, testes em `BeneficioEjbServiceTest` |
| CRUD + transferência | 15% | REST `/api/v1/beneficios` + `/transferencias`, UI com modais |
| Qualidade de código | 10% | Validação Bean Validation, `@Slf4j`, controller fino, `groupId` Maven `com.bip` |
| Testes | 15% | JUnit/Mockito backend + EJB + Jasmine frontend |
| Documentação | 10% | Este README, Swagger, `docs/README.md` |
| Frontend | 10% | Angular 21, toasts, modais, PT-BR, proxy para API |

Detalhes do enunciado: `docs/README.md`.

## Publicacao

Este projeto foi criado por clone do template, sem fork. Para entregar, crie um novo repositorio publico na sua conta GitHub e publique esta pasta como projeto independente.
