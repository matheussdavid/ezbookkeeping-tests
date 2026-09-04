# ezBookKeeping — QA Automation

Automação de testes para o [ezBookKeeping](https://ezbookkeeping.mayswind.net/) — API, UI e E2E.

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
# Copiar config
cp .env.example .env

# Subir aplicação
docker compose up -d

# Aguardar healthcheck
curl http://localhost:8080

# Rodar testes
mvn clean test
```

## Profiles

```bash
mvn clean test -Psmoke    # Smoke suite
mvn clean test -Papi      # Apenas API
mvn clean test -Pui       # Apenas UI
mvn clean test -Pe2e      # Apenas E2E
```

## Estrutura

```
src/test/java/com/ezbookkeeping/qa/
├── config/       Configuração
├── api/          Client e model para API
├── ui/           Page Objects
├── tests/        Testes (api/ui/e2e)
├── data/         Dados de teste
└── utils/        Utilitários
```

## Documentação

Ver [AGENTS.md](AGENTS.md) para contexto completo do projeto.
