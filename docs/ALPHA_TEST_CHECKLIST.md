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

## Dedicated Server

- [ ] Run `.\gradlew.bat runServer --no-daemon`.
- [ ] Confirm there is no client-only class crash.
- [ ] Confirm the run reaches the normal EULA stop or server startup.
- [ ] If debug commands are needed, enable `debugCommandsEnabled` locally in config for that test world.
