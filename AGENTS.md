# ezBookKeeping — QA Automation

## Visão Geral

Automação de testes para o [ezBookKeeping](https://ezbookkeeping.mayswind.net/) (Go + Vue.js).
Stack: Java 21, REST Assured, Selenium WebDriver, JUnit 5, AssertJ, Maven, Docker.

## Setup

```bash
cp .env.example .env          # obrigatório antes do primeiro run
docker compose up -d           # sobe app em :8080
curl http://localhost:8080     # aguardar healthcheck antes de rodar testes
mvn clean test                 # run all
```

## Comandos de teste

```bash
mvn clean test -Psmoke    # smoke (API + UI+tag smoke)
mvn clean test -Papi      # apenas API
mvn clean test -Pui       # apenas UI
mvn clean test -Pe2e      # apenas E2E
```

Profiles usam JUnit 5 tags. Test classes devem ter `@Tag("api")`, `@Tag("ui")`, `@Tag("e2e")` e opcionalmente `@Tag("smoke")`.

## Configuração (.env)

`AppConfig` carrega em ordem: **System env > .env > hardcoded defaults**.
Campos: `BASE_URL`, `UI_URL`, `USER_USERNAME`, `USER_PASSWORD`, `USER_EMAIL`, `DEFAULT_CURRENCY`, `DEFAULT_LANGUAGE`.

## Paralelismo

JUnit 5 roda em **modo paralelo por classe** (`per_class` lifecycle). Testes devem ser independentes — sem estado compartilhado entre classes.

## Estrutura

```
src/test/java/com/ezbookkeeping/qa/
├── config/AppConfig.java          # config via .env
├── api/
│   ├── client/AuthClient.java     # login/register
│   └── model/                     # DTOs (AuthResponse, ApiResponse)
├── tests/
│   ├── TestBase.java              # base: RestAssured setup + auto-login/registro
│   ├── api/                       # testes de API
│   ├── ui/                        # testes de UI
│   └── e2e/                       # testes E2E
├── utils/                         # MoneyUtils, DriverFactory, Screenshot
└── data/                          # dados de teste
```

## TestBase

Herde `TestBase` em todos os testes. Ele configura `RestAssured.baseURI` e `authentication` (Bearer token) em `@BeforeAll`. Tenta login; se falhar, registra o usuário automaticamente. O token é compartilhado via `RestAssured.oauth2(token)`.

## API do ezBookKeeping

- Base: `http://localhost:8080`
- Pré-auth: `/api/authorize.json`, `/api/register.json`
- Autenticado: `/api/v1/...`
- Headers obrigatórios: `Authorization: Bearer <token>`, timezone (`X-Timezone-Name` ou `X-Timezone-Offset`)
- Response wrapper: `{ "result": ..., "success": true }` ou `{ "errorCode", "errorMessage", "success": false }`

## Convenções

- Package: `com.ezbookkeeping.qa`
- **BigDecimal para dinheiro** — nunca `double`. Usar `MoneyUtils`.
- Valores em centésimos quando a API representar (`toCents` / `fromCents`)
- Dados de teste criados via API sempre que possível
- UI tests usam headless Chrome por padrão (`DriverFactory.createChrome()`)
- WebDriver explicit wait: 15s

## Doc de Funcionalidades

`docs/features/*.md` — histórico de cada funcionalidade (descrição, regras, critérios de aceite para API e UI). Consultar ao implementar testes; manter atualizado.

- `docs/features/login.md`
- `docs/features/cadastro-usuario.md`

## CI (.github/workflows/tests.yml)

Roda em `ubuntu-latest` com JDK 21 (Temurin). Sequência: `docker compose up` → aguardar com polling → smoke → regression → upload reports → `docker compose down -v`.
Reports ficam em `reports/` (artifact retido 14 dias).

## Fontes

- [Repositório](https://github.com/mayswind/ezbookkeeping)
- [API Docs](https://ezbookkeeping.mayswind.net/httpapi/)
- [Docker Hub](https://hub.docker.com/r/mayswind/ezbookkeeping)
