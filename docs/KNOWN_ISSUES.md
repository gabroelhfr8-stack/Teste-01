# Selarium Known Issues

This file tracks acceptable Alpha limitations and items to revisit after the first internal playtest.

## User Interface

- The Warding Grimoire and Selarium Codex screens are functional but text-heavy. They share the workshop panel
  style (`WorkshopUi`) with the Grinder, Inscription Bench and Sigil screens; none of them has animations yet.
- Ward glyphs exist as textures (`textures/vfx/sigil/glyph/`) but the Codex does not show them yet.

## Wards

- Every ward area is now a sphere of radius `range` (the same volume the client draws). Most wards used to query a
  cube of half-side `range`, so their reach in the corners is gone and the covered volume is about half
  (pi/6). Ranges were not re-tuned; compare against `docs/WARDS.md` when playtesting.
- Citadel and Tangible create temporary structures. Their walls animate, but there is no build-up or fade-out
  animation when they appear or expire.
- Tangible currently behaves as a practical barrier field; selective passage for allies is limited until a later
  permission-focused pass.
- Phasing is intentionally conservative and tied to Selarium-owned phasing behavior rather than global vanilla
  block collision changes.
- Efficiency focuses on safe functional block acceleration. Hopper acceleration remains a future TODO if a clean
  no-mixin path is chosen.
- Some event Wards depend on Forge events and may not catch every edge case from other mods.
- Ward balance is ready for Alpha, but exact upkeep values still need real survival playtime.

## Visuals and VFX

- All visual effects are cosmetic and client-side except the particles, which the server sends (as coloured
  `ParticleOptions`) so every player sees the same ward pulses. On servers with many wards, lower `vfxQuality` in
  `selarium-client.toml` or the server's particle traffic will still be there; a server-side toggle is a possible follow-up.
- Ward shells and sigil rings are drawn with additive, unlit render types. They read best against dark ground and
  at night; some resource packs or shader packs may need `wardShells = OFF`.
- There are no custom sounds for wards yet; activation, pulses and events reuse vanilla sound events.
- The CI smoke test renders with a software OpenGL driver at 854x480, so it verifies that rendering works but not
  frame rate or exact colour on real hardware.

## Progression

- Arcane Crystal has an Alpha fallback recipe using Amethyst Shard and Glowstone Dust. This keeps test worlds from
  being blocked by worldgen luck, but may be disabled or gated later.
- Geode rarity should be watched during survival testing; current defaults aim to be discoverable but not common.
- Ambient Mana is capped per activation and has an activation cost, but early mana pacing still needs playtest feedback.

## Technical

- Debug commands are disabled by default for Alpha. Enable `debugCommandsEnabled` only in local test configs.
- Existing Forge/Gradle deprecation warnings remain. They are not blocking Alpha, but should be cleaned up before a
  wider release.
- Cosmetic options (VFX quality, shells, mana HUD position) moved from `selarium-common.toml` to
  `selarium-client.toml` in 0.3.0; old values in the common file are ignored.
