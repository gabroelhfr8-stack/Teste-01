"""Block textures (32x32). Everything is tileable unless noted."""
from __future__ import annotations

import numpy as np

from . import pal, px

S = 32


def shade_field(field, colors, steps=None, dither=0.0):
    steps = steps or len(colors)
    idx = px.dither_index(field, steps, dither)
    return px.apply_palette(np.clip(idx * (len(colors) - 1) // max(1, steps - 1), 0, len(colors) - 1), colors)


def ramp_lookup(field, colors, lo=0.0, hi=1.0, dither=0.0):
    t = np.clip((field - lo) / max(1e-6, hi - lo), 0, 1)
    idx = px.dither_index(t, len(colors), dither)
    return px.apply_palette(idx, colors)


def crystal_faces(seed: int, ramp, points=13, glint=0.03, glow_edges=True, size=S, tile=True):
    """Faceted crystalline surface: voronoi cells lit by a fake top-left light."""
    cid, d1, d2 = px.voronoi(size, size, points, seed, tile)
    rng = np.random.default_rng(seed + 5)
    cell_light = rng.random(points).astype(np.float32)
    base = cell_light[cid % points]
    # gradient inside each cell toward the lit corner gives faceted shading
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    grad = (1.0 - (xx + yy) / (2.0 * size)) * 0.25
    lit = np.clip(base * 0.62 + grad + 0.2, 0, 1)
    edge = np.clip(1.0 - (d2 - d1) / 1.6, 0, 1)
    img = ramp_lookup(lit, ramp[1:-1], dither=0.0)
    hi = ramp[-2]
    dark = ramp[1]
    m = (edge > 0.55)[..., None]
    img[..., :3] = np.where(m, ramp[-3], img[..., :3])
    # a few sparkles
    sparks = rng.random((size, size)) < glint
    img[..., :3] = np.where(sparks[..., None], ramp[-1], img[..., :3])
    return img


def geode_stone(seed=11):
    """Outer geode shell: dark violet-grey stone with crystalline flecks."""
    n = px.fbm(S, S, 4, seed, 4)
    n2 = px.fbm(S, S, 8, seed + 3, 3)
    field = 0.55 * n + 0.45 * n2
    img = ramp_lookup(field, pal.STONE[1:6], 0.15, 0.85, dither=0.35)
    # violet tint
    v = px.fbm(S, S, 3, seed + 9, 3)
    tint = px.hexrgb("#5a3f92")
    img[..., :3] = img[..., :3] * (0.78 + 0.22 * v[..., None]) + tint * 0.16 * v[..., None]
    rng = np.random.default_rng(seed)
    for _ in range(9):
        x, y = rng.integers(0, S, 2)
        col = pal.VIOLET[int(rng.integers(5, 8))]
        img[y, x, :3] = col
        if rng.random() < 0.5:
            img[(y + 1) % S, x, :3] = pal.VIOLET[4]
    return img


def polished_geode(seed=12):
    """Polished slabs with a bevel and a soft crystalline sheen."""
    img = px.blank(S)
    n = px.fbm(S, S, 3, seed, 3)
    tile = np.zeros((S, S), dtype=np.float32)
    field = 0.5 + (n - 0.5) * 0.5
    base = ramp_lookup(field, pal.STONE[2:7], 0.2, 0.8, dither=0.2)
    tint = px.hexrgb("#7548dc")
    base[..., :3] = base[..., :3] * 0.85 + tint * 0.12
    img = base
    for (x0, y0, x1, y1) in ((0, 0, 16, 16), (16, 0, 32, 16), (0, 16, 16, 32), (16, 16, 32, 32)):
        m = px.rect_mask(S, S, x0, y0, x1, y1)
        hi, lo = px.edge_light(m)
        img[..., :3] = np.where(hi[..., None], pal.STONE[6], img[..., :3])
        img[..., :3] = np.where(lo[..., None], pal.STONE[1], img[..., :3])
    for (x, y) in ((4, 4), (21, 5), (6, 22), (25, 25)):
        img[y, x, :3] = pal.VIOLET[7]
        img[y, x + 1, :3] = pal.VIOLET[5]
    return img


def crystal_block(seed=21):
    img = crystal_faces(seed, pal.VIOLET, points=14, glint=0.02)
    # cyan glints deep inside
    rng = np.random.default_rng(seed)
    for _ in range(6):
        x, y = rng.integers(0, S, 2)
        img[y, x, :3] = pal.CYAN[6]
        img[y, (x + 1) % S, :3] = pal.CYAN[4]
    return img


def budding_crystal(seed=22):
    img = crystal_faces(seed, pal.INDIGO, points=11, glint=0.0)
    img[..., :3] *= 0.8
    rng = np.random.default_rng(seed)
    for _ in range(10):
        x, y = int(rng.integers(1, S - 1)), int(rng.integers(1, S - 1))
        for dx, dy, c in ((0, 0, 7), (1, 0, 5), (-1, 0, 5), (0, 1, 4), (0, -1, 4)):
            img[(y + dy) % S, (x + dx) % S, :3] = pal.CYAN[c]
    return img


def arcane_block(seed=23):
    """Storage block: crystal plating inside a gilded frame with a central rune."""
    img = crystal_faces(seed, pal.VIOLET, points=12, glint=0.02, tile=False)
    frame = np.zeros((S, S), dtype=np.float32)
    frame[0:2, :] = frame[-2:, :] = 1
    frame[:, 0:2] = frame[:, -2:] = 1
    inner = np.zeros((S, S), dtype=np.float32)
    inner[2:3, 2:-2] = inner[-3:-2, 2:-2] = 1
    inner[2:-2, 2:3] = inner[2:-2, -3:-2] = 1
    img[..., :3] = np.where(frame[..., None] > 0, pal.GOLD[4], img[..., :3])
    img[0, :, :3] = pal.GOLD[6]
    img[:, 0, :3] = pal.GOLD[6]
    img[-1, :, :3] = pal.GOLD[2]
    img[:, -1, :3] = pal.GOLD[2]
    img[..., :3] = np.where(inner[..., None] > 0, pal.GOLD[2], img[..., :3])
    # central 4-point star
    cx = cy = 16
    star = [(0, -7), (1, -2), (7, 0), (1, 2), (0, 7), (-1, 2), (-7, 0), (-1, -2)]
    for dy in range(-7, 8):
        for dx in range(-7, 8):
            if abs(dx) + abs(dy) * 1.0 <= 7 and (abs(dx) <= 1 or abs(dy) <= 1 or abs(dx) + abs(dy) <= 3):
                d = abs(dx) + abs(dy)
                col = pal.CYAN[7] if d <= 2 else pal.CYAN[5] if d <= 5 else pal.CYAN[3]
                img[cy + dy, cx + dx, :3] = col
    return img


def moon_planks(seed=31):
    img = px.blank(S)
    img[..., 3] = 1
    grain = px.stretched_noise(S, S, 2, 14, seed)
    rows = 4
    for r in range(rows):
        y0, y1 = r * 8, r * 8 + 8
        rng = np.random.default_rng(seed + r)
        shift = int(rng.integers(0, S))
        tone = 0.42 + rng.random() * 0.22
        band = np.roll(grain, shift, axis=1)[y0:y1]
        f = np.clip(tone + (band - 0.5) * 0.55, 0, 1)
        img[y0:y1, :, :3] = ramp_lookup(f, pal.MOON[2:7], 0.15, 0.85, dither=0.25)[..., :3]
        img[y0, :, :3] = pal.MOON[6]
        img[y1 - 1, :, :3] = pal.MOON[1]
        # butt joint + nails
        jx = int(rng.integers(6, 26))
        img[y0 + 1:y1 - 1, jx, :3] = pal.MOON[2]
        for nx in (jx - 4, jx + 4):
            img[y0 + 3, nx % S, :3] = pal.MOON[7]
            img[y0 + 4, nx % S, :3] = pal.MOON[2]
    # faint cyan sap veins
    rng = np.random.default_rng(seed + 40)
    for _ in range(7):
        x, y = int(rng.integers(0, S)), int(rng.integers(0, S))
        if (y % 8) not in (0, 7):
            img[y, x, :3] = pal.CYAN[5]
    return img


def moon_log_side(seed=32):
    """Pale silver bark: soft vertical grain, thin dark grooves, luminous moss specks."""
    img = px.blank(S)
    img[..., 3] = 1
    n = px.stretched_noise(S, S, 6, 2, seed) * 0.6 + px.stretched_noise(S, S, 12, 5, seed + 1) * 0.4
    img[..., :3] = ramp_lookup(n, pal.MOON[3:8], 0.25, 0.8, dither=0.3)[..., :3]
    g = px.stretched_noise(S, S, 8, 2, seed + 2)
    groove = np.abs(g - 0.5) < 0.028
    img[..., :3] = np.where(groove[..., None], pal.MOON[1], img[..., :3])
    lit = np.zeros_like(groove)
    lit[:, :-1] = groove[:, 1:]
    img[..., :3] = np.where((lit & ~groove)[..., None], pal.MOON[7], img[..., :3])
    rng = np.random.default_rng(seed)
    for _ in range(8):   # birch-like horizontal lenticels
        x, y = int(rng.integers(0, S - 4)), int(rng.integers(1, S - 1))
        ln = int(rng.integers(2, 5))
        img[y, x:x + ln, :3] = pal.MOON[2]
    for _ in range(9):   # luminous moss specks
        x, y = int(rng.integers(0, S)), int(rng.integers(0, S))
        img[y, x, :3] = pal.CYAN[5]
        img[(y + 1) % S, x, :3] = pal.CYAN[3]
    return img


def moon_log_top(seed=33):
    img = px.blank(S)
    img[..., 3] = 1
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    d = np.hypot(xx - 15.5, yy - 15.5)
    wob = px.fbm(S, S, 3, seed, 3, tile=False)
    rings = np.sin((d + wob * 3.0) * 1.15) * 0.5 + 0.5
    field = 0.35 + rings * 0.45 - d / 60.0
    img[..., :3] = ramp_lookup(field, pal.MOON[2:8], 0.1, 0.85, dither=0.2)[..., :3]
    core = d < 2.6
    img[..., :3] = np.where(core[..., None], pal.CYAN[6], img[..., :3])
    img[..., :3] = np.where(((d >= 2.6) & (d < 3.6))[..., None], pal.CYAN[3], img[..., :3])
    bark = (np.maximum(np.abs(xx - 15.5), np.abs(yy - 15.5)) > 13.2)
    bark_n = px.fbm(S, S, 6, seed + 4, 2, tile=False)
    img[..., :3] = np.where(bark[..., None], ramp_lookup(bark_n, pal.MOON[1:5], dither=0.3)[..., :3], img[..., :3])
    edge = np.maximum(np.abs(xx - 15.5), np.abs(yy - 15.5)) > 14.5
    img[..., :3] = np.where(edge[..., None], pal.MOON[0], img[..., :3])
    return img


def leaves(seed, blossoms):
    """Cutout leaf tile; `blossoms` controls cyan/rose flower density."""
    n = px.fbm(S, S, 5, seed, 3)
    hole = px.fbm(S, S, 6, seed + 50, 2)
    img = px.blank(S)
    solid = hole > 0.30
    # remove isolated speckles so holes read as gaps not noise
    field = np.clip(n * 1.1, 0, 1)
    body = ramp_lookup(field, pal.INDIGO[2:8], 0.15, 0.9, dither=0.35)
    img[..., :3] = body[..., :3]
    img[..., 3] = solid.astype(np.float32)
    # dark under-shadow toward bottom-right of each mass
    shifted = np.zeros_like(solid)
    shifted[1:, 1:] = solid[:-1, :-1]
    shadow = solid & ~shifted
    img[..., :3] = np.where(shadow[..., None], pal.INDIGO[1], img[..., :3])
    rng = np.random.default_rng(seed)
    cols = [pal.CYAN[6], pal.CYAN[4], pal.ROSE[5], pal.VIOLET[6]]
    placed = 0
    tries = 0
    while placed < blossoms and tries < 400:
        tries += 1
        x, y = int(rng.integers(1, S - 1)), int(rng.integers(1, S - 1))
        if not solid[y, x]:
            continue
        c = cols[int(rng.integers(0, len(cols)))]
        img[y, x, :3] = c
        img[y, x, 3] = 1
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            xx_, yy_ = x + dx, y + dy
            if 0 <= xx_ < S and 0 <= yy_ < S and solid[yy_, xx_] and rng.random() < 0.6:
                img[yy_, xx_, :3] = c * 0.75
        img[y, x, :3] = np.minimum(c * 1.15, 1)
        placed += 1
    return img


def sapling(seed=41):
    img = px.blank(S)
    rng = np.random.default_rng(seed)
    # slim forked trunk
    for y in range(15, 31):
        img[y, 15:17, :3] = pal.MOON[5]
        img[y, 15:17, 3] = 1
        img[y, 14 if y % 4 else 17, :3] = pal.MOON[2]
        img[y, 14 if y % 4 else 17, 3] = 1
    for i in range(6):
        img[14 - i, 15 - i // 2 - 1, :3] = pal.MOON[4]
        img[14 - i, 15 - i // 2 - 1, 3] = 1
        img[13 - i, 17 + i // 2, :3] = pal.MOON[4]
        img[13 - i, 17 + i // 2, 3] = 1
    img[30:32, 12:20, :3] = pal.MOON[2]
    img[30:32, 12:20, 3] = 1
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    lobes = [(16, 7, 6.2), (9, 10, 4.6), (23, 10, 4.6), (12, 4, 4.0), (20, 4, 4.0), (16, 12, 4.8)]
    solid = np.zeros((S, S), dtype=bool)
    for k, (cx, cy, r) in enumerate(lobes):
        d = np.hypot(xx - cx, yy - cy) + px.fbm(S, S, 5, seed + k, 2, tile=False) * 2.0
        m = d < r
        col = ramp_lookup(np.clip(1.0 - d / r * 0.85 + 0.25 - (yy - cy) * 0.018, 0, 1), pal.INDIGO[2:8])[..., :3]
        img[..., :3] = np.where(m[..., None], col, img[..., :3])
        solid |= m
    img[..., 3] = np.where(solid, 1.0, img[..., 3])
    for (x, y) in ((13, 6), (19, 8), (9, 10), (22, 10), (16, 3), (17, 12), (14, 10), (11, 5), (21, 5)):
        if solid[y, x]:
            c = [pal.CYAN[6], pal.ROSE[5], pal.CYAN[4]][(x + y) % 3]
            img[y, x, :3] = c
            img[y, min(x + 1, S - 1), :3] = c * 0.72
    mask = img[..., 3] > 0.5
    out = outline_of(mask)
    img[..., :3] = np.where(out[..., None], pal.INDIGO[0], img[..., :3])
    img[..., 3] = np.where(out, 1.0, img[..., 3])
    return img


def outline_of(mask):
    m = mask.copy()
    o = np.zeros_like(m)
    o[1:, :] |= m[:-1, :]
    o[:-1, :] |= m[1:, :]
    o[:, 1:] |= m[:, :-1]
    o[:, :-1] |= m[:, 1:]
    return o & ~m


def petals(seed=42):
    img = px.blank(S)
    rng = np.random.default_rng(seed)
    cols = [(pal.CYAN[6], pal.CYAN[4]), (pal.VIOLET[6], pal.VIOLET[4]), (pal.ROSE[5], pal.ROSE[3])]
    spots = []
    for _ in range(26):
        x, y = int(rng.integers(2, S - 3)), int(rng.integers(2, S - 3))
        if all(abs(x - a) + abs(y - b) > 5 for a, b in spots):
            spots.append((x, y))
    for i, (x, y) in enumerate(spots):
        hi, lo = cols[i % 3]
        for dx, dy in ((0, 0), (1, 0), (0, 1), (-1, 0), (0, -1)):
            img[y + dy, x + dx, :3] = hi if (dx, dy) in ((0, 0), (0, -1), (-1, 0)) else lo
            img[y + dy, x + dx, 3] = 1
        img[y, x, :3] = pal.SILVER[7]
    return img


def build_all(assets):
    out = assets / "textures" / "block"
    tex = {
        "arcane_geode_stone": geode_stone(),
        "polished_arcane_geode_stone": polished_geode(),
        "arcane_crystal_block": crystal_block(),
        "budding_arcane_crystal": budding_crystal(),
        "arcane_block": arcane_block(),
        "arcane_planks": moon_planks(),
        "arcane_log": moon_log_side(),
        "arcane_log_top": moon_log_top(),
        "flowering_arcane_leaves_v1": leaves(51, 9),
        "flowering_arcane_leaves_v2": leaves(52, 14),
        "flowering_arcane_leaves_v3": leaves(53, 6),
        "arcane_sapling": sapling(),
        "arcane_petals": petals(),
    }
    for name, img in tex.items():
        px.save(img, out / f"{name}.png")
    return tex
