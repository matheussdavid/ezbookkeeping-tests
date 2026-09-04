# ezBookKeeping — QA Automation

[![Tests](https://github.com/matheussdavid/ezbookkeeping-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/matheussdavid/ezbookkeeping-tests/actions/workflows/tests.yml)

Automação de testes para o [ezBookKeeping](https://ezbookkeeping.mayswind.net/) — API e UI.

## Stack

- Java 21
- REST Assured 5.5.7
- Selenium WebDriver 4.27.0
- JUnit 5.11.4
- AssertJ 3.27.7
- Maven
- Docker

## Setup

```bash
# 1. Copiar config
cp .env.example .env

# 2. Subir aplicação
docker compose up -d

# 3. Aguardar healthcheck
curl http://localhost:8080

# 4. Rodar testes
mvn clean test
```

> **UI tests requisitos:** Chrome instalado na máquina (headless). O `DriverFactory` gerencia o chromedriver via WebDriverManager.

## Profiles

```bash
mvn clean test -Psmoke    # Smoke suite (API + UI com tag smoke)
mvn clean test -Papi      # Apenas API
mvn clean test -Pui       # Apenas UI
mvn clean test -Pe2e      # Apenas E2E
```

Executar uma classe específica:

```bash
mvn test -Dtest=LoginApiTest        # Classe
mvn test -Dtest="LoginApiTest#loginComUsernameValido"  # Método
```

### Rate limiting

O CT-007 (rate limiting) roda **isolado** em `LoginRateLimitTest` com `@Tag("rate-limit")`. Ele bloqueia o IP por ~1 minuto e **nunca** roda nos profiles padrão:

```bash
mvn test -Dgroups="rate-limit"
```

## Pipeline CI (GitHub Actions)

O workflow `tests.yml` roda **API e UI em jobs separados e paralelos** em cada push na `main`:

- **api** — `-Psmoke -Papi` (smoke) seguido de `-Papi` (regression)
- **ui** — `-Pui` com Chrome headless

Cada job sobe a aplicação via Docker, **semeia o usuário de teste** (`tester`) no SQLite fresco e sobe os reports como artifact (14 dias).

Detalhes de CI:

- CI usa `docker-compose.ci.yml` (volume nomeado p/ SQLite) — o container roda como uid `1000` e um bind mount root causa `permission denied`
- DB é fresco por run — o step `Seed test user` registra o usuário (endpoint exige `nickname` + `categories`)
- UI headless força `intl.accept_languages=pt-BR` — o front traduz pela locale do browser, `--lang` não muda `navigator.language`

## Estrutura

```
src/test/java/com/ezbookkeeping/qa/
├── config/       AppConfig (System env > .env > defaults)
├── api/
│   ├── client/   AuthClient (login/register)
│   └── model/    DTOs (AuthResponse, ApiResponse)
├── tests/
│   ├── TestBase.java        Base: RestAssured setup + auto-login/registro
│   ├── api/                 Testes de API
│   └── ui/                  Testes de UI
└── utils/                   DriverFactory, MoneyUtils, Screenshot

docs/features/<funcionalidade>/
├── <funcionalidade>.md          Doc de funcionalidade (regras, CAs)
└── test-cases/                  CTs de API e UI
```

## Documentação

- [AGENTS.md](AGENTS.md) — contexto completo e convenções do projeto
- [Caso de Teste API - Login](docs/features/login/test-cases/login-api.md)
- [Caso de Teste UI - Login](docs/features/login/test-cases/login-ui.md)