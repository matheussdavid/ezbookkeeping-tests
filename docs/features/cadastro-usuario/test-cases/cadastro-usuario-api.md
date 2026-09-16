# Casos de Teste API - Cadastro de Usuario

**Data de geracao:** 14/09/2026 (atualizado: 15/09/2026)
**Autor:** QA Agent
**Fonte:** docs/features/cadastro-usuario/cadastro-usuario.md

## Resumo Executivo
- **Total de casos de teste:** 18
- **Automatizados:** 16/18 (89%)
- **Distribuicao por prioridade:**
  - P0: 6
  - P1: 5
  - P2: 7
  - P3: 0
- **Criterios de aceite cobertos:** CA-01 (Cadastro com sucesso), CA-02 (Token autentica /api/v1), CA-03 (Username duplicado), CA-04 (Email duplicado), CA-05 (Moeda invalida), CA-06 (Registro desabilitado), CA-07 (Campo obrigatorio ausente/malformado), CA-08 (Verificacao de email), CA-09 (Senha < 6)
- **Regras de negocio cobertas:** 14/14 (API)
- **Observacoes gerais:** CT-001 a CT-002 (contrato, JSON Schema estrito) ja automatizados. O contrato de GET /api/v1/tokens/list.json (que antes era o CT-003 deste doc) agora vive em `TokensApiTest` — fora deste doc de feature; o CT-004 (token autentica usuario), que usa tokens/list como validacao, permanece aqui. CT-008 (registro desabilitado) e CT-010 (verificacao de email) dependem de configuracao do servidor (`enableUserRegister`, `enableUserForceVerifyEmail`) nao controlavel via API — nao automatizaveis no momento.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-001 | Contrato do response de sucesso do POST /api/register.json | P0 | Contrato | - | - |
| [x]    | CT-002 | Contrato do response de erro do POST /api/register.json | P0 | Contrato | - | - |
| [x]    | CT-003 | Cadastro com sucesso (happy path) | P0 | Positivo | CA-01 | #1, #6, #13 |
| [x]    | CT-004 | Token autentica usuario recem-criado em /api/v1 | P0 | Positivo | CA-02 | #1, #6 |
| [x]    | CT-005 | Cadastro com username duplicado | P0 | Negativo | CA-03 | #1 |
| [x]    | CT-006 | Cadastro com email duplicado | P0 | Negativo | CA-04 | #2 |
| [x]    | CT-007 | Moeda padrao invalida | P1 | Negativo | CA-05 | #7 |
| [ ]    | CT-008 | Registro desabilitado | P1 | Negativo | CA-06 | #10 |
| [x]    | CT-009 | Campos obrigatorios ausentes | P1 | Borda | CA-07 | #1, #4, #5, #6, #9 |
| [ ]    | CT-010 | Registro com verificacao de email ativa | P1 | Positivo | CA-08 | #11 |
| [x]    | CT-011 | Senha com menos de 6 caracteres | P1 | Borda | CA-09 | #6 |
| [x]    | CT-012 | Formato de username invalido | P2 | Borda | CA-07 | #3 |
| [x]    | CT-013 | Formato de email invalido | P2 | Borda | CA-07 | #4 |
| [x] | CT-014 | Trim de nickname no cadastro | P2 | Borda | CA-01 | #12 |
| [x] | CT-015 | Senha cadastrada autentica login pos-cadastro | P2 | Borda | CA-01 | #6 |
| [x] | CT-016 | firstDayOfWeek fora do intervalo 0-6 | P2 | Borda | CA-07 | #8 |
| [x] | CT-017 | Categorias iniciais em lote | P2 | Positivo | CA-01 | #14 |
| [x] | CT-018 | Valores no limite maximo de tamanho | P2 | Borda | CA-01 | #3, #4, #5, #6, #9 |

---

