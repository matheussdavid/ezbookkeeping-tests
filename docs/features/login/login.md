# História: Login

## Descrição

Permite que um usuário existente acesse o ezBookKeeping informando suas credenciais (username **ou** e-mail + senha). Ao autenticar com sucesso, o sistema emite um token JWT de sessão que autoriza o acesso às demais funcionalidades (contas, transações, estatísticas etc.) sob o prefixo `/api/v1/`.

Além do caminho feliz, o fluxo cobre validação de credenciais, rate limiting, usuário desabilitado, e-mail não verificado e autenticação em duas etapas (2FA).

---

## API

### Endpoint

- **Método / Path:** `POST /api/authorize.json`
- **Content-Type:** `application/json`
- **Autenticação requerida:** Não (pré-autenticação)

### Body (Request)

```json
{
  "loginName": "tester",        // ou e-mail. Obrigatório, não-blanco, máx 100. Aceita username OU email
  "password": "senha123"        // Obrigatório, mín 6, máx 128
}
```

### Response — Sucesso (HTTP 200)

```json
{
  "result": {
    "token": "<JWT>",
    "need2FA": false,
    "user": { "uid": 1, "username": "tester", "email": "...", "nickname": "...", "language": "...", "defaultCurrency": "..." },
    "applicationCloudSettings": {},
    "notificationContent": ""
  },
  "success": true
}
```

Quando `need2FA` é `true`, o `token` retornado é **temporário** (exige confirmação de 2FA antes de conceder acesso completo).

---

## Regras de negócio (API)

1. **Identificador de login:** `loginName` aceita **username** ou **e-mail**. Validação: `required, notBlank, max=100, validUsername | validEmail`.
2. **Formato username:** regex `^(?i)[a-z0-9_-]+$` (case-insensitive, apenas alfanuméricos, `_` e `-`), máx 32 caracteres.
3. **Formato senha:** `required, min=6, max=128`.
4. **Credenciais inválidas:** usuário inexistente ou senha incorreta → login negado.
   - Se o usuário **existe** mas a **senha está errada** → `ErrUserPasswordWrong`.
5. **Usuário desabilitado:** `disabled = true` → login negado.
6. **E-mail não verificado:** se `enableUserForceVerifyEmail` estiver ativo e o perfil não tiver `emailVerified` → login negado (contexto inclui `email` e `hasValidEmailVerifyToken`).
7. **Rate limiting:** contagem de falhas por **IP** (`maxFailuresPerIpPerMinute`) e por **usuário** (`maxFailuresPerUserPerMinute`). Ao atingir o limite → `ErrFailureCountLimitReached`.
8. **Login por senha desabilitado:** se `enableInternalAuth = false` → `ErrCannotLoginByPassword`.
9. **Sucesso:** atualiza `last_login_unix_time` do usuário e retorna token JWT de sessão.
10. **Senha nunca transmitida em claro na resposta** — o hash reside apenas no servidor.

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** um usuário cadastrado com username `tester` e senha `senha123`
- **Quando** autentico com `{ "loginName": "tester", "password": "senha123" }`
- **Então** recebo HTTP 200 com `success: true` e um `result.token` não-vazio

- **Dado** o mesmo usuário com e-mail `tester@ezbookkeeping.test`
- **Quando** autentico com `loginName` = e-mail e senha correta
- **Então** recebo HTTP 200 e token de sessão (login via e-mail funciona)

### Casos negativos / borda
- **Dado** usuário existente e senha incorreta
- **Quando** autentico com a senha errada
- **Então** recebo HTTP 400 com `errorCode: 201011` e `success: false`

- **Dado** `loginName` ou `password` ausente/vazio (binding inválido)
- **Quando** envio body incompleto
- **Então** recebo HTTP 401 com `errorCode: 201001`

- **Dado** usuário com `disabled = true`
- **Quando** autentico com credenciais corretas desse usuário
- **Então** recebo HTTP 400 com `errorCode: 201016`

- **Dado** `enableUserForceVerifyEmail` ativo e usuário com e-mail não verificado
- **Quando** autentico com credenciais corretas
- **Então** recebo HTTP 400 com `errorCode: 201020`

- **Dado** `enableInternalAuth = false`
- **Quando** tento autenticar por senha
- **Então** recebo HTTP 400 com `errorCode: 201032`

- **Dado** muitas tentativas falhas dentro da janela configurada (IP ou usuário)
- **Quando** excedo o rate limit
- **Então** recebo HTTP 400 com `errorCode: 200018`

- **Dado** o token de sessão obtido no login
- **Quando** o uso no header `Authorization: Bearer <token>` em um endpoint autenticado (`/api/v1/...`)
- **Então** o acesso é autorizado (token válido concede acesso)

