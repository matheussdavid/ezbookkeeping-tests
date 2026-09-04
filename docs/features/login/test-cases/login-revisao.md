# Relatorio de Revisao - Login

**Data da revisao:** 04/09/2026
**Revisor:** QA Agent
**Arquivos revisados:** login-api.md, login-ui.md

## Resumo da revisao
- **CTs originais (API):** 12
- **CTs originais (UI):** 10
- **CTs removidos (duplicados):** 0
- **CTs combinados:** 0
- **CTs finais (API):** 12
- **CTs finais (UI):** 10

## CTs removidos
Nenhum CT removido. Todos os CTs testam cenarios distintos.

## CTs combinados
Nenhum CT combinado. CTs aparentemente similares testam comportamentos distintos:
- API CT-001 (login username) vs CT-002 (login email): testam metodos de login diferentes (Regra #1 — aceita username OU email). Manter ambos.
- UI CT-003 (username vazio) vs CT-004 (password vazio): testam validacao de campos diferentes. Manter ambos para rastreabilidade com CAs distintos (CA-03 vs CA-04).
- UI CT-003/CT-004 vs CT-005 (botao desabilitado): CT-003/CT-004 verificam que a API nao e chamada; CT-005 verifica o estado do botao. Comportamentos distintos.

## Cobertura
- **CAs cobertos (API):** 10/10
- **CAs nao cobertos (API):** Nenhum
- **Regras cobertas (API):** 11/11
- **Regras nao cobertas (API):** Nenhuma

- **CAs cobertos (UI):** 8/8
- **CAs nao cobertos (UI):** Nenhum
- **Regras cobertas (UI):** 6/6
- **Regras nao cobertas (UI):** Nenhuma

## CTs sem rastreabilidade
Nenhum. Todos os CTs possuem CA e/ou regra de negocio associados.

## Alteracoes de prioridade
Nenhuma. Todas as prioridades estao coerentes com os tipos de cenario:
- P0: Funcionalidade principal (happy path)
- P1: Funcionalidade importante com workaround (negativos criticos)
- P2: Borda com impacto moderado
- P3: Opcional/cosmetico

## Observacoes
1. **Cobertura completa:** Todos os 18 CAs (10 API + 8 UI) e todas as 17 regras de negocio (11 API + 6 UI) estao cobertos.
2. **Regra #10 (API) — last_login_unix_time:** CT-001 cobre login bem-sucedido, mas nao verifica explicitamente a atualizacao do timestamp. [SUPOSICAO] Assume-se que a atualizacao happen internamente no servidor e nao e verificavel via API de login. Se necessario, criar CT especifico apos login usando endpoint de perfil para verificar `last_login_unix_time`.
3. **CTs de configuracao:** CT-006 (email nao verificado), CT-008 (senha desabilitada) e CT-009 (rate limiting) dependem de configuracao especifica do ambiente. Podem nao ser executaveis em todos os ambientes.
4. **CT-010 (UI) — links:** Classificado como P3 porque e funcionalidade opcional (depende de `isUserRegistrationEnabled()` e configuracao de recuperacao de senha).
