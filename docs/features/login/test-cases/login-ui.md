# Casos de Teste UI - Login

**Data de geracao:** 04/09/2026
**Autor:** QA Agent
**Fonte:** docs/features/login/login.md

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 0/10 (0%)
- **Distribuicao por prioridade:**
  - P0: 2
  - P1: 6
  - P2: 1
  - P3: 1
- **Criterios de aceite cobertos:** CA-01 (Login com credenciais validas), CA-02 (Enter submete login), CA-03 (Username vazio), CA-04 (Password vazio), CA-05 (Campos vazios — botao desabilitado), CA-06 (Credencial incorreta), CA-07 (2FA — campo Passcode), CA-08 (Email nao verificado — redireciona)
- **Regras de negocio cobertas:** 6/6
- **Observacoes gerais:** CTs de UI focam em comportamento visivel ao usuario (estados de elementos, navegacao, feedback). Testes de 2FA completos (passcode + backup code) cobertos parcialmente — fluxo completo depende de configuracao 2FA no ambiente.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [ ] | CT-001 | Login com credenciais validas | P0 | Positivo | CA-01 | #1 |
| [ ] | CT-002 | Enter no campo senha submete login | P0 | Positivo | CA-02 | #2 |
| [ ] | CT-003 | Campo Username vazio — nao chama API | P1 | Negativo | CA-03 | #1, #5 |
| [ ] | CT-004 | Campo Password vazio — nao chama API | P1 | Negativo | CA-04 | #1, #5 |
| [ ] | CT-005 | Campos vazios — botao desabilitado | P1 | Borda | CA-05 | #2 |
| [ ] | CT-006 | Credencial incorreta — snackbar erro | P1 | Negativo | CA-06 | #5 |
| [ ] | CT-007 | 2FA — campo Passcode aparece | P1 | Negativo | CA-07 | #3, #6 |
| [ ] | CT-008 | Email nao verificado — redireciona | P1 | Negativo | CA-08 | #3 |
| [ ] | CT-009 | Duplo clique bloqueado | P2 | Borda | CA-01 | #4 |
| [ ] | CT-010 | Links Forget Password e Create account | P3 | Borda | CA-01 | N/A |

---

### CT-001 - Login com credenciais validas
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** Usuario valido existente. Tela de login carregada em `/login`.
- **Passos:**
  1. Navegar para a pagina de login
  2. Verificar que o campo Username recebe foco automatico
  3. Preencher campo Username com "tester"
  4. Preencher campo Password com "senha123"
  5. Verificar que o botao "Log In" esta habilitado
  6. Clicar no botao "Log In"
- **Dados de entrada:** Username: "tester", Password: "senha123"
- **Resultado esperado:** Apos clicar em "Log In", o usuario e autenticado, navegado para a pagina inicial (`/`), e os dados (idioma, cor, moeda) sao aplicados conforme o perfil do usuario.
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** #1
- **Observacoes:** Durante o envio, os campos e o botao ficam desabilitados (estado `loggingInByPassword`).

### CT-002 - Enter no campo senha submete o login
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. Credenciais validas conhecidas.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher campo Username com "tester"
  3. Preencher campo Password com "senha123"
  4. Pressionar a tecla Enter estando no campo Password
- **Dados de entrada:** Username: "tester", Password: "senha123"
- **Resultado esperado:** Login e submetido (mesmo efeito que clicar no botao "Log In"). Usuario e autenticado e navegado para a pagina inicial.
- **CA:** CA-02 - Enter submete login
- **Regra:** #2
- **Observacoes:** A tecla Enter funciona como atalho para submissao do formulario.

### CT-003 - Campo Username vazio — exibe erro e nao chama API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada.
- **Passos:**
  1. Navegar para a pagina de login
  2. Deixar o campo Username vazio
  3. Preencher campo Password com "senha123"
  4. Verificar que o botao "Log In" esta desabilitado
- **Dados de entrada:** Username: (vazio), Password: "senha123"
- **Resultado esperado:** Botao "Log In" permanece desabilitado. Nenhuma requisicao HTTP e enviada. Nenhum snackbar/toast de erro aparece (validacao client impede o submit).
- **CA:** CA-03 - Username vazio
- **Regra:** #1, #5
- **Observacoes:** A validacao client-side impede a chamada a API antes que ela ocorra.

### CT-004 - Campo Password vazio — exibe erro e nao chama API
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher campo Username com "tester"
  3. Deixar o campo Password vazio
  4. Verificar que o botao "Log In" esta desabilitado
