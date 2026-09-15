# Casos de Teste UI - Cadastro de Usuario

**Data de geracao:** 14/09/2026
**Autor:** QA Agent
**Fonte:** docs/features/cadastro-usuario/cadastro-usuario.md

## Resumo Executivo
- **Total de casos de teste:** 6
- **Executados:** 5/6 (83%) — CT-004 **adiado** (regra mobile-only, sem automacao mobile prevista)
- **Distribuicao por prioridade:**
  - P0: 3
  - P1: 2
  - P2: 1
  - P3: 0
- **Criterios de aceite cobertos:** CA-01 (Cadastro com sucesso + login implicito), CA-02 (Password != confirmation), CA-03 (Campo obrigatorio em branco), CA-04 (Botao desabilitado), CA-05 (Username/email em uso)
- **Regras de negocio cobertas:** 6/6 (UI)
- **Observacoes gerais:** CT-004 (botao desabilitado) e regra **mobile-only** — sem previsao de automacao mobile, esta **adiado** (no desktop os botoes "Proximo"/"Enviar" nunca desabilitam por campo vazio/invalido; validacao e por snackbar no clique). Senha < 6 **nao** e validada no client (so API). Demais CTs executaveis com headless Chrome via Page Object.

---

## Casos de Teste

| Status | CT | Titulo | Prioridade | Tipo | CA | Regra |
|--------|----|--------|------------|------|----|-------|
| [x]    | CT-001 | Cadastro com sucesso e login implicito | P0 | Positivo | CA-01 | #1, #5 |
| [x]    | CT-002 | Password diferente da confirmacao | P0 | Negativo | CA-02 | #1 |
| [x]    | CT-003 | Campos obrigatorios em branco | P0 | Negativo | CA-03 | #2 |
| [~]    | CT-004 | Botao criar conta desabilitado | P1 | Borda | CA-04 | #3 |
| [x]    | CT-005 | Username ou email ja em uso | P1 | Negativo | CA-05 | #4 |
| [x]    | CT-006 | Troca de idioma atualiza moeda e primeiro dia da semana | P2 | Positivo | N/A | #6 |

---

### CT-001 - Cadastro com sucesso e login implicito
- **Prioridade:** P0
- **Tipo:** Positivo
- **Camada:** UI
- **Pre-condicoes:** Navegador headless (DriverFactory.createChrome()). Username e e-mail nao utilizados. Pagina de cadastro acessivel.
- **Passos:**
  1. Navegar para a pagina de cadastro (rota de signup)
  2. Preencher Username com valor unico
  3. Preencher Password e Password Confirmation com a mesma senha
  4. Preencher Email e Nickname
  5. Selecionar Default Currency (ex.: BRL)
  6. Clicar no botao criar conta
  7. Aguardar redirecionamento para a pagina inicial
