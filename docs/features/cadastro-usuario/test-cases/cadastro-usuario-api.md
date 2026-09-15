# Casos de Teste API - Cadastro de Usuario

**Data de geracao:** 14/09/2026
**Autor:** QA Agent
**Fonte:** docs/features/cadastro-usuario/cadastro-usuario.md

## Resumo Executivo
- **Total de casos de teste:** 16
- **Executados:** 14/16 (88%)
- **Distribuicao por prioridade:**
  - P0: 4
  - P1: 5
  - P2: 7
  - P3: 0
- **Criterios de aceite cobertos:** CA-01 (Cadastro com sucesso), CA-02 (Token autentica /api/v1), CA-03 (Username duplicado), CA-04 (Email duplicado), CA-05 (Moeda invalida), CA-06 (Registro desabilitado), CA-07 (Campo obrigatorio ausente/malformado), CA-08 (Verificacao de email), CA-09 (Senha < 6)
- **Regras de negocio cobertas:** 14/14 (API)
- **Observacoes gerais:** CT-006 (registro desabilitado) e CT-008 (verificacao de email) dependem de configuracao do servidor (`enableUserRegister`, `enableUserForceVerifyEmail`) nao controlavel via API — nao automatizaveis no momento.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Cadastro com sucesso (happy path) | P0 | Positivo | CA-01 | #1, #6, #13 |
| [ ] | CT-002 | Token autentica usuario recem-criado em /api/v1 | P0 | Positivo | CA-02 | #1, #6 |
| [ ] | CT-003 | Cadastro com username duplicado | P0 | Negativo | CA-03 | #1 |
| [ ] | CT-004 | Cadastro com email duplicado | P0 | Negativo | CA-04 | #2 |
| [ ] | CT-005 | Moeda padrao invalida | P1 | Negativo | CA-05 | #7 |
| [ ] | CT-006 | Registro desabilitado | P1 | Negativo | CA-06 | #10 |
| [ ] | CT-007 | Campos obrigatorios ausentes | P1 | Borda | CA-07 | #1, #4, #5, #6, #9 |
| [ ] | CT-008 | Registro com verificacao de email ativa | P1 | Positivo | CA-08 | #11 |
| [ ] | CT-009 | Senha com menos de 6 caracteres | P1 | Borda | CA-09 | #6 |
| [ ] | CT-010 | Formato de username invalido | P2 | Borda | CA-07 | #3 |
| [ ] | CT-011 | Formato de email invalido | P2 | Borda | CA-07 | #4 |
| [ ] | CT-012 | Trim de username, email e nickname | P2 | Borda | CA-01 | #12 |
| [ ] | CT-013 | Senha nao retornada em claro | P2 | Borda | CA-01 | #6 |
| [ ] | CT-014 | firstDayOfWeek fora do intervalo 0-6 | P2 | Borda | CA-07 | #8 |
| [ ] | CT-015 | Categorias iniciais em lote | P2 | Positivo | CA-01 | #14 |
| [ ] | CT-016 | Valores no limite maximo de tamanho | P2 | Borda | CA-01 | #3, #4, #5, #6, #9 |

---

### CT-001 - Cadastro com sucesso (happy path)
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username e e-mail ainda nao utilizados.
- **Passos:**
  1. Enviar POST /api/register.json com payload completo e valido
  2. Verificar o response body
- **Dados de entrada:** `username: "novo_usuario"`, `email: "novo@example.com"`, `nickname: "Novo Usuario"`, `password: "senha123"`, `language: "en-US"`, `defaultCurrency: "BRL"`, `firstDayOfWeek: 1`
- **Resultado esperado:** HTTP 200 com `success: true`; `result.token` nao-vazio (JWT); `result.user.uid` preenchido (UUID); `user.username/email/nickname/language/defaultCurrency` iguais aos enviados; `needVerifyEmail: false`; `presetCategoriesSaved: true`
- **CA:** CA-01 - Cadastro com sucesso (200, success true, uid + token, needVerifyEmail false)
- **Regra:** #1, #6, #13
- **Observacoes:** Usar username/e-mail unicos por execucao (ex.: sufixo timestamp). Nenhuma.

### CT-002 - Token autentica usuario recem-criado em /api/v1
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** Cadastro realizado com sucesso (token recem-obtido via register).
- **Passos:**
  1. Realizar cadastro via POST /api/register.json
  2. Enviar GET /api/v1/tokens/list.json com header `Authorization: Bearer <token>` e timezone (`X-Timezone-Name`)
- **Dados de entrada:** Token da resposta do cadastro
- **Resultado esperado:** HTTP 200 com `success: true`; `result` e uma lista de sessoes do usuario recem-criado (validando que o token concede acesso autenticado)
- **CA:** CA-02 - Token autentica usuario nos endpoints /api/v1/...
- **Regra:** #1, #6
- **Observacoes:** [SUPOSICAO] Endpoint `/api/v1/tokens/list.json` usado para validar o token, mesma convencao adotada nos CTs de login. Se houver endpoint mais adequado (ex.: profile), usar como alternativa.

