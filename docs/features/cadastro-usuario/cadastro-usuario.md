# História: Cadastro de Usuário

## Descrição

Permite que um novo usuário crie sua conta no ezBookKeeping informando username, e-mail, nickname, senha, idioma, moeda padrão e primeiro dia da semana. Ao concluir com sucesso, o sistema cria o usuário e emite um token de sessão (ou, se exigir verificação de e-mail, apenas dispara o fluxo de confirmação).

O fluxo cobre validação de unicidade (username/e-mail), regras de formato, senha segura (PBKDF2 + salt) e a opção de exigir verificação de e-mail.

---

## API

### Endpoint

- **Método / Path:** `POST /api/register.json`
- **Content-Type:** `application/json`
- **Autenticação requerida:** Não

### Body (Request)

```json
{
  "username": "novo_user",          // Obrigatório, não-blanco, máx 32
  "email": "novo@example.com",      // Obrigatório, não-blanco, máx 100
  "nickname": "Novo Usuário",       // Obrigatório, não-blanco, máx 64
  "password": "senha123",           // Obrigatório, mín 6, máx 128
  "language": "en-US",              // Obrigatório, mín 2, máx 16
  "defaultCurrency": "BRL",         // Obrigatório, len 3, moeda ISO 4217
  "firstDayOfWeek": 1,              // 0–6 (opcional)
  "categories": []                  // Opcional (categorias iniciais em lote)
}
```

### Response — Sucesso (HTTP 200)

```json
{
  "result": {
    "token": "<JWT>",
    "need2FA": false,
    "user": { "uid": 1, "username": "novo_user", "email": "novo@example.com", "nickname": "Novo Usuário", "language": "en-US", "defaultCurrency": "BRL" },
    "applicationCloudSettings": {},
    "notificationContent": "",
    "needVerifyEmail": false,
    "presetCategoriesSaved": true
  },
  "success": true
}
```

`needVerifyEmail` fica `true` quando `enableUserForceVerifyEmail` está ativo — nesse caso **não** é retornado `token` de sessão.

---

## Regras de negócio (API)

1. **Unicidade de username:** obrigatório e **único**. Username já existente → `ErrUsernameAlreadyExists`.
2. **Unicidade de e-mail:** obrigatório e **único**. E-mail já existente → `ErrUserEmailAlreadyExists`.
3. **Formato username:** regex `^(?i)[a-z0-9_-]+$` (case-insensitive, apenas alfanuméricos, `_` e `-`), máx 32 caracteres.
4. **Formato e-mail:** RFC 5322 (case-insensitive), máx 100 caracteres.
5. **Formato nickname:** sem regex, apenas não-blanco e máx 64 caracteres.
6. **Senha:** `required, min=6, max=128`. Armazenada como hash **PBKDF2 (SHA-256, 10.000 iterações, saída 48 bytes)** com salt aleatório (10 caracteres) — nunca em texto claro.
7. **Moeda padrão:** obrigatória, exatamente 3 caracteres, deve ser moeda ISO 4217 válida → inválida gera `ErrUserDefaultCurrencyIsInvalid`.
8. **Primeiro dia da semana:** inteiro 0–6 (`WeekDay`).
9. **Idioma:** obrigatório, mín 2 e máx 16 caracteres (ex.: `en-US`, `pt-BR`).
10. **Registro desabilitado:** se `enableUserRegister = false` → `ErrUserRegistrationNotAllowed`.
11. **Verificação de e-mail:** se `enableUserForceVerifyEmail` → conta criada com e-mail não verificado, retorna `needVerifyEmail` e **sem token de sessão**.
12. **Trim:** username, e-mail e nickname são normalizados (trim) no cadastro.
13. **Identificador:** gera um `uid` (UUID) para o novo usuário.
14. **Categorias iniciais:** opcional — pode receber um lote de categorias para pré-popular a conta (`presetCategoriesSaved`).

---

## Critérios de aceite (API)

### Caminho feliz
- **Dado** uma requisição válida com username, e-mail, nickname, senha (≥6), idioma e moeda válida
- **Quando** envio `POST /api/register.json`
- **Então** recebo HTTP 200 com `success: true`, um `uid` criado e um `result.token` de sessão (com `needVerifyEmail: false`)

- **Dado** um e-mail e username ainda não usados
- **Quando** cadastro com `defaultCurrency = "BRL"` e senha `senha123`
- **Então** o token retornado autentica o usuário recém-criado nos endpoints `/api/v1/...`

### Casos negativos / borda
- **Dado** que já existe um usuário com o mesmo username
- **Quando** tento cadastrar com esse username
- **Então** recebo HTTP 400 com `errorCode: 201012`

- **Dado** que já existe um usuário com o mesmo e-mail
- **Quando** tento cadastrar com esse e-mail
- **Então** recebo HTTP 400 com `errorCode: 201013`

- **Dado** `defaultCurrency` com valor inválido (ex.: `"XYZ"`)
- **Quando** envio o cadastro
- **Então** recebo HTTP 400 com `errorCode: 201009`

