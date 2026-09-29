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

The Arcane Sigil is drawn on the ground with powder and magical energy. It has no block model: `ArcaneSigilRenderer`
builds it every frame from white, tintable textures so one set of assets serves all 32 wards.

Layers, bottom to top:
1. Chalk circle (`vfx/sigil/base_circle`), always visible.
2. One small mark per dust type in the sigil (`vfx/sigil/component/<dust>`), tinted with the dust colour.
3. The ward glyph (`vfx/sigil/glyph/<ward>`), tinted with the ward colour. Each glyph is a distinct vector rune.
4. While active: slowly counter-rotating rings, orbiting runes, a floating focus crystal, a faint light column and the
   translucent field shell that shows the real area.

Rules:
- No filled square background and no translucent plate; only line work with real alpha.
- Glow is additive and unlit (`SelariumRenderTypes.ADDITIVE`), so it reads at night and never washes out the ground.
- The inactive sigil is quiet; activation is the moment of spectacle (expanding ring, rune burst).
- Every animated part can be switched off in `selarium-client.toml` (`sigilAnimations`, `sigilFloatingRunes`,
  `sigilLightBeam`, `wardShells`, `vfxQuality`).

Prefer simple and beautiful over complex and noisy.

## Ward Colour Language

Every ward has a signature primary and secondary colour in `WardStyles`, and one of four shell styles:

- `SOFT`: flowing energy dome, the default for buffs and utilities.
- `HEX`: hexagonal force-field lattice, for defensive and hostile control wards (Banishment, Crushing, Stasis, Deflection...).
- `RUNES`: scrolling band of runes, for wards about information and subtle effects (Whispering, Silence, Transmutation).
- `NONE`: the ward is already visible as real blocks (Citadel, Tangible).

Colours are chosen so neighbours in a category stay distinguishable: blues and greens for support, warm golds and
greens for utilities, reds and magentas for hostile pressure, violets for structures, and gold, teal and orange for events. The same colour
tints the sigil glyph, the field shell, the ward's particles and the seal of its scroll.

## Particles and VFX Vocabulary

Four custom particles (`selarium:wisp`, `spark`, `rune`, `ring`) carry a colour and a scale, so the server can send one
particle type in any ward colour (`WardFx`, `GlowParticleOptions`).

- **Wisp**: soft orb that drifts upwards; the ambient life of a sigil and the shimmer on every creature a ward touches.
- **Spark**: small fast glint; bursts for events (a deflected projectile, a prevented death), dotted trails for drain,
  soul-chain and mana transfer, and the work of the Grinder and the Inscription Bench.
- **Rune**: a tiny floating glyph, used only in the burst when a sigil activates.
- **Ring**: a flat ring that expands along the ground; the visible beat of every ward cycle and of activation.

Use particles sparingly: one clear cue per event. Continuous effects live on the sigil renderer, not in particle floods.

## Texture Conventions

- Block and item textures are 32x32 pixel art with a consistent light direction (upper left), a one-pixel dark outline
  for items, and colour ramps from `tools/art/pal.py`.
- Crystals are faceted with a bright ridge; metal is a warm gilded trim; stone is desaturated violet-grey.
- Animated block textures (Citadel wall, Tangible barrier) use a `.png.mcmeta` with a vertical strip of frames.
- Textures are generated (`python3 tools/art/build_all.py`); change the generator, not the PNG.

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