### CT-001 - Contrato do response de sucesso do POST /api/register.json
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/auth/register-response.json`
- **Pre-condicoes:** API disponivel. Username e e-mail ainda nao utilizados.
- **Passos:**
  1. Enviar POST /api/register.json com payload completo e valido
  2. Validar o response body contra o schema estrito
- **Dados de entrada:** Payload valido (username/email/nickname/password/language/defaultCurrency/firstDayOfWeek)
- **Resultado esperado:** HTTP 200; o body valida contra `schemas/auth/register-response.json` (schema estrito: `additionalProperties: false`, todos os campos conhecidos de `result` e `user` enumerados e tipados). Campo ausente, extra ou tipo errado falha o teste.
- **CA:** -
- **Regra:** -
- **Observacoes:** Contrato estrito — campo novo no response sem atualizacao do schema e intencionalmente tratado como falha. `result.token` e condicional (ausente quando `enableUserForceVerifyEmail` ativo) — representado de forma condicional no schema. Executa em `RegisterApiTest`.

### CT-002 - Contrato do response de erro do POST /api/register.json
- **Prioridade:** P0
- **Tipo:** Contrato
- **Camada:** API
- **Schema:** `schemas/common/error-response.json`
- **Pre-condicoes:** Username ja cadastrado (para forcar erro).
- **Passos:**
  1. Enviar POST /api/register.json com username ja em uso
  2. Validar o response body contra o schema estrito
- **Dados de entrada:** username duplicado + demais campos validos
- **Resultado esperado:** HTTP 400; o body valida contra `schemas/common/error-response.json` (`{ errorCode, errorMessage, path, success: false }`, estrito). Campo ausente, extra ou tipo errado falha o teste.
- **CA:** -
- **Regra:** -
- **Observacoes:** Contrato estrito do shape de erro compartilhado pela API. Executa em `RegisterApiTest`.

### CT-003 - Cadastro com sucesso (happy path)
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username e e-mail ainda nao utilizados.
- **Passos:**
  1. Enviar POST /api/register.json com payload completo e valido
  2. Verificar o response body
- **Dados de entrada:** Payload gerado por `UserFaker.randomRegister()` (username/email/nickname unicos, password, language e defaultCurrency do AppConfig)
- **Resultado esperado:** HTTP 200 com `success: true`; `result.token` nao-vazio (JWT); `user.username/email/nickname/language/defaultCurrency` iguais aos enviados; `needVerifyEmail: false`; `presetCategoriesSaved` conforme payload
- **CA:** CA-01 - Cadastro com sucesso (200, success true, uid + token, needVerifyEmail false)
- **Regra:** #1, #6, #13
- **Observacoes:** Usar username/e-mail unicos por execucao (`UserFaker`). Executa em `RegisterApiTest`.

### CT-004 - Token autentica usuario recem-criado em /api/v1
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
- **Observacoes:** Endpoint `/api/v1/tokens/list.json` usado como validacao; o contrato desse endpoint vive em `TokensApiTest`. Executa em `RegisterApiTest`.

### CT-005 - Cadastro com username duplicado
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
- **Observacoes:** Validado em `RegisterApiTest`. A unicidade do username e **case-sensitive**: `USUARIO_EXISTENTE` (maiusculas) cadastra com sucesso (200) — o regex `^(?i)[a-z0-9_-]+$` da Regra #3 so valida formato, nao unicidade. Teste positivo separado `deveCadastrarUsuarioComUsernameDiferindoApenasPorCaixa` documenta esse comportamento.

### CT-006 - Cadastro com email duplicado
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

### CT-007 - Moeda padrao invalida
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
- **Observacoes:** Moeda com tamanho != 3 e tratada como binding invalido (CT-009/CA-07), nao como este codigo.

### CT-008 - Registro desabilitado
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

### CT-009 - Campos obrigatorios ausentes
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
- **Observacoes:** Binding invalido ocorre antes de qualquer consulta ao banco. Campo de `firstDayOfWeek` e opcional — nao faz parte desta validacao. Campo ausente e vazio (`""`) retornam a mesma mensagem: `parameter "X" is required`. Executa como `@ParameterizedTest` + `@MethodSource` (12 cenarios) em `RegisterApiTest` — campo ausente omitido via `@JsonInclude(NON_NULL)` no DTO.

### CT-010 - Registro com verificacao de email ativa
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

### CT-011 - Senha com menos de 6 caracteres
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
- **Observacoes:** Minimo de 6 caracteres (incluido no CT-018 o caso de 128).

### CT-012 - Formato de username invalido
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
- **Observacoes:** Mensagens reais: `parameter "username" is invalid username format` (caracter invalido) e `parameter "username" must be less than 32 characters` (tamanho). Executa como `@ParameterizedTest` em `RegisterApiTest`.

### CT-013 - Formato de email invalido
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
- **Observacoes:** Validacao RFC 5322 server-side (case-insensitive). Mensagens reais: `parameter "email" is invalid email format` (formato invalido) e `parameter "email" must be less than 100 characters` (tamanho). Executa como `@ParameterizedTest` em `RegisterApiTest`.

### CT-014 - Trim de nickname no cadastro
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username/email/nickname nao utilizados.
- **Passos:**
  1. Enviar POST /api/register.json com `nickname: "  Novo Usuario  "` (espacos ao redor) e demais campos validos
  2. Verificar o valor do nickname no response
- **Dados de entrada:** nickname com espacos no inicio/fim
- **Resultado esperado:** HTTP 200 com `success: true`; `user.nickname` = "Novo Usuario" (sem espacos)
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #12
- **Observacoes:** O trim so aplica ao **nickname** (unico campo sem validacao de formato). Para username/email os espacos sao rejeitados pela validacao de formato ANTES do trim: `username: "  novo_user  "` → 400 `parameter "username" is invalid username format`; `email: "  novo@example.com  "` → 400 `parameter "email" is invalid email format` (ja cobertos como formato invalido em CT-012/CT-013; aqui validados explicitamente como cenarios de trim rejeitado). A Regra #12 ("trim em username/email") e **inalcancavel** via API normal — validacao de formato bloqueia espacos primeiro. Executa em `RegisterApiTest`: `deveAplicarTrimNoNicknameAoCadastrar` (positivo) + `deveRejeitarUsernameEmailComEspacos` (negativo, 2 cenarios).

### CT-015 - Senha cadastrada autentica login pos-cadastro
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** API
- **Pre-condicoes:** Cadastro realizado com sucesso.
- **Passos:**
  1. Realizar cadastro via POST /api/register.json
  2. Inspecionar o response body completo (confirmar que o valor da senha NAO aparece em claro)
  3. Realizar login com a senha cadastrada (POST /api/authorize.json)
- **Dados de entrada:** Senha gerada via `UserFaker.password()` (8-16 chars)
- **Resultado esperado:** Response register NAO contem o valor da senha em claro; login com a mesma senha retorna HTTP 200 com `success: true` e `result.token` nao-vazio (prova que o hash PBKDF2 + salt foi aplicado e a comparacao funciona)
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #6
- **Observacoes:** Hash PBKDF2 (SHA-256, 10.000 iteracoes, 48 bytes) + salt verificado indiretamente. A ausencia estrutural de `password` no response register ja e garantida pelo schema estrito do CT-001 (`additionalProperties: false`); este CT reforca com assert defensivo `doesNotContain(senha)` no body + valida o roundtrip de login. Executa em `RegisterApiTest.devePermitirLoginComSenhaCadastrada`.

### CT-016 - firstDayOfWeek fora do intervalo 0-6
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
- **Observacoes:** Campo opcional, mas quando enviado deve ser inteiro 0-6 (WeekDay). Mensagem real para valor fora do intervalo: `parameter "firstDayOfWeek" must be less than 6`. Executa em `RegisterApiTest.deveRejeitarCadastroComFirstDayOfWeekInvalido`.

### CT-017 - Categorias iniciais em lote
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** API
- **Pre-condicoes:** API disponivel. Username/email unicos.
- **Passos:**
  1. Enviar POST /api/register.json com lote de categorias no shape real: `{ name, type, icon, color, subCategories: [{ name, type, icon, color }] }`
  2. Verificar `presetCategoriesSaved` no response
  3. (robustencimento) Validar categorias salvas via GET /api/v1/transaction/categories/list.json com o token
- **Dados de entrada:** Lote de 3 categorias (ex.: Alimentacao/Transporte type 2, Salario type 1) com subcategorias
- **Resultado esperado:** HTTP 200 com `success: true`; `presetCategoriesSaved: true`; GET categories/list retorna exatamente os nomes enviados
- **CA:** CA-01 - Cadastro com sucesso
- **Regra:** #14
- **Observacoes:** Shape real das categorias e `{ name, type (1=receita, 2=despesa, 3=transferencia), icon, color, subCategories }` — diferente do simplificado no CT original. Server salva as categorias **verbatim** (nome custom `ZZZ_CAT_UNICA` confirmado via GET). Registro SEM categories → server auto-cria catalogo default do locale (por isso `presetCategoriesSaved: false` com `categories: []`). Validacao pos-cadastro usa `auth().oauth2(token)` (nao header Authorization explicito) para sobrescrever a auth global do `RestAssured` do TestBase. Executa em `RegisterApiTest.deveSalvarCategoriasIniciaisEmLote`.

### CT-018 - Valores no limite maximo de tamanho
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
- **Observacoes:** Limites inclusive confirmados: 32/100/64/128/16 → HTTP 200 com `success: true`; 33/101/65/129/17 → HTTP 400. Mensagens reais (padrao `parameter "X" must be less than N characters`): username `...less than 32 characters`, email `...less than 100 characters`, nickname `...less than 64 characters`, password `...less than 128 characters`, language `...less than 16 characters`. Positivo usa valores aleatorios no limite (unicidade); `@ParameterizedTest` com metodo `cenariosAcimaDoLimite`. Executa em `RegisterApiTest.deveCadastrarUsuarioComValoresNoLimiteMaximo` e `deveRejeitarValoresAcimaDoLimite`.

---