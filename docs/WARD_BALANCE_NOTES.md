# Selarium Ward Balance Notes

These notes summarize the current Alpha defaults. Exact values live in `SelariumCommonConfig`; this document is for playtest orientation.

## Global Rules

- Wards pay continuous upkeep while active.
- If upkeep cannot be paid, the Ward either becomes inert or deactivates depending on config.
- Harmful player targeting is disabled by default.
- Boss targeting is disabled by default.
- Entity and block hard caps are performance guards, not the main balance mechanism.
- Warding Grimoire rules can restrict targeting, but server configs still win.
- Every ward area is a sphere of radius `range` around the sigil (`WardArea`), the same volume the client draws.
  Before 0.3.0 most wards queried a cube of the same half-side, so effective coverage dropped to about half. If a
  ward feels too small in playtests, raise its `range` in the config rather than reintroducing cubes.

## Tier 1: Low Cost

- Ambient Mana: starter mana source, capped per activation, activation cost enabled.
- Whispering: detection utility, low cost.
- Spectral: reveals invaders with Glowing, moderate safety cap.
- Featherweight: owner-focused fall protection.
- Grounding: owner-focused anti-levitation and fall stabilization.
- Bulwark: simple defensive buff.
- Rejuvenation: simple healing/regeneration buff.

## Tier 2: Medium Cost

- Magnetism: item collection utility, range 10, field-like cap.
- Banishment: defensive repositioning, upkeep uses the legacy per-entity config key.
- Eclipse: blindness/darkness pressure, players disabled by default.
- Fertility: passive breeding support, capped by cooldowns.
- Cloaking: invisibility and target clearing support.
- Accelerating: plant and baby growth acceleration with block/entity caps.
- Efficiency: safe functional block acceleration.
- Aqualung: underwater sustain support.
- Transmutation: item/block transformation utility.

## Tier 3: High Cost

- Citadel: temporary wall field, high block cap, activation/upkeep should require support.
- Disruption: teleport denial field.
- Crushing: heavy pressure and movement suppression.
- Inversion: vertical launch control with cooldown.
- Tangible: temporary barrier shell.
- Sanctuary: spawn protection, high range and upkeep.
- Bounty: bonus drops, limited extra stacks and chance.
- Drain: combat drain with meaningful upkeep to avoid free mana loops.
- Soul-Chain: damage sharing, capped linked targets.
- Stasis: strong slowdown and projectile control.
- Maelstrom: drowning-like pressure and slow.
- Decay: Wither pressure.
- Deflection: projectile reflection field.
- Silence: anti-buff/weakening pressure.
- Phasing: conservative passage support.

## Tier 4: Very High Cost

- Immortal: continuous protection field with very high upkeep. This should require Mana Tank support in normal play.

## Playtest Watchlist

- Check whether Ambient Mana plus Mana Tank can bootstrap too quickly.
- Check whether Drain can become net-positive mana too easily in mob farms.
- Check whether Accelerating feels useful without replacing all early farming progression.
- Check whether Sanctuary range feels protective without trivializing hostile spawn pressure.
- Check whether Tangible and Citadel block placement caps are high enough for real structures but safe for servers.
- Check whether Deflection consumes upkeep fairly compared with its practical benefit.

