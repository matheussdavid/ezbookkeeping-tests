# Casos de Teste API - Login

**Data de geracao:** 04/09/2026
**Autor:** QA Agent
**Fonte:** docs/features/login/login.md

## Resumo Executivo
- **Total de casos de teste:** 11
- **Automatizados:** 7/11 (64%)
- **Distribuicao por prioridade:**
  - P0: 3
  - P1: 5
  - P2: 3
  - P3: 0
- **Criterios de aceite cobertos:** CA-01 (Login com username), CA-02 (Login com email), CA-03 (Senha incorreta), CA-04 (Campos vazios), CA-09 (Rate limiting), CA-10 (Token valido)
- **Regras de negocio cobertas:** 8/11 (regras #5, #6, #8 requerem config do servidor — nao automatizaveis no momento; regra #9 (2FA) removida do escopo)
- **Observacoes gerais:** CT-005/006/008 dependem de configuracao do servidor e nao sao automatizaveis via API.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|-----|----|--------|------------|------|----|-------|
| [x] | CT-001 | Login com username valido | P0 | Positivo | CA-01 | #1, #9 |
| [x] | CT-002 | Login com email valido | P0 | Positivo | CA-02 | #1 |
| [x] | CT-003 | Login com senha incorreta | P0 | Negativo | CA-03 | #4 |
| [x] | CT-004 | Campos obrigatorios vazios | P1 | Borda | CA-04 | #1, #3 |
| [ ] | CT-005 | Login com usuario desabilitado | P1 | Negativo | CA-05 | #5 |
| [ ] | CT-006 | Login com email nao verificado | P1 | Negativo | CA-06 | #6 |
| [x] | CT-007 | Rate limiting excedido | P1 | Excecao | CA-09 | #7 |
| [ ] | CT-008 | Login com senha desabilitada | P1 | Negativo | CA-08 | #8 |
| [x] | CT-009 | Token valido concede acesso | P1 | Positivo | CA-10 | #9 |
| [x] | CT-010 | Senha nao retornada em claro | P2 | Borda | CA-01 | #10 |
| [x] | CT-011 | Formato de username invalido | P2 | Borda | CA-04 | #2 |

---

### CT-001 - Login com username valido
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** Usuario cadastrado com username `tester` e senha `senha123`. Conta ativa (nao desabilitada).
- **Passos:**
  1. Enviar POST /api/authorize.json com `{ "loginName": "tester", "password": "senha123" }`
- **Dados de entrada:** loginName: "tester", password: "senha123"
- **Resultado esperado:** HTTP 200 com `success: true`, `result.token` nao-vazio, `result.user.username` = "tester"
- **CA:** CA-01 - Login com credenciais validas (username)
- **Regra:** #1, #9
- **Observacoes:** Nenhuma.

### CT-002 - Login com email valido
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** Mesmo usuario do CT-001, com email `tester@ezbookkeeping.test` cadastrado.
- **Passos:**
  1. Enviar POST /api/authorize.json com `{ "loginName": "tester@ezbookkeeping.test", "password": "senha123" }`
- **Dados de entrada:** loginName: "tester@ezbookkeeping.test", password: "senha123"
- **Resultado esperado:** HTTP 200 com `success: true`, `result.token` nao-vazio
- **CA:** CA-02 - Login com email
- **Regra:** #1
- **Observacoes:** Login via email funciona como alternativa ao username.

### CT-003 - Login com senha incorreta
- **Prioridade:** P0
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** Usuario `tester` cadastrado e ativo.
- **Passos:**
  1. Enviar POST /api/authorize.json com `{ "loginName": "tester", "password": "senhaErrada" }`
- **Dados de entrada:** loginName: "tester", password: "senhaErrada"
- **Resultado esperado:** HTTP 400 com `success: false`, `errorCode: 201011`
- **CA:** CA-03 - Credenciais invalidas
- **Regra:** #4
- **Observacoes:** Mensagem de erro deve ser generica (nao revela se usuario ou senha esta errado).

### CT-004 - Campos obrigatorios vazios
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/authorize.json com `{ "loginName": "", "password": "" }`
  2. Enviar POST /api/authorize.json com `{ "loginName": "tester" }` (password ausente)
  3. Enviar POST /api/authorize.json com `{ "password": "senha123" }` (loginName ausente)
- **Dados de entrada:** Variacoes de campos vazios/ausentes
- **Resultado esperado:** HTTP 401 com `success: false`, `errorCode: 201001` em todos os casos
- **CA:** CA-04 - Campos obrigatorios vazios
- **Regra:** #1, #3
- **Observacoes:** Binding invalido gera erro antes de consultar o banco.

### CT-005 - Login com usuario desabilitado
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Status:** Nao automatizavel no momento — depende de usuario com `disabled = true`, nao ha endpoint para desabilitar usuario via API. Precisa de fixture manual ou seed data.

### CT-006 - Login com email nao verificado
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Status:** Nao automatizavel no momento — depende de `enableUserForceVerifyEmail` ativo + usuario com `emailVerified = false`. Configuracao do servidor nao controlavel via API.

### CT-007 - Rate limiting excedido
- **Prioridade:** P1
- **Tipo:** Excecao
- **Camada:** API
- **Pre-condicoes:** `maxFailuresPerIpPerMinute` ou `maxFailuresPerUserPerMinute` configurado. Multiplas tentativas falhas consecutivas.
- **Passos:**
  1. Enviar N tentativas de login com credenciais incorretas (excedendo o limite configurado)
  2. Enviar uma tentativa adicional
- **Dados de entrada:** Sequencia de loginName/password incorretos
- **Resultado esperado:** HTTP 400 com `success: false`, `errorCode: 200018`
- **CA:** CA-09 - Rate limiting
- **Regra:** #7
- **Observacoes:** [SUPOSICAO] Assume-se que o limite e por IP e/ou por usuario. O numero exato de tentativas depende da configuracao do ambiente. Isolado em `LoginRateLimitTest` (`@Tag("rate-limit")`) — roda so com `-Dgroups=rate-limit`, nunca na suite normal.

### CT-008 - Login com senha desabilitada
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Status:** Nao automatizavel no momento — depende de `enableInternalAuth = false`. Configuracao do servidor nao controlavel via API.

### CT-009 - Token valido concede acesso a endpoints autenticados
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** Token de sessao valido obtido via login bem-sucedido.
- **Passos:**
  1. Realizar login para obter token valido
  2. Enviar GET /api/v1/tokens/list.json com header `Authorization: Bearer <token>`
- **Dados de entrada:** Token valido
- **Resultado esperado:** HTTP 200 com `success: true`, `result` e uma lista (sessoes do usuario)
- **CA:** CA-10 - Token valido
- **Regra:** #9
- **Observacoes:** Endpoint `/api/v1/tokens/list.json` retorna lista de sessoes autenticadas. Confere que token concede acesso real.

### CT-010 - Senha nao retornada em claro na resposta
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** Login bem-sucedido.
- **Passos:**
  1. Realizar login com credenciais validas
  2. Verificar que o campo `password` nao existe no response body
- **Dados de entrada:** Credenciais validas
- **Resultado esperado:** Campo `password` (ou equivalente) nao esta presente no response. Apenas hash reside no servidor.
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** #10
- **Observacoes:** Seguranca — senha nunca deve ser transmitida em claro na resposta.

### CT-011 - Formato de username invalido no loginName
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/authorize.json com `loginName` contendo caracteres invalidos (ex.: "user name", "user@name", "user name!")
  2. Enviar POST /api/authorize.json com `loginName` excedendo 32 caracteres
  3. Enviar POST /api/authorize.json com `loginName` excedendo 100 caracteres
- **Dados de entrada:** Variacoes de formato invalido
- **Resultado esperado:** HTTP 400 ou HTTP 401 com `success: false` (binding invalido ou credenciais invalidas)
- **CA:** CA-04 - Campos obrigatorios vazios
- **Regra:** #2
- **Observacoes:** [SUPOSICAO] Assume-se que o regex `^(?i)[a-z0-9_-]+$` e aplicado server-side no campo loginName. O erro pode ser de binding (401) ou de credenciais invalidas (400).

---