---

## UI — Fluxo / comportamento

Fonte: `src/views/desktop/LoginPage.vue`, `src/views/mobile/LoginPage.vue`, `src/views/base/LoginPageBase.ts`

### Campos do formulário

| Campo | Tipo | Autocomplete | Observação |
|---|---|---|---|
| **Username** | text | `username` | placeholder "Your username or email", `autocapitalize=none`, `inputmode=email` |
| **Password** | password | `current-password` | enter dispara login |

### Comportamento
- Ao abrir a página: campo **Username** recebe foco automático.
- **Botão "Log In"** fica **desabilitado** enquanto `username` ou `password` estiverem vazios (`inputIsEmpty`).
- Campos são desabilitados durante o envio (`loggingInByPassword`), durante login OAuth2 (`loggingInByOAuth2`) e na verificação 2FA.
- **Enter** no campo senha submete o login.
- Digitar no campo username/senha limpa qualquer `tempToken` pendente (reinicia fluxo 2FA).

### 2FA (UI)
- Se a API retorna `need2FA: true`, o campo **Passcode** (número, `autocomplete=one-time-code`) aparece e recebe foco/seleção.
- Ícone de ajuda alterna para modo **Backup Code** (e vice-versa).
- Passcode ou backup code vazio → snackbar "Passcode cannot be blank" / "Backup code cannot be blank".

### Erros e navegação
- **Erro de login** → snackbar (desktop) / toast (mobile) com a mensagem.
- **E-mail não verificado** e `isUserVerifyEmailEnabled()` → redireciona para `/verify_email?email=...&emailSent=...`.
- **Sucesso** → `doAfterLogin(authResponse)` aplica idioma/cores/currency e navega para `/` (desktop) / refresh page (mobile).

### Links e extras
- "Forget Password?" → fluxo de recuperação de senha (se habilitado).
- "Create an account" → navega para `/signup` (se `isUserRegistrationEnabled()`).
- Botão OAuth2 (ex.: GitHub, OIDC) exibido quando habilitado.
- Ao expirar o token ou por inatividade, o usuário retorna ao login.

---

## Regras de negócio (UI)

1. **Campos obrigatórios client-side:** username e senha não podem ser vazios antes de chamar a API.
2. **Submit bloqueado** enquanto qualquer campo obrigatório estiver vazio (botão desabilitado).
3. **Redirecionamento condicional:** e-mail não verificado leva a `/verify_email`; 2FA levam ao fluxo de passcode.
4. **Estado de submissão** impede duplo clique / múltiplos envios.
5. **Feedback visual** (snackbar/toast) em toda falha não processada automaticamente.
6. **Token temporário 2FA** é mantido em memória e descartado ao editar username/senha.

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** usuário válido na tela de login
- **Quando** preencho username e senha corretos e clico em "Log In"
- **Então** sou autenticado, navego para a página inicial e os dados (idioma, cor, moeda) são aplicados

- **Dado** botão "Log In" com username e senha preenchidos
- **Então** o botão está habilitado e, ao pressionar Enter no campo senha, o login é submetido

### Casos negativos / borda
- **Dado** campo Username vazio
- **Quando** tento logar
- **Então** exibe snackbar "Username cannot be blank" e **não** chama a API

- **Dado** campo Password vazio
- **Quando** tento logar
- **Então** exibe snackbar "Password cannot be blank" e **não** chama a API

- **Dado** username e senha vazios
- **Então** o botão "Log In" permanece desabilitado

- **Dado** credencial incorreta
- **Quando** submeto
- **Então** exibe snackbar/toast de erro e permaneço na página de login

- **Dado** usuário com 2FA ativo
- **Quando** informo credenciais corretas
- **Então** aparece o campo Passcode; ao validar o passcode, recebo acesso e navego para a página inicial

- **Dado** usuário com e-mail não verificado e verificação de e-mail habilitada
- **Quando** tento logar com credenciais corretas
- **Então** sou redirecionado para `/verify_email` com o e-mail e estado do token no query string

---

## Referência (código-fonte)

- `pkg/api/authorizations.go`
- `pkg/models/user.go` (`UserLoginRequest`)
- `pkg/models/auth_response.go` (`AuthResponse`)
- `pkg/services/users.go` (`GetUserByUsernameOrEmailAndPassword`, `IsPasswordEqualsUserPassword`)
- `pkg/errs/user.go` / `pkg/errs/global.go` / `pkg/errs/error.go` (códigos numéricos)
- `pkg/utils/validators.go` (regex username/e-mail)
- `src/views/desktop/LoginPage.vue`
- `src/views/mobile/LoginPage.vue`
- `src/views/base/LoginPageBase.ts`