- **Dados de entrada:** Username: "tester", Password: (vazio)
- **Resultado esperado:** Botao "Log In" permanece desabilitado. Nenhuma requisicao HTTP e enviada.
- **CA:** CA-04 - Password vazio
- **Regra:** #1, #5
- **Observacoes:** Mesmo comportamento do CT-003 para o campo Password.

### CT-005 - Campos obrigatorios vazios — botao Log In desabilitado
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada.
- **Passos:**
  1. Navegar para a pagina de login
  2. Verificar que ambos os campos estao vazios
  3. Verificar que o botao "Log In" esta desabilitado
- **Dados de entrada:** Username: (vazio), Password: (vazio)
- **Resultado esperado:** Botao "Log In" exibe estado desabilitado (classe `v-btn--disabled` ou equivalente). Nao e possivel clicar nele.
- **CA:** CA-05 - Campos vazios — botao desabilitado
- **Regra:** #2
- **Observacoes:** O botao so fica habilitado quando ambos os campos estiverem preenchidos (`inputIsEmpty = false`).

### CT-006 - Credencial incorreta — exibe snackbar de erro
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. Usuario `tester` existente.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher campo Username com "tester"
  3. Preencher campo Password com "senhaErrada"
  4. Clicar no botao "Log In"
- **Dados de entrada:** Username: "tester", Password: "senhaErrada"
- **Resultado esperado:** A API retorna erro. Snackbar (desktop) ou toast (mobile) exibe mensagem de erro. O usuario permanece na pagina de login.
- **CA:** CA-06 - Credencial incorreta
- **Regra:** #5
- **Observacoes:** A mensagem de erro vem da API e e exibida via feedback visual.

### CT-007 - 2FA habilitado — campo Passcode aparece
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. Usuario com 2FA ativo.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher credenciais validas
  3. Clicar no botao "Log In"
  4. Verificar que o campo "Passcode" aparece
  5. Verificar que o campo "Passcode" recebe foco automatico
- **Dados de entrada:** Credenciais validas de usuario com 2FA
- **Resultado esperado:** Apos submeter credenciais, o campo "Passcode" (tipo numerico, `autocomplete=one-time-code`) aparece na tela e recebe foco/selecao. O icone de ajuda alterna para modo "Backup Code".
- **CA:** CA-07 - 2FA — campo Passcode
- **Regra:** #3, #6
- **Observacoes:** [SUPOSICAO] Assume-se que o campo Passcode aparece dinamicamente apos a resposta `need2FA: true` da API. Passcode vazio gera snackbar "Passcode cannot be blank".

### CT-008 - Email nao verificado — redireciona para /verify_email
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. `isUserVerifyEmailEnabled()` retorna true. Usuario com email nao verificado.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher credenciais validas do usuario com email nao verificado
  3. Clicar no botao "Log In"
- **Dados de entrada:** Credenciais validas de usuario com email nao verificado
- **Resultado esperado:** Apos submeter, o sistema redireciona para `/verify_email?email=...&emailSent=...` com o email e estado do token no query string.
- **CA:** CA-08 - Email nao verificado — redireciona
- **Regra:** #3
- **Observacoes:** O redirecionamento so ocorre quando a verificacao de email esta habilitada no servidor.

### CT-009 - Duplo clique no botao Log In bloqueado
- **Prioridade:** P2
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. Credenciais validas.
- **Passos:**
  1. Navegar para a pagina de login
  2. Preencher credenciais validas
  3. Clicar rapidamente duas vezes no botao "Log In"
- **Dados de entrada:** Credenciais validas
- **Resultado esperado:** Apenas uma requisicao de login e enviada. O estado de submissao (`loggingInByPassword`) bloqueia o duplo clique. Campos e botao ficam desabilitados apos o primeiro clique.
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** #4
- **Observacoes:** Estado de submissao previne multiplas requisicoes simultaneas.

### CT-010 - Links "Forget Password" e "Create an account"
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. `isUserRegistrationEnabled()` retorna true. Fluxo de recuperacao de senha habilitado.
- **Passos:**
  1. Navegar para a pagina de login
  2. Verificar a existencia do link "Forget Password?"
  3. Clicar no link "Forget Password?"
  4. Navegar de volta para a pagina de login
  5. Verificar a existencia do link "Create an account"
  6. Clicar no link "Create an account"
- **Dados de entrada:** Nenhum.
- **Resultado esperado:** "Forget Password?" navega para o fluxo de recuperacao de senha. "Create an account" navega para `/signup`.
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** N/A
- **Observacoes:** Links sao exibidos condicionalmente baseado em configuracoes do servidor. Botao OAuth2 (ex.: GitHub, OIDC) tambem e exibido quando habilitado.

---
