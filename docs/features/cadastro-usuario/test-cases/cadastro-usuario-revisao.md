# Relatorio de Revisao - Cadastro de Usuario

**Data da revisao:** 14/09/2026
**Revisor:** QA Agent
**Arquivos revisados:** cadastro-usuario-api.md, cadastro-usuario-ui.md

## Resumo da revisao
- **CTs originais (API):** 16
- **CTs originais (UI):** 8
- **CTs removidos (duplicados):** 0
- **CTs combinados:** 1
- **CTs finais (API):** 16
- **CTs finais (UI):** 7

## CTs removidos
Nenhum CT removido por duplicacao. Nao ha CTs com passos e resultado esperado identicos.

## CTs combinados
| IDs originais | Novo ID | Motivo |
|---------------|---------|--------|
| UI CT-007, UI CT-004 | UI CT-004 | Mesmo CA (CA-04) e mesma regra (#3): CT-004 testa botao desabilitado com campo vazio; CT-007 testa o mesmo comportamento com campo invalido (senha < 6). CT-004 passa a cobrir vazio E invalido. |

Nao combinados — decisao de manter separados:
- **API CT-010 (username) vs CT-011 (email):** mesma logica de binding invalido e mesmo CA (CA-07), porem regras de negocio distintas (#3 vs #4). Combinar perderia rastreabilidade de regra (Regra do skill: nao combinar quando perde rastreabilidade).
- **API CT-007 (campos ausentes) vs CT-009 (senha < 6):** CAs distintos (CA-07 vs CA-09) — senha minima tem criterio de aceite proprio.
- **API CT-016 (limites maximos) vs CT-010/011 (formato excedido):** CT-016 valida aceite no limite exato (borda positiva); CT-010/011 validam rejeicao de formato invalido. Complementares.

## Cobertura
- **CAs cobertos (API):** 9/9
- **CAs nao cobertos (API):** Nenhum
- **Regras cobertas (API):** 14/14
- **Regras nao cobertas (API):** Nenhuma

- **CAs cobertos (UI):** 6/6
- **CAs nao cobertos (UI):** Nenhum
- **Regras cobertas (UI):** 6/6
- **Regras nao cobertas (UI):** Nenhuma

## CTs sem rastreabilidade
| ID | Observacao |
|----|------------|
| UI CT-008 (antigo) | Cobre apenas Regra UI #6 (locale influencia defaults). NAo possui CA dedicado no doc — mantido como cobertura de regra de negocio. |

## Alteracoes de prioridade
Nenhuma. Prioridades coerentes com tipo de cenario:
- P0: funcionalidade principal (cadastro feliz, conflito de unicidade, validacoes criticas client/server)
- P1: comportamento importante com workaround ou dependente de configuracao
- P2: borda com impacto moderado / conveniencia (locale)

## Correcoes aplicadas
| Arquivo | Correcao |
|---------|----------|
| API CT-002 | Typo no endpoint: `tookens/list.json` -> `tokens/list.json` |
| API CT-002 | `recém-obtido` -> `recem-obtido` (pt-BR sem acento, convencao do repo) |
| API CT-010 | `usuário` -> `usuario` |
| UI (todos) | `Página` -> `Pagina` (convencao pt-BR sem acento) |

## Observacoes
1. **Cobertura completa:** 15/15 CAs (9 API + 6 UI) e 20/20 regras de negocio (14 API + 6 UI) cobertos.
2. **Nao automatizaveis (config do servidor):** API CT-006 (`enableUserRegister=false`), API CT-008 e UI CT-006 (`enableUserForceVerifyEmail=true`). Nao controlaveis via API no momento.
3. **Regra #13 (uid UUID):** verificada indiretamente no CT-001 (validacao de `result.user.uid` preenchido/nao-vazio).
4. **[SUPOSICAO] CT-007 UI (senha < 6):** apos combinacao, mantida a suposicao de que `inputIsInvalid` cobre minimo de 6 caracteres. Se nao bloquear client-side, o cenario vira cobertura exclusiva do CT-009 (API).
5. **Renumeracao UI:** apos combinacao de CT-007 em CT-004, o antigo CT-008 (troca de idioma) virou **UI CT-007**. IDs recalculados por prioridade (P0 -> P1 -> P2).