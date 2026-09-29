"""Tileable 32x32 materials and decals used by the multi-element machine models."""
from __future__ import annotations

import numpy as np

from . import pal, px
from .blocks import S, crystal_faces, outline_of, ramp_lookup


def ritual_stone(seed=61, dark=False):
    """Ashlar bricks with a violet undertone, a hairline of gold-lit mortar and rare glowing runes."""
    ramp = pal.STONE[0:6] if dark else pal.STONE[2:8]
    img = px.blank(S)
    img[..., 3] = 1
    rng = np.random.default_rng(seed)
    n = px.fbm(S, S, 6, seed, 3)
    rows = 4
    for r in range(rows):
        y0, y1 = r * 8, r * 8 + 8
        off = 0 if r % 2 == 0 else 8
        for bx in range(2):
            x0 = (bx * 16 + off) % S
            tone = 0.42 + rng.random() * 0.25
            cols = [(x0 + i) % S for i in range(16)]
            f = tone + (n[y0:y1][:, cols] - 0.5) * 0.5
            brick = ramp_lookup(f, ramp, 0.15, 0.85, dither=0.3)[..., :3]
            img[y0:y1, cols, :3] = brick
            # bevel: light top/left edge, shadow bottom/right
            img[y0, cols, :3] = ramp[-2]
            img[y1 - 1, cols, :3] = ramp[0]
            img[y0:y1, cols[0], :3] = ramp[-3]
            img[y0:y1, cols[-1], :3] = ramp[1]
    tint = px.hexrgb("#5a3f92")
    img[..., :3] = img[..., :3] * 0.9 + tint * 0.08
    if not dark:
        # two glowing runes
        for (x, y) in ((6, 10), (22, 26)):
            for dx, dy in ((0, 0), (0, 1), (0, 2), (1, 1), (2, 0), (2, 2)):
                img[y + dy, x + dx, :3] = pal.CYAN[6]
            img[y + 1, x - 1, :3] = pal.CYAN[3]
    return img


def arcane_metal(seed=62):
    """Silver-violet plates with rivets, brushed horizontally."""
    img = px.blank(S)
    img[..., 3] = 1
    grain = px.stretched_noise(S, S, 2, 20, seed)
    for (x0, y0) in ((0, 0), (16, 0), (0, 16), (16, 16)):
        f = 0.55 + (grain[y0:y0 + 16, x0:x0 + 16] - 0.5) * 0.35
        img[y0:y0 + 16, x0:x0 + 16, :3] = ramp_lookup(f, pal.SILVER[2:7], 0.2, 0.9, dither=0.2)[..., :3]
        img[y0, x0:x0 + 16, :3] = pal.SILVER[7]
        img[y0 + 15, x0:x0 + 16, :3] = pal.SILVER[1]
        img[y0:y0 + 16, x0, :3] = pal.SILVER[6]
        img[y0:y0 + 16, x0 + 15, :3] = pal.SILVER[2]
        for (rx, ry) in ((2, 2), (12, 2), (2, 12), (12, 12)):
            img[y0 + ry, x0 + rx, :3] = pal.SILVER[7]
            img[y0 + ry + 1, x0 + rx + 1, :3] = pal.SILVER[1]
    tint = px.hexrgb("#7548dc")
    img[..., :3] = img[..., :3] * 0.92 + tint * 0.06
    return img


def gilded_trim(seed=63):
    img = px.blank(S)
    img[..., 3] = 1
    grain = px.stretched_noise(S, S, 2, 16, seed)
    for r in range(4):
        y0 = r * 8
        f = 0.55 + (grain[y0:y0 + 8] - 0.5) * 0.3
        img[y0:y0 + 8, :, :3] = ramp_lookup(f, pal.GOLD[3:8], 0.2, 0.9, dither=0.2)[..., :3]
        img[y0, :, :3] = pal.GOLD[7]
        img[y0 + 7, :, :3] = pal.GOLD[2]
        img[y0 + 3, ::4, :3] = pal.GOLD[2]
    return img


def glass(seed=64):
    """Cyan-tinted translucent glass: faint body, bright edge lines and a diagonal sheen."""
    img = px.blank(S)
    img[..., :3] = pal.CYAN[5]
    img[..., 3] = 0.16
    yy, xx = np.mgrid[0:S, 0:S]
    diag = ((xx + yy) % 16 < 2) & (xx > 3) & (yy > 3)
    img[..., 3] = np.where(diag, 0.42, img[..., 3])
    img[..., :3] = np.where(diag[..., None], pal.CYAN[7], img[..., :3])
    edge = (xx < 1) | (yy < 1) | (xx > S - 2) | (yy > S - 2)
    img[..., 3] = np.where(edge, 0.7, img[..., 3])
    img[..., :3] = np.where(edge[..., None], pal.CYAN[6], img[..., :3])
    return img


def crystal_violet(seed=65):
    return crystal_faces(seed, pal.VIOLET, points=8, glint=0.015)


def crystal_cyan(seed=66):
    img = crystal_faces(seed, pal.CYAN, points=8, glint=0.02)
    return img


def moon_wood_dark(seed=67):
    img = px.blank(S)
    img[..., 3] = 1
    grain = px.stretched_noise(S, S, 2, 12, seed)
    for r in range(4):
        y0 = r * 8
        f = 0.45 + (grain[y0:y0 + 8] - 0.5) * 0.5
        img[y0:y0 + 8, :, :3] = ramp_lookup(f, pal.MOON[0:5], 0.1, 0.85, dither=0.25)[..., :3]
        img[y0, :, :3] = pal.MOON[4]
        img[y0 + 7, :, :3] = pal.MOON[0]
    return img


