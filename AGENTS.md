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
mvn clean test -Psmoke         # smoke (API + UI+tag smoke)
mvn clean test -Papi           # apenas API
mvn clean test -Pui            # apenas UI
mvn clean test -Pe2e           # apenas E2E
mvn clean test -Prate-limit    # apenas rate-limit (isolado)
mvn clean test -Pcontract      # apenas testes de contrato (API, JSON Schema)
```

Profiles usam JUnit 5 tags. Test classes devem ter `@Tag("api")`, `@Tag("ui")`, `@Tag("e2e")` e opcionalmente `@Tag("smoke")`.

## Configuração (.env)

`AppConfig` carrega em ordem: **System env > .env > hardcoded defaults**.
Campos: `BASE_URL`, `UI_URL`, `USER_USERNAME`, `USER_PASSWORD`, `USER_EMAIL`, `DEFAULT_CURRENCY`, `DEFAULT_LANGUAGE`, `DEFAULT_TIMEZONE`, `RATE_LIMIT_USERNAME`, `RATE_LIMIT_PASSWORD`, `REPORT_DIR`.

## Paralelismo

JUnit 5 roda em **modo paralelo por classe** (`per_class` lifecycle). Testes devem ser independentes — sem estado compartilhado entre classes.

## Rate limit (isolamento)

O bloqueio do ezBookKeeping é **por IP**, não por usuário. O `LoginRateLimitTest` usa usuário dedicado + `@Execution(SAME_THREAD)`, mas por queimar o IP de localhost ele **nunca roda em runs padrão**: o surefire exclui a tag `rate-limit` por default (`surefire.excludedGroups=rate-limit`). Rode sempre isolado:

```bash
mvn clean test -Prate-limit
```

## Estrutura

O framework vive em `src/main` e os casos de teste em `src/test`.

```
src/main/java/com/ezbookkeeping/qa/
├── config/AppConfig.java          # config via .env
├── core/TestBase.java             # base: RestAssured setup + auto-login/registro
├── api/
│   ├── client/                    # ClientBase, AuthClient
│   └── model/                     # DTOs (AuthResponse, ApiResponse)
├── ui/
│   ├── driver/DriverFactory.java  # Chrome/Firefox
│   └── pages/BasePage.java, LoginPage.java   # Page Objects
├── utils/                         # MoneyUtils (puro), MoneyAssertions, Screenshot
└── fixtures/TestUsers.java        # usuários de teste

src/test/java/com/ezbookkeeping/qa/tests/
├── api/                           # testes de API (por recurso/endpoint)
│   ├── AuthorizeApiTest.java      # POST /api/authorize.json
│   ├── RegisterApiTest.java       # POST /api/register.json
│   ├── TokensApiTest.java         # GET /api/v1/tokens/list.json
│   └── LoginRateLimitTest.java    # rate-limit (isolado)
├── ui/                            # testes de UI (por feature)
└── e2e/                           # testes E2E (futuro)

src/test/resources/schemas/        # JSON Schema estritos dos contratos
├── auth/                          # authorize-response.json, register-response.json
├── v1/                            # tokens-list-response.json (por endpoint autenticado)
└── common/error-response.json     # shape de erro compartilhado
```

## Contrato (JSON Schema)

- Testes de contrato rodam com `mvn clean test -Pcontract` (tag `@Tag("contract")`, também `@Tag("api")`).
- Schemas em `src/test/resources/schemas/<camada>/<endpoint>-response.json`, sempre **estritos**:
  `additionalProperties: false` + todos os campos conhecidos enumerados e tipados em `properties`
  (`required` só para os obrigatórios). Campo novo ausente do schema faz o teste falhar de propósito
  (sinaliza mudança de contrato).
- Interpolação em testes: `.body(matchesJsonSchemaInClasspath("schemas/..."))`.
- Validação em casos de erro usa `schemas/common/error-response.json` (`errorCode`, `errorMessage`,
  `path`, `success: false`).

## Bases

- **`TestBase`** (API): herde nos testes de API. Configura `RestAssured.baseURI`, parser e `authentication` (Bearer token) em `@BeforeAll` sincronizado (safe com paralelismo por classe). Tenta login; se falhar, registra o usuário automaticamente. O token é compartilhado via `RestAssured.oauth2(token)`.
- **`BasePage`** (UI): toda Page Object estende; fornece `driver`, `wait` (15s) e helpers (`esperarVisivel`, `esperarClicavel`, `clicar`, `preencher`, `getUrl`, `esperarUrlContendo`). **Testes de UI NÃO herdam `TestBase`** — criam driver via `DriverFactory` e instanciam a page.

## API do ezBookKeeping

- Base: `http://localhost:8080`
- Pré-auth: `/api/authorize.json`, `/api/register.json`
- Autenticado: `/api/v1/...`
- Headers obrigatórios: `Authorization: Bearer <token>`, timezone (`X-Timezone-Name` ou `X-Timezone-Offset`)
- Response wrapper: `{ "result": ..., "success": true }` ou `{ "errorCode", "errorMessage", "success": false }`

## Convenções

- Package: `com.ezbookkeeping.qa`
- **BigDecimal para dinheiro** — nunca `double`. Usar `MoneyUtils`.
- Nomes de métodos de teste no estilo **`deve...`** (pt-BR, sem acento): `deveAutenticarUsuarioComCredenciaisValidas`, `deveRejeitarLoginComSenhaIncorreta`. Mesmo CT usa o mesmo nome em API e UI.
- Valores em centésimos quando a API representar (`toCents` / `fromCents`)
- **Dados de teste via `fixtures/TestUsers`** principalmente quando exigido isolamento (ex.: rate-limit)
- Dados de teste criados via API sempre que possível
- **Classes de teste API nomeadas por recurso/endpoint** (`AuthorizeApiTest`, `RegisterApiTest`, `TokensApiTest`); o contrato de um endpoint mora na classe do recurso (ex.: tokens/list em `TokensApiTest`), mesmo que a feature doc o referencie. Classes de UI continuam por feature (`LoginUiTest`, `SignUpUiTest`).
- UI tests usam headless Chrome por padrão (`DriverFactory.createChrome()`)
- UI tests usam Page Object (`ui/pages/*`); toda page estende `BasePage`; locators centralizados na page; pages não expõem `WebElement` (métodos de estado como `isXxx()`)
- WebDriver explicit wait: 15s
- `MoneyUtils` é puro (math); assertions ficam em `MoneyAssertions`

## Doc de Funcionalidades

`docs/features/*.md` — histórico de cada funcionalidade (descrição, regras, critérios de aceite para API e UI). Consultar ao implementar testes; manter atualizado.

- `docs/features/login/login.md`
- `docs/features/cadastro-usuario/cadastro-usuario.md`

## CI (.github/workflows/tests.yml)

Roda em `ubuntu-latest` com JDK 21 (Temurin). Sequência: `docker compose up` → aguardar com polling → smoke → regression → upload reports → `docker compose down -v`.
Reports ficam em `reports/` (artifact retido 14 dias).

## Fontes

- [Repositório](https://github.com/mayswind/ezbookkeeping)
- [API Docs](https://ezbookkeeping.mayswind.net/httpapi/)
- [Docker Hub](https://hub.docker.com/r/mayswind/ezbookkeeping)