- **Dado** `enableUserRegister = false`
- **Quando** tento cadastrar
- **Então** recebo HTTP 400 com `errorCode: 201014`

- **Dado** campo obrigatório ausente ou malformado (binding inválido, ex.: senha < 6, e-mail inválido, moeda com tamanho ≠ 3)
- **Quando** envio o cadastro
- **Então** recebo HTTP 400 com `success: false` e mensagem de erro de submissão incompleta/incorreta (descritivo — não há code numérico dedicado)

- **Dado** `enableUserForceVerifyEmail` ativo
- **Quando** concluo o cadastro com sucesso
- **Então** recebo `needVerifyEmail: true` e **nenhum** `token` de sessão é retornado

- **Dado** senha com valor sem caracteres mínimos (menos de 6)
- **Quando** envio o cadastro
- **Então** a requisição é rejeitada (binding inválido) — descritivo

---

## UI — Fluxo / comportamento

Fonte: `src/views/base/SignupPageBase.ts`, `src/views/desktop/SignupPage.vue`, `src/views/mobile/SignupPage.vue`

### Campos do formulário

| Campo | Tipo | Observação |
|---|---|---|
| **Username** | text | máx 32, padrão `[a-z0-9_-]` |
| **Password** | password | mín 6 |
| **Password Confirmation** | password | deve ser igual a Password |
| **Email** | email | máx 100 |
| **Nickname** | text | máx 64 |
| **Default Currency** | select | moeda ISO 4217 |

### Comportamento
- **Troca de idioma** no topo atualiza o `defaultCurrency` e o `firstDayOfWeek` padrão para o locale selecionado.
- **Botão de criar conta** fica **desabilitado** se `inputIsEmpty` (algum campo obrigatório vazio) ou `inputIsInvalid`.
- **Confirmação de senha:** se `password !== confirmPassword` → validação client bloqueia o submit.
- Durante o envio, os campos ficam desabilitados.
- **Erros** da API → snackbar (desktop) / toast (mobile) com a mensagem.
- **Sucesso** → `doAfterSignupSuccess(authResponse)`: aplica idioma/cor/moeda e redireciona (login implícito).
- Se o cadastro exige verificação de e-mail → redireciona para o fluxo `/verify_email`.

### Validação client (ordem de checagem)
1. Username em branco → "Username cannot be blank"
2. Password em branco → "Password cannot be blank"
3. Confirm password em branco → "Password confirmation cannot be blank"
4. Email em branco → "Email cannot be blank"
5. Nickname em branco → "Nickname cannot be blank"
6. Default currency em branco → "Default currency cannot be blank"
7. **Password ≠ confirm password** → "Password and password confirmation do not match"

---

## Regras de negócio (UI)

1. **Confirmação de senha** é exigida na UI (não existe no contrato da API) — deve bater com a senha antes do submit.
2. **Campos obrigatórios** validados client-side antes de qualquer chamada à API.
3. **Botão desabilitado** quando há campo obrigatório vazio ou validação inválida.
4. **Feedback visual** (snackbar/toast) para erros da API e validações.
5. **Login implícito** pós-cadastro: ao sucesso, o usuário é autenticado e redirecionado (a menos que exija verificação de e-mail).
6. **Locale influencia defaults:** trocar idioma ajusta moeda padrão e primeiro dia da semana.

---

## Critérios de aceite (UI)

### Caminho feliz
- **Dado** um novo usuário preenchendo todos os campos corretamente (senha = confirmação)
- **Quando** clico em criar conta
- **Então** a conta é criada, sou autenticado e redirecionado para a página inicial com idioma/moeda aplicados

### Casos negativos / borda
- **Dado** password diferente de password confirmation
- **Quando** tento criar a conta
- **Então** exibe a mensagem "Password and password confirmation do not match" e **não** submete à API

- **Dado** campo obrigatório em branco (username, password, confirmation, email, nickname ou currency)
- **Quando** tento criar a conta
- **Então** exibe a respectiva mensagem "...cannot be blank" e **não** chama a API

- **Dado** qualquer campo obrigatório vazio ou inválido
- **Então** o botão de criar conta permanece desabilitado

- **Dado** username ou e-mail já em uso
- **Quando** submeto o cadastro
- **Então** exibe snackbar/toast de erro de conflito e permaneço na página de cadastro

- **Dado** registro com exigência de verificação de e-mail habilitada
- **Quando** concluo o cadastro
- **Então** sou direcionado ao fluxo `/verify_email` antes de obter acesso completo

---

## Referência (código-fonte)

- `pkg/api/users.go` (handler de register)
- `pkg/models/user.go` (`UserRegisterRequest`, binding/comandos de validação)
- `pkg/models/auth_response.go` (`RegisterResponse`)
- `pkg/services/users.go` (criação de usuário, hash de senha)
- `pkg/errs/user.go` / `pkg/errs/global.go` / `pkg/errs/error.go` (códigos numéricos)
- `pkg/utils/validators.go` (regex username/e-mail)
- `src/views/base/SignupPageBase.ts`
- `src/views/desktop/SignupPage.vue`
- `src/views/mobile/SignupPage.vue`
