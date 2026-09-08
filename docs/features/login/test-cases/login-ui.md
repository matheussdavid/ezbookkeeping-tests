# Casos de Teste UI - Login

**Data de geracao:** 04/09/2026
**Data de atualizacao:** 08/09/2026
**Autor:** QA Agent
**Fonte:** docs/features/login/login.md

## Resumo Executivo
- **Total de casos de teste:** 10
- **Executados:** 6/10 (60%)
- **Distribuicao por prioridade:**
  - P0: 2
  - P1: 5
  - P2: 1
  - P3: 2
- **Criterios de aceite cobertos:** CA-01 (Login com credenciais validas), CA-02 (Enter submete login), CA-03 (Username vazio), CA-04 (Password vazio), CA-05 (Campos vazios — botao desabilitado), CA-06 (Credencial incorreta), CA-08 (Email nao verificado — redireciona)
- **Regras de negocio cobertas:** 6/6
- **Observacoes gerais:**
  - **2FA removido do escopo** — CA-07 nao sera testado (ex-`CT-007`).
  - **Email nao verificado** (CT-010, CA-08 — ultimo caso) nao testavel no momento — depende de `enableUserForceVerifyEmail` ativo e usuario com email nao verificado; configuracao do servidor nao controlavel no ambiente. Teste mantido no `LoginUiTest` como `@Disabled` com essa informacao.
  - **Links divididos** em CT-008 ("Forget Password?") e CT-009 ("Create an account") — antes um unico CT.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x] | CT-001 | Login com credenciais validas | P0 | Positivo | CA-01 | #1 |
| [x] | CT-002 | Enter no campo senha submete login | P0 | Positivo | CA-02 | #2 |
| [x] | CT-003 | Campo Username vazio — nao chama API | P1 | Negativo | CA-03 | #1, #5 |
| [x] | CT-004 | Campo Password vazio — nao chama API | P1 | Negativo | CA-04 | #1, #5 |
| [x] | CT-005 | Campos vazios — botao desabilitado | P1 | Borda | CA-05 | #2 |
| [x] | CT-006 | Credencial incorreta — snackbar erro | P1 | Negativo | CA-06 | #5 |
| [x] | CT-007 | Duplo clique bloqueado | P2 | Borda | CA-01 | #4 |
| [x] | CT-008 | Link Forget Password: navega para recuperacao | P3 | Borda | CA-01 | N/A |
| [x] | CT-009 | Link Create an account: navega para /signup | P3 | Borda | CA-01 | N/A |
| [x] | CT-010 | Email nao verificado — redireciona | P1 | Negativo | CA-08 | #3 |

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

### CT-007 - Duplo clique no botao Log In bloqueado
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

### CT-008 - Link "Forget Password?" — navega para o fluxo de recuperacao de senha
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. Fluxo de recuperacao de senha habilitado.
- **Passos:**
  1. Navegar para a pagina de login
  2. Verificar a existencia do link "Forget Password?"
  3. Clicar no link "Forget Password?"
- **Dados de entrada:** Nenhum.
- **Resultado esperado:** "Forget Password?" navega para o fluxo de recuperacao de senha (`/forgetpassword`).
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** N/A
- **Observacoes:** Detalhado a partir do ex-CT-010 (que combinava ambos os links). Link exibido condicionalmente baseado em configuracao do servidor.

### CT-009 - Link "Create an account" — navega para /signup
- **Prioridade:** P3
- **Tipo:** Borda
- **Camada:** UI
- **Pre-condicoes:** Tela de login carregada. `isUserRegistrationEnabled()` retorna true.
- **Passos:**
  1. Navegar para a pagina de login
  2. Verificar a existencia do link "Create an account"
  3. Clicar no link "Create an account"
- **Dados de entrada:** Nenhum.
- **Resultado esperado:** "Create an account" navega para `/signup`.
- **CA:** CA-01 - Login com credenciais validas
- **Regra:** N/A
- **Observacoes:** Detalhado a partir do ex-CT-010 (que combinava ambos os links). Link exibido condicionalmente baseado em configuracao do servidor.

### CT-010 - Email nao verificado — redireciona para /verify_email
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Status:** Nao testavel no momento — depende de `enableUserForceVerifyEmail` ativo + usuario com `emailVerified = false`. Configuracao do servidor nao controlavel no ambiente de teste. Teste mantido no `LoginUiTest` como `@Disabled` com essa informacao.
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

---