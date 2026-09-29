# Selarium Visual Identity

This document defines the visual direction for Selarium assets. It exists to keep future textures, GUI work, and render layers aligned with the same magical identity.

## Core Direction

Selarium is magical, mystic, ritualistic, and arcane fantasy.

Use:
- Arcane violet, purple, amethyst, and mana cyan as the main magical colors.
- Mystic silver, pale stone, ritual stone, muted copper, and small warm-gold accents as support colors.
- Clean shapes with a few meaningful runes or glow pixels.
- Crystal, mana, dust, sigil, and altar language.

Avoid:
- Industrial machinery.
- Generic dark technology boxes.
- Flat gray placeholder panels.
- Overloaded noise, random symbols, or heavy black metal everywhere.
- Assets that do not explain their craft, function, or place in the Selarium loop.

## Dusts

Dusts must read like magical powder, similar in silhouette logic to redstone or gunpowder.

Rules:
- Use the same compact powder mound silhouette for every dust.
- Use color, brightness, and a few small particles to distinguish types.
- Basic dusts are simple, readable piles.
- Refined dusts keep the same silhouette but gain stronger contrast, saturation, and subtle extra sparkles.
- Dusts should not look like gems, shards, blobs, ingots, or unrelated item families.

Type palettes:
- Arcane: violet and blue-purple.
- Aegis: mystic silver and pale gray.
- Vital: green and warm gold.
- Focus: red and magenta.
- Binding: amber and gold.
- Echo: blue and cyan.
- Density: deep violet and indigo.
- Warp: ender green and purple.
- Veil: black-violet and shadow purple.
- Chrono: white, pale gold, and warm light.

Legacy dust item IDs should keep using the matching basic dust visual.

## Arcane Crystal

The Arcane Crystal is the base raw material.

Rules:
- It should read like an amethyst-like magical crystal, but remain original.
- It should be compact and clear in inventory and in hand.
- Use violet, blue-purple, and small cyan highlights.
- Avoid wand, vial, staff, long artifact, or abstract shapes.

## Arcane Grinder

The Arcane Grinder is a mystical crystal refining station, not industrial tech.

Rules:
- Show ritual stone or mystic metal structure.
- Show a visible arcane crystal or refinement focus.
- Show a simple grinding/refining area.
- Use a small number of runic or mana accents.
- Keep the block readable from front, side, and top.
- The visual should make sense with metal, glass, crystal, and utility ingredients.

Avoid:
- Heavy dark tech-box styling.
- Too many symbols.
- Noise that hides the functional parts.

## Mana Tank

The Mana Tank is a mystical mana reservoir.

Rules:
- The static block texture should read as an empty framed vessel.
- Dynamic mana fill should be the only part that makes the tank look full.
- Use glass/cyan highlights for the reservoir.
- Use mystic stone or metal for the frame.
- Low mana must look low; full mana must look full.

Avoid:
- Baking a full purple tank into the base texture.
- Making the frame too dark or too noisy.

## Arcane Sigil

The Arcane Sigil is drawn on the ground with powder and magical energy.

Rules:
- No filled square background.
- No translucent colored plate.
- Use line-only circles, runes, and geometric patterns with real alpha transparency.
- Base texture is a clean ritual circle.
- Ward overlay is the main visual identity when a ward resolves.
- Active overlay is a subtle glow, not a second full design.
- Do not render every component at full strength if it makes the sigil unreadable.

Prefer simple and beautiful over complex and noisy.

## Arcane Grinder GUI

The Grinder GUI should be functional first and mystical second.

Rules:
- Keep the 176x166 vanilla-style container layout.
- Keep real slots aligned with drawn slots.
- Inventory and hotbar must remain readable and familiar.
- Use subtle arcane framing, violet accents, and a clean stone panel.
- Decoration should never look like fake slots or cover usable slots.

## Future Asset Checks

Before adding or replacing assets, check:
- Does this asset look magical, ritualistic, or crystal/mana related?
- Does it belong to the same family as the other Selarium assets?
- Does it explain the item or block function at a glance?
- Does it remain readable at Minecraft inventory scale?
- Does it avoid returning to generic dark machinery?
