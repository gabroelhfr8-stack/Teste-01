# Selarium Alpha Test Checklist

Use this checklist for the first internal survival pass. Start with default configs unless a test explicitly asks for debug commands.

## Survival Progression

- [ ] Create a fresh survival world.
- [ ] Find an Arcane Crystal Geode in new Overworld chunks.
- [ ] Harvest Arcane Crystal Clusters and confirm Arcane Crystal drops.
- [ ] Craft an Arcane Grinder.
- [ ] Grind Arcane Crystal into Arcane Dust.
- [ ] Craft basic Dusts through Arcane Grinding recipes.
- [ ] Place Arcane Dust and create an Arcane Sigil.
- [ ] Add Dusts to resolve Ambient Mana.
- [ ] Activate Ambient Mana and confirm mana is generated into the sigil buffer.
- [ ] Craft and place a Mana Tank next to a sigil.
- [ ] Confirm active Wards can draw from sigil buffer, adjacent tank, then owner mana if enabled.
- [ ] Craft the Selarium Codex.
- [ ] Craft the Warding Grimoire.
- [ ] Register Monstrous, Bestial, and Anthropic soul knowledge.
- [ ] Unlock a Ward-specific rule by activating that Ward once.

## Core Recipes

- [ ] Arcane Grinder recipe is available and craftable.
- [ ] Mana Tank recipe is available and craftable.
- [ ] Selarium Codex recipe is available and craftable.
- [ ] Warding Grimoire recipe is available and craftable.
- [ ] Arcane Block crafts from 9 Arcane Crystals.
- [ ] Arcane Block unpacks into 9 Arcane Crystals.
- [ ] Alpha fallback Arcane Crystal recipe works if worldgen luck blocks early testing.
- [ ] No recipe references the removed legacy refined arcane dust item.

## Wards

- [ ] Test one buff Ward: Bulwark, Rejuvenation, Featherweight, Grounding, Aqualung, or Cloaking.
- [ ] Test one utility Ward: Magnetism, Fertility, Accelerating, Efficiency, Transmutation, or Phasing.
- [ ] Test one hostile Ward: Banishment, Eclipse, Crushing, Inversion, Drain, Stasis, Maelstrom, Decay, or Silence.
- [ ] Test one structural Ward: Citadel, Tangible, or Sanctuary.
- [ ] Test one event Ward: Bounty, Immortal, Soul-Chain, Deflection, or Disruption.
- [ ] Confirm each tested Ward pays upkeep continuously.
- [ ] Confirm each tested Ward stops applying effects when upkeep cannot be paid.
- [ ] Confirm harmful Wards do not affect players by default.
- [ ] Confirm bosses are not affected by default.
- [ ] Confirm Warding Grimoire allow and deny rules are respected.

## Warding Grimoire

- [ ] First use binds the Grimoire owner.
- [ ] Non-owner cannot edit when owner lock is enabled.
- [ ] Shift-right-click a hostile mob to record Monstrous Soul.
- [ ] Shift-right-click a passive animal or pet to record Bestial Soul.
- [ ] Shift-right-click a villager or player to record Anthropic Soul.
- [ ] Armor stands, items, projectiles, and unreadable entities do not unlock soul knowledge.
- [ ] Global category toggles persist after closing and reopening.
- [ ] Ward-specific toggles persist after closing and reopening.
- [ ] Player allow and deny lists persist.
- [ ] Entity type allow and deny lists persist.

## Visuals and VFX

Do this pass on a real GPU; CI only proves that rendering does not crash.

- [ ] An unused sigil shows a hand-drawn chalk decagon; each dust type adds its own mark.
- [ ] An active sigil shows its ward glyph, rotating rings, orbiting runes, the floating focus crystal and the light column.
- [ ] The crystal dome encloses the real area (a sphere of radius `range`; the dome sits just inside it), is faceted rather than round and is tinted per ward category.
- [ ] Nothing looks perfectly round: rings and outlines are polygons, particles are kites and stars, geodes are lumpy and faceted.
- [ ] Turning a ward on plays the expanding ring and rune burst; turning it off fades out with wisps.
- [ ] Each ward cycle pulses; affected creatures shimmer; event wards (Immortal, Deflection, Disruption) burst.
- [ ] `selarium-client.toml`: `vfxQuality` (OFF/LOW/MEDIUM/HIGH), `wardShells` (OFF/NEAR/ALWAYS) and `shellOpacity` behave.
- [ ] Mana Tank fluid rises and falls with the stored mana and animates; the HUD gauge matches the player's mana.
- [ ] The Arcane Grinder lights up (glowing runes, light level 9, sparks) only while it is grinding.
- [ ] Citadel walls (seams glow in the dark) and the Tangible barrier (translucent force field) are visible and animated.
- [ ] Crystal buds grow through four visibly different stages.
- [ ] Scroll seals take the ward's colour; scroll tooltips show the ward, its category and the creator's name.
- [ ] Inventory, hand and ground models of dusts, crystals, books and scrolls look right (no purple/black checkers).

## Dedicated Server

- [ ] Run `./gradlew runServer` (or `runGameTestServer` for the automated checks).
- [ ] Confirm there is no client-only class crash.
- [ ] Confirm the run reaches the normal EULA stop or server startup.
- [ ] If debug commands are needed, enable `debugCommandsEnabled` locally in config for that test world.