- **Dados de entrada:** username/email unicos; password = confirmacao = "senha123"; currency "BRL"
- **Resultado esperado:** Conta criada; usuario autenticado e redirecionado para a pagina inicial; idioma/moeda aplicados (login implicito — Regra UI #5)
- **CA:** CA-01 - Cadastro feliz (conta criada, autenticado, redirecionado com idioma/moeda)
- **Regra:** #1, #5
- **Observacoes:** Usar dados unicos por execucao. Verificar estado autenticado (sem tela de login aparecer apos submit).

### CT-002 - Password diferente da confirmacao
- **Prioridade:** P0
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Pagina de cadastro acessivel.
- **Passos:**
  1. Navegar para a pagina de cadastro
  2. Preencher todos os campos com valores validos
  3. Preencher Password com "senha123" e Password Confirmation com "senha124"
  4. Clicar em "Proximo" e aguardar a snackbar
  5. Confirmar que nenhum `POST /api/register.json` foi disparado (por construcao — o POST so dispara no "Enviar" da 2ª etapa, nunca acionado aqui)
- **Dados de entrada:** password "senha123", confirmacao "senha124"
- **Resultado esperado:** Mensagem "A senha e a confirmação da senha não coincidem" exibida (snackbar no clique em "Proximo"); avanço bloqueado; **nenhuma** chamada a API de cadastro
- **CA:** CA-02 - Password diferente da confirmacao
- **Regra:** #1
- **Observacoes:** Validacao client-side bloqueia o avanco antes de qualquer requisicao. No desktop o botao "Proximo" nao desabilita (validacao por snackbar) — a regra de desabilitar e mobile-only.

### CT-003 - Campos obrigatorios em branco
- **Prioridade:** P0
- **Tipo:** Negativo
- **Camada:** UI
- **Pre-condicoes:** Pagina de cadastro acessivel.
- **Passos:**
  1. Navegar para a pagina de cadastro
  2. Clicar em "Proximo" com o formulario vazio e validar a 1ª mensagem ("O nome de usuário não pode estar em branco")
  3. Preencher o campo indicado, clicar novamente em "Proximo" e validar o proximo campo vazio — repetir ate o ultimo campo
  4. Confirmar que o formulario so avanca (etapa de categorias) quando todos os campos estao preenchidos
- **Dados de entrada:** Formulario vazio, preenchido campo a campo na ordem da validacao
- **Resultado esperado:** Mensagem correspondente "...não pode estar em branco" para cada campo, na ordem (username → password → confirm → email → nickname); sem avanco de etapa enquanto houver campo vazio
- **CA:** CA-03 - Campo obrigatorio em branco
- **Regra:** #2
- **Observacoes:** Nenhuma chamada a API por construcao: `POST /api/register.json` so dispara no "Enviar" da 2ª etapa (nunca acionado). A mensagem de "Default currency" e indisponivel na UI (moeda pre-preenchida, veja cadastro-usuario.md).

### CT-004 - Botao criar conta desabilitado
- **Prioridade:** P1
- **Tipo:** Borda
- **Camada:** UI (mobile — `mobile#/signup`)
- **Status:** **Adiado** — regra mobile-only (`:class="'disabled': inputIsEmpty || submitting"` no link "Sign Up" da navbar do `MobileApp`). Sem previsao de automacao mobile neste projeto.
- **Pre-condicoes:** Pagina de cadastro mobile acessivel (UA mobile + rota `mobile#/signup`).
- **Passos:**
  1. Navegar para a pagina de cadastro mobile (formulario vazio)
  2. Verificar o estado do link "Sign Up" (navbar) — deve estar `disabled`
  3. Preencher campo por campo, verificando o estado do link a cada etapa
  4. Preencher todos os campos validos e verificar que o link fica habilitado
- **Dados de entrada:** Formulario vazio → gradualmente completo
- **Resultado esperado:** Link desabilitado enquanto houver campo obrigatorio vazio; habilitado somente quando todos os campos estao preenchidos e validos
- **CA:** CA-04 - Botao desabilitado com campo vazio/invalido
- **Regra:** #3
- **Observacoes:** **NAO aplicavel ao desktop**: "Proximo" (etapa 1) e "Enviar" (etapa 2) nunca desabilitam por campo vazio/invalido — a validacao desktop acontece por snackbar no clique (CT-003). Alem disso, "senha < 6" **nao** e validada no client (`SignupPageBase.ts` so cobre vazio e mismatch) — cai apenas no binding da API; logo tambem nao desabilita botoes. Se um dia o mobile entrar no escopo, implementar com `MobileSignUpPage` + ChromeOptions de UA/emulacao mobile.

### CT-005 - Username ou email ja em uso
- **Prioridade:** P1
- **Tipo:** Negativo
- **Camada:** UI
- **Status:** Executado — 2 testes em `SignUpUiTest`: `deveExibirConflitoQuandoUsernameJaExiste` (CT-005a) e `deveExibirConflitoQuandoEmailJaExiste` (CT-005b)
- **Pre-condicoes:** Pagina de cadastro acessivel. Usuario de apoio criado via API (`AuthClient.register` + `UserFaker.randomRegister()`) — o campo duplicado pertence a esse usuario pre-registrado.
- **Passos:**
  1. Criar o usuario de apoio via API (pre-seed)
  2. Navegar para a pagina de cadastro
  3. Preencher a etapa 1: (a) **username** do usuario de apoio + email novo/único; (b) **email** do usuario de apoio + username novo/único; demais campos validos
  4. Clicar em "Proximo" → avancar para a etapa 2 (categorias)
  5. Clicar em "Enviar" → dispara `POST /api/register.json`, que devolve **400 de conflito**
  6. Aguardar a snackbar com a mensagem de conflito
- **Dados de entrada:** (a) username ja registrado / email novo; (b) email ja registrado / username novo; senha = confirmacao = `UserFaker.password()`; moeda pre-preenchida (nao alterada); categorias predefinidas **nao** marcadas (`categories: []`)
- **Resultado esperado:** (a) snackbar **"O nome de usuário já existe"** (cod 201012); (b) snackbar **"O e-mail já existe"** (cod 201013); em ambos o usuario permanece na pagina (`#/signup`) e **nao** autentica
- **CA:** CA-05 - Username ou email ja em uso
- **Regra:** #4
- **Observacoes:** O conflito so e detectado no "Enviar" (2ª etapa), apos POST real — diferentemente do CT-003 (que so valida campos vazios no "Proximo", sem requisicao). O pre-seed via API revelou que `/api/register.json` exige tambem `nickname` e `categories` (default `[]`) — o DTO `RegisterRequest` reflete isso.

### CT-006 - Troca de idioma atualiza moeda e primeiro dia da semana
- **Prioridade:** P2
- **Tipo:** Positivo
- **Camada:** UI
- **Status:** Executado — `deveAlterarMoedaEPrimeiroDiaAoAlterarAMoeda` em `SignUpUiTest`
- **Pre-condicoes:** Pagina de cadastro acessivel (inicia em pt-BR por default).
- **Passos:**
  1. Navegar para a pagina de cadastro
  2. Verificar Default Currency e First Day of Week padroes do locale inicial (pt-BR: Real Brasileiro, Segunda-feira)
  3. Trocar o idioma no topo para "English"
  4. Verificar os novos valores de Default Currency e First Day of Week (USD, Sunday)
- **Dados de entrada:** Alteracao de locale no seletor de idioma
- **Resultado esperado:** Default Currency e First Day of Week atualizados automaticamente conforme o locale selecionado (via watcher do `SignupPageBase.ts` — so quando o valor atual ainda e o default do locale)
- **CA:** N/A (comportamento de conveniencia, sem CA dedicado no doc)
- **Regra:** #6
- **Observacoes:** Locators de moeda e primeiro dia sao **bilíngues** (pt-BR/English), pois os labels mudam com o idioma selecionado. Baseado em `SignupPageBase.ts`/`SignupPage.vue`. Sem CA explicito mapeado — cobre regra de negocio UI #6.

---