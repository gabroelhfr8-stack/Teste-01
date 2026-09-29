# Referência das proteções (Wards)

> Gerado automaticamente por `tools/gen_docs.py` a partir do código-fonte — não edite à mão.

Cada proteção é resolvida quando o sigilo contém **todas** as poeiras exigidas. Proteções *refinadas* exigem poeiras **refinadas** (exceto a Arcana). Os valores abaixo são os padrões da configuração `selarium-common.toml`; o servidor pode alterá-los.

Total: **32 proteções**. O alcance é um raio **esférico** em blocos, o mesmo volume desenhado pela casca translúcida do campo.

## Fonte de mana

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Mana Ambiente** | Arcana, 2× Foco | 1 | 6s | 0 (+25 ao ativar) | 300s | 90s |

- **Ward de Mana Ambiente** — Gera mana com limite por ativação.

## Detecção

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward Sussurrante** | Arcana, Eco, Foco | 12 | 2s | 5 | 300s | 30s |
| **Ward Espectral** | Arcana, Eco, Véu | 10 | 4s | 6 | 300s | 30s |

- **Ward Sussurrante** — Alerta o dono sobre intrusos.
- **Ward Espectral** — Revela intrusos com brilho.

## Benefícios para aliados

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Baluarte** | Arcana, Égide, Foco | 8 | 4s | 8 | 300s | 30s |
| **Ward de Rejuvenescimento** | Arcana, 2× Vital | 8 | 5s | 10 | 300s | 30s |
| **Ward de Leveza** | Arcana, Vital, Densidade | 8 | 2s | 6 | 300s | 30s |
| **Ward de Ancoragem** | Arcana, Foco, Densidade | 8 | 1.5s | 6 | 300s | 30s |
| **Ward de Camuflagem** | Arcana, Foco, Véu | 10 | 3s | 8 | 300s | 30s |
| **Ward de Respiração Aquática** | Arcana, Vital, Dobra | 12 | 4s | 8 | 300s | 30s |

- **Ward de Baluarte** — Concede resistência a aliados.
- **Ward de Rejuvenescimento** — Regenera aliados.
- **Ward de Leveza** — Reduz perigos de queda.
- **Ward de Ancoragem** — Remove levitação e estabiliza aliados.
- **Ward de Camuflagem** — Oculta o dono e desfaz alvos hostis.
- **Ward de Respiração Aquática** — Restaura ar e ajuda na água.

## Utilidades

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Magnetismo** | Arcana, Foco, Densidade, Vínculo | 10 | 1s | 5 | 300s | 30s |
| **Ward de Fertilidade** | Arcana, Vínculo, Vital | 10 | 5s | 12 | 300s | 30s |
| **Ward de Aceleração** | Arcana, Vital, Crono | 8 | 5s | 14 | 300s | 30s |
| **Ward de Eficiência** | Arcana, Foco, Crono | 8 | 4s | 12 | 300s | 30s |
| **Ward de Transmutação** | Arcana, Foco Refinado, Crono Refinado, Vital Refinada | 8 | 5s | 16 | 300s | 35s |
| **Ward de Fase** | Arcana, Véu Refinado, Dobra Refinada, Densidade Refinada | 8 | 2s | 18 | 300s | 45s |

- **Ward de Magnetismo** — Atrai itens soltos.
- **Ward de Fertilidade** — Estimula reprodução animal.
- **Ward de Aceleração** — Acelera plantas e filhotes.
- **Ward de Eficiência** — Acelera fornalhas e o Moedor Arcano.
- **Ward de Transmutação** — Converte itens específicos e oxida cobre.
- **Ward de Fase** — Permite atravessar blocos de phasing do Selarium.

## Efeitos contra invasores

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Banimento** | Arcana, Foco, Dobra | 9 | 3s | 18 | 300s | 45s |
| **Ward de Eclipse** | Arcana, Vínculo, Véu, Foco | 8 | 3s | 12 | 300s | 35s |
| **Ward de Esmagamento** | Arcana, Vínculo, Densidade | 9 | 2s | 18 | 300s | 35s |
| **Ward de Inversão** | Arcana, Foco Refinado, Densidade Refinada, Dobra Refinada | 9 | 4s | 24 | 300s | 40s |
| **Ward de Drenagem** | Arcana, Vital Refinada, Vínculo Refinado, Densidade Refinada | 9 | 2s | 25 | 300s | 40s |
| **Ward de Estase** | Arcana, Vínculo Refinado, Crono Refinado | 9 | 2s | 35 | 300s | 40s |
| **Ward de Maelstrom** | Arcana, Vínculo Refinado, Densidade Refinada, Vital Refinada, Dobra Refinada | 9 | 2s | 26 | 300s | 40s |
| **Ward de Decadência** | Arcana, Vínculo Refinado, Vital Refinada, Crono Refinado | 9 | 3s | 24 | 300s | 40s |
| **Ward de Silêncio** | Arcana, Foco Refinado, Vínculo Refinado, Eco Refinado | 8 | 4s | 20 | 300s | 40s |

- **Ward de Banimento** — Afasta intrusos do campo.
- **Ward de Eclipse** — Escurece a visão dos intrusos.
- **Ward de Esmagamento** — Pressiona e enfraquece intrusos.
- **Ward de Inversão** — Arremessa intrusos para cima.
- **Ward de Drenagem** — Drena intrusos, com geração limitada por ciclo.
- **Ward de Estase** — Retarda intrusos e projéteis.
- **Ward de Maelstrom** — Puxa e sufoca intrusos.
- **Ward de Decadência** — Aplica deterioração aos intrusos.
- **Ward de Silêncio** — Enfraquece e remove efeitos benéficos.

## Estruturas e proteção de área

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Cidadela** | Arcana, 2× Égide | 10 | 2s | 35 | 180s | 45s |
| **Ward Tangível** | Arcana, Foco Refinado, Égide Refinada, Vínculo Refinado | 5 | 2s | 50 | 120s | 45s |
| **Ward de Santuário** | Arcana, Foco Refinado, Égide Refinada, Vínculo Refinado, Dobra Refinada | 20 | 3s | 35 | 300s | 45s |

- **Ward de Cidadela** — Ergue muralha temporária com passagem para aliados.
- **Ward Tangível** — Forma barreira temporária com passagem para aliados.
- **Ward de Santuário** — Impede surgimento de hostis.

## Reações a eventos

| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |
|---|---|---:|---:|---:|---:|---:|
| **Ward de Disrupção** | Arcana, Vínculo, Dobra | 14 | 2s | 20 | 300s | 35s |
| **Ward de Recompensa** | Arcana, Foco Refinado, Vital Refinada, Crono Refinado, Vínculo Refinado | 12 | 3s | 28 | 300s | 40s |
| **Ward Imortal** | Arcana, Égide Refinada, Vital Refinada, Crono Refinado, Vínculo Refinado | 8 | 1s | 80 | 300s | 60s |
| **Ward de Elo de Alma** | Arcana, Vínculo Refinado, Vital Refinada, Eco Refinado | — | — | — | — | — |
| **Ward de Deflexão** | Arcana, Égide Refinada, Dobra Refinada, Foco Refinado | 10 | 1s | 25 | 300s | 35s |

- **Ward de Disrupção** — Interrompe teletransportes.
- **Ward de Recompensa** — Pode acrescentar um item a um saque elegível.
- **Ward Imortal** — Impede uma morte de aliado por 1.200 ticks, cobrando 200 mana extra.
- **Ward de Elo de Alma** — Propaga parte do dano entre intrusos.
- **Ward de Deflexão** — Desvia projéteis hostis.