### CT-003 - Cadastro com username duplicado
- **Prioridade:** P0
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** Usuario com username `usuario_existente` ja cadastrado.
- **Passos:**
  1. Enviar POST /api/register.json com username `usuario_existente` e demais campos validos e unicos
- **Dados de entrada:** `username: "usuario_existente"`, email/nickname/password/language/currency validos
- **Resultado esperado:** HTTP 400 com `success: false` e `errorCode: 201012` (ErrUsernameAlreadyExists)
- **CA:** CA-03 - Username duplicado
- **Regra:** #1
- **Observacoes:** Validar tambem variacao case-insensitive (ex.: `USUARIO_EXISTENTE`) conforme regex case-insensitive da Regra #3.

### CT-004 - Cadastro com email duplicado
- **Prioridade:** P0
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** Usuario com email `existente@example.com` ja cadastrado.
- **Passos:**
  1. Enviar POST /api/register.json com email `existente@example.com` e username/nickname unicos
- **Dados de entrada:** `email: "existente@example.com"`, demais campos validos
- **Resultado esperado:** HTTP 400 com `success: false` e `errorCode: 201013` (ErrUserEmailAlreadyExists)
- **CA:** CA-04 - Email duplicado
- **Regra:** #2
- **Observacoes:** E-mail e comparado case-insensitive (Regra #4) — variacao de maiusculas tambem deve ser rejeitada.

### CT-005 - Moeda padrao invalida
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json com `defaultCurrency: "XYZ"` (3 caracteres, nao-ISO 4217) e demais campos validos
- **Dados de entrada:** `defaultCurrency: "XYZ"`
- **Resultado esperado:** HTTP 400 com `success: false` e `errorCode: 201009` (ErrUserDefaultCurrencyIsInvalid)
- **CA:** CA-05 - Moeda invalida
- **Regra:** #7
- **Observacoes:** Moeda com tamanho != 3 e tratada como binding invalido (CT-007/CA-07), nao como este codigo.

### CT-006 - Registro desabilitado
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** API
- **Status:** Nao automatizavel no momento — depende da configuracao `enableUserRegister = false` no servidor, nao controlavel via API.
- **Pre-condicoes:** Servidor com `enableUserRegister = false`.
- **Passos:**
  1. Enviar POST /api/register.json com payload valido
- **Dados de entrada:** Payload valido (qualquer)
- **Resultado esperado:** HTTP 400 com `success: false` e `errorCode: 201014` (ErrUserRegistrationNotAllowed)
- **CA:** CA-06 - Registro desabilitado
- **Regra:** #10
- **Observacoes:** Requer fixture manual ou seed de configuracao do servidor.

### CT-007 - Campos obrigatorios ausentes
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json removendo 1 campo por vez: username, email, nickname, password, language, defaultCurrency
  2. Enviar POST com os mesmos campos preenchidos com strings vazias ("")
- **Dados de entrada:** Variacoes de campo ausente/vazio para cada obrigatorio
- **Resultado esperado:** HTTP 400 com `success: false` em todos os casos; mensagem descritiva de submissao incompleta/incorreta (sem `errorCode` numerico dedicado)
- **CA:** CA-07 - Campo obrigatorio ausente ou malformado
- **Regra:** #1, #4, #5, #6, #9
- **Observacoes:** Binding invalido ocorre antes de qualquer consulta ao banco. Campo de `firstDayOfWeek` e opcional — nao faz parte desta validacao.

### CT-008 - Registro com verificacao de email ativa
- **Prioridade:** P1
- **Tipo:** Positivo
- **Camada:** API
- **Status:** Nao automatizavel no momento — depende da configuracao `enableUserForceVerifyEmail = true` no servidor.
- **Pre-condicoes:** Servidor com `enableUserForceVerifyEmail = true`.
- **Passos:**
  1. Enviar POST /api/register.json com payload valido
  2. Verificar o response body
- **Dados de entrada:** Payload valido (cadastro feliz)
- **Resultado esperado:** HTTP 200 com `success: true`, `result.needVerifyEmail: true` e **ausencia** de `result.token` (sem sessao)
- **CA:** CA-08 - Verificacao de email
- **Regra:** #11
- **Observacoes:** Conta criada com email nao verificado. Requer configuracao do servidor.

### CT-009 - Senha com menos de 6 caracteres
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json com `password: "12345"` (5 caracteres)
  2. Enviar POST com `password: ""` (vazia)
- **Dados de entrada:** Senhas com 5 e 0 caracteres
- **Resultado esperado:** HTTP 400 com `success: false` e mensagem descritiva de binding invalido (sem `errorCode` dedicado)
- **CA:** CA-09 - Senha sem caracteres minimos
- **Regra:** #6
- **Observacoes:** Minimo de 6 caracteres (incluido no CT-016 o caso de 128).

### CT-010 - Formato de username invalido
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json com username contendo caracteres fora de `[a-z0-9_-]` (ex.: "user name", "user@name", "usuario")
  2. Enviar POST com username excedendo 32 caracteres (33 chars)
- **Dados de entrada:** Variacoes de username invalido
- **Resultado esperado:** HTTP 400 com `success: false` e mensagem descritiva de binding invalido
- **CA:** CA-07 - Campo obrigatorio ausente ou malformado
- **Regra:** #3
- **Observacoes:** [SUPOSICAO] Assumido regex `^(?i)[a-z0-9_-]+$` aplicado server-side; retorno descritivo sem `errorCode` dedicado.

### CT-011 - Formato de email invalido
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json com email sem arroba (ex.: "usuario.example.com")
  2. Enviar POST com email sem dominio (ex.: "usuario@")
  3. Enviar POST com email excedendo 100 caracteres
- **Dados de entrada:** Variacoes de email invalido
- **Resultado esperado:** HTTP 400 com `success: false` e mensagem descritiva de binding invalido
- **CA:** CA-07 - Campo obrigatorio ausente ou malformado
- **Regra:** #4
- **Observacoes:** Validacao RFC 5322 server-side (case-insensitive).

### CT-012 - Trim de username, email e nickname
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username/email/nickname nao utilizados.
- **Passos:**
  1. Enviar POST /api/register.json com `username: "  novo_user  "`, `email: "  novo@example.com  "` e `nickname: "  Novo Usuario  "` (espacos ao redor)
  2. Verificar os valores do usuario criado no response
- **Dados de entrada:** Campos com espacos no inicio/fim
- **Resultado esperado:** HTTP 200 com `success: true`; `user.username` = "novo_user", `user.email` = "novo@example.com", `user.nickname` = "Novo Usuario" (sem espacos)
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #12
- **Observacoes:** Validar que espacos nao fazem parte do valor persistido (tambem evita falsos positivos de unicidade).

### CT-013 - Senha nao retornada em claro
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** Cadastro realizado com sucesso.
- **Passos:**
  1. Realizar cadastro via POST /api/register.json
  2. Inspecionar o response body completo
  3. Realizar login com a senha cadastrada (POST /api/authorize.json)
- **Dados de entrada:** Senha "senha123"
- **Resultado esperado:** O campo `password` (ou equivalente) NAO esta presente no response; o login com a mesma senha funciona (confirmando hash PBKDF2 aplicado server-side)
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #6
- **Observacoes:** Hash PBKDF2 (SHA-256, 10.000 iteracoes, 48 bytes) + salt verificado indiretamente — nunca transmitido em claro.

### CT-014 - firstDayOfWeek fora do intervalo 0-6
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel.
- **Passos:**
  1. Enviar POST /api/register.json com `firstDayOfWeek: -1`
  2. Enviar POST com `firstDayOfWeek: 7`
- **Dados de entrada:** firstDayOfWeek -1 e 7
- **Resultado esperado:** HTTP 400 com `success: false` e mensagem descritiva de binding invalido
- **CA:** CA-07 - Campo obrigatorio ausente ou malformado
- **Regra:** #8
- **Observacoes:** [SUPOSICAO] Campo opcional, mas quando enviado deve ser inteiro 0-6 (WeekDay). Validar tambem valor nao-inteiro (String).

### CT-015 - Categorias iniciais em lote
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username/email unicos.
- **Passos:**
  1. Enviar POST /api/register.json com `categories: [{ "name": "Alimentacao" }, { "name": "Transporte" }, { "name": "Lazer" }]`
  2. Verificar o campo `presetCategoriesSaved` no response
- **Dados de entrada:** Lote de 3 categorias iniciais
- **Resultado esperado:** HTTP 200 com `success: true` e `presetCategoriesSaved: true`
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #14
- **Observacoes:** [SUPOSICAO] Validacao pos-cadastro das categorias (ex.: via GET /api/v1/categories/list.json com o token) fica como passo opcional de robustecimento.

### CT-016 - Valores no limite maximo de tamanho
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username/email unicos.
- **Passos:**
  1. Enviar POST /api/register.json com username de 32 caracteres, email de 100, nickname de 64, password de 128 e language de 16 (todos no limite)
  2. Enviar POST com cada campo com 1 caractere a mais (excedendo o limite)
- **Dados de entrada:** Valores no limite e acima do limite por campo
- **Resultado esperado:** Valores exatamente no limite → HTTP 200 com `success: true`; valores acima do limite → HTTP 400 com `success: false` (binding invalido)
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #3, #4, #5, #6, #9
- **Observacoes:** [SUPOSICAO] Limites inclusive (32/100/64/128/16 aceitos, 33/101/65/129/17 rejeitados).

---