def leather_inlay(seed=68):
    """Deep violet leather with a gilded border - desk blotter."""
    img = px.blank(S)
    img[..., 3] = 1
    n = px.fbm(S, S, 8, seed, 2)
    img[..., :3] = ramp_lookup(n, pal.VIOLET[1:5], 0.2, 0.8, dither=0.3)[..., :3]
    img[0:2, :, :3] = pal.GOLD[4]
    img[-2:, :, :3] = pal.GOLD[3]
    img[:, 0:2, :3] = pal.GOLD[4]
    img[:, -2:, :3] = pal.GOLD[3]
    img[0, :, :3] = pal.GOLD[6]
    img[:, 0, :3] = pal.GOLD[6]
    return img


def parchment(seed=69, lines=True):
    img = px.blank(S)
    img[..., 3] = 1
    n = px.fbm(S, S, 6, seed, 3)
    img[..., :3] = ramp_lookup(n, pal.PEARL[3:7], 0.2, 0.85, dither=0.25)[..., :3]
    if lines:
        rng = np.random.default_rng(seed)
        for y in range(5, S - 3, 4):
            x = 4
            while x < S - 5:
                ln = int(rng.integers(2, 6))
                img[y, x:x + ln, :3] = pal.AMBER[2]
                x += ln + 2
    img[0, :, :3] = pal.PEARL[6]
    img[-1, :, :3] = pal.PEARL[1]
    return img


def rune_panel(seed=70):
    """Dark panel with a glowing cyan runic band (decal, not tileable vertically)."""
    img = px.blank(S)
    img[..., 3] = 1
    n = px.fbm(S, S, 6, seed, 2)
    img[..., :3] = ramp_lookup(n, pal.STONE[0:4], 0.2, 0.8, dither=0.3)[..., :3]
    img[0, :, :3] = pal.GOLD[4]
    img[-1, :, :3] = pal.GOLD[2]
    img[3, :, :3] = pal.CYAN[2]
    img[-4, :, :3] = pal.CYAN[2]
    rng = np.random.default_rng(seed)
    from .runes import RUNES
    x = 2
    i = 0
    while x < S - 5:
        rune = RUNES[(i * 5 + 3) % len(RUNES)]
        for stroke in rune:
            pts = [(int(round(x + px_ * 1.0)), int(round(8 + py_ * 4.0))) for px_, py_ in stroke]
            for (ax, ay), (bx, by) in zip(pts, pts[1:]):
                steps = max(abs(bx - ax), abs(by - ay), 1)
                for t in range(steps + 1):
                    xx = ax + (bx - ax) * t // steps
                    yy = ay + (by - ay) * t // steps
                    if 0 <= xx < S and 0 <= yy < S:
                        img[yy, xx, :3] = pal.CYAN[6]
        x += 5
        i += 1
    return img


def gauge(seed=71):
    """Vertical mana gauge: metal plate, tick marks, cyan window."""
    img = px.blank(S)
    img[..., 3] = 1
    img[..., :3] = pal.SILVER[3]
    img[0, :, :3] = pal.SILVER[7]
    img[-1, :, :3] = pal.SILVER[1]
    img[:, 0, :3] = pal.SILVER[6]
    img[:, -1, :3] = pal.SILVER[1]
    img[2:-2, 11:21, :3] = pal.CYAN[0]
    img[2:-2, 12:20, :3] = pal.CYAN[1]
    for k, y in enumerate(range(3, S - 3, 3)):
        w = 8 if k % 3 == 0 else 5
        img[y, 12:12 + w, :3] = pal.CYAN[6] if k % 3 == 0 else pal.CYAN[4]
    return img


def book_cover(seed=72, hue="violet"):
    ramp = pal.VIOLET if hue == "violet" else pal.INDIGO
    img = px.blank(S)
    img[..., 3] = 1
    n = px.fbm(S, S, 10, seed, 2)
    img[..., :3] = ramp_lookup(n, ramp[1:5], 0.2, 0.8, dither=0.3)[..., :3]
    img[0:2, :, :3] = pal.GOLD[4]
    img[-2:, :, :3] = pal.GOLD[3]
    img[:, 0:2, :3] = pal.GOLD[4]
    img[:, -2:, :3] = pal.GOLD[3]
    return img


def pages_side(seed=73):
    img = px.blank(S)
    img[..., 3] = 1
    img[..., :3] = pal.PEARL[5]
    for y in range(0, S, 2):
        img[y, :, :3] = pal.PEARL[4]
    img[:, 0, :3] = pal.PEARL[3]
    img[:, -1, :3] = pal.PEARL[3]
    return img


def ink(seed=74):
    img = px.blank(S)
    img[..., 3] = 1
    n = px.fbm(S, S, 4, seed, 2)
    img[..., :3] = ramp_lookup(n, pal.INDIGO[0:4], 0.2, 0.9, dither=0.3)[..., :3]
    img[3:5, 3:9, :3] = pal.CYAN[5]
    return img


def build_all(assets):
    out = assets / "textures" / "block"
    items = {
        "ritual_stone": ritual_stone(), "ritual_stone_dark": ritual_stone(seed=66, dark=True),
        "arcane_metal": arcane_metal(), "gilded_trim": gilded_trim(), "glass_cyan": glass(),
        "crystal_violet": crystal_violet(), "crystal_cyan": crystal_cyan(), "moon_wood_dark": moon_wood_dark(),
        "leather_inlay": leather_inlay(), "parchment": parchment(), "rune_panel": rune_panel(), "gauge": gauge(),
        "book_cover_violet": book_cover(hue="violet"), "book_cover_indigo": book_cover(seed=75, hue="indigo"),
        "pages_side": pages_side(), "ink": ink(),
    }
    for name, img in items.items():
        px.save(img, out / f"{name}.png")
    return items
