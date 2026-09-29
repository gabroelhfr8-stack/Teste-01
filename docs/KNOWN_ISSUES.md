# Selarium Known Issues

This file tracks acceptable Alpha limitations and items to revisit after the first internal playtest.

## User Interface

- The Warding Grimoire GUI is functional but still visually MVP.
- The Selarium Codex GUI is functional but simple and text-heavy.
- Neither GUI has final visual polish, animations, or a strong magic-book presentation yet.

## Wards

- Citadel and Tangible create temporary structures, but their entry and exit animations are still basic.
- Tangible currently behaves as a practical barrier field; selective passage for allies is limited until a later permission-focused pass.
- Phasing is intentionally conservative and tied to Selarium-owned phasing behavior rather than global vanilla block collision changes.
- Efficiency focuses on safe functional block acceleration. Hopper acceleration remains a future TODO if a clean no-mixin path is chosen.
- Some event Wards depend on Forge events and may not catch every edge case from other mods.
- Ward balance is ready for Alpha, but exact upkeep values still need real survival playtime.

## Progression

- Arcane Crystal has an Alpha fallback recipe using Amethyst Shard and Glowstone Dust. This keeps test worlds from being blocked by worldgen luck, but may be disabled or gated later.
- Geode rarity should be watched during survival testing; current defaults aim to be discoverable but not common.
- Ambient Mana is capped per activation and has an activation cost, but early mana pacing still needs playtest feedback.

## Technical

- Debug commands are disabled by default for Alpha. Enable `debugCommandsEnabled` only in local test configs.
- Existing Forge/Gradle deprecation warnings remain. They are not blocking Alpha, but should be cleaned up before a wider release.
- The dedicated server validation may stop at the normal Minecraft EULA message in fresh run directories.

