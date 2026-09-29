"""Item sprites: arcane crystal, dust mounds and the sigil icon (32x32)."""
from __future__ import annotations

import math

import numpy as np

from . import pal, px
from .blocks import outline_of, ramp_lookup

S = 32


def crystal_item() -> np.ndarray:
    """Faceted diagonal shard: light left facet, dark right facet, bright ridge and tip highlights."""
    img = px.blank(S)
    ax, ay, bx, by = 5.5, 26.5, 26.5, 5.5
    d = np.array([bx - ax, by - ay], dtype=np.float32)
    length = float(np.hypot(*d))
    d /= length
    n = np.array([-d[1], d[0]], dtype=np.float32)
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    px_, py_ = xx + 0.5 - ax, yy + 0.5 - ay
    t = (px_ * d[0] + py_ * d[1]) / length
    s = px_ * n[0] + py_ * n[1]
    half = 4.6 * np.clip(np.minimum(t / 0.22, (1.0 - t) / 0.30), 0, 1)
    body = (t > 0) & (t < 1) & (np.abs(s) <= half)
    ramp = pal.VIOLET
    left = s < -0.6
    right = s > 0.6
    idx = np.full((S, S), 4, dtype=np.int32)
    idx = np.where(left, 5, idx)
    idx = np.where(right, 3, idx)
    idx = np.where(np.abs(s) <= 0.6, 7, idx)                       # ridge
    idx = np.where((t > 0.84) & body, np.minimum(idx + 1, 8), idx)   # bright tip
    idx = np.where((t < 0.14) & body, np.maximum(idx - 1, 2), idx)   # shadowed foot
    img[..., :3] = ramp[np.clip(idx, 0, len(ramp) - 1)]
    img[..., 3] = body.astype(np.float32)
    # inner sheen and cyan core glow
    sheen = body & (np.abs(s + 2.2) < 0.55) & (t > 0.25) & (t < 0.7)
    img[..., :3] = np.where(sheen[..., None], pal.VIOLET[8], img[..., :3])
    core = body & (right) & (np.abs(s - 2.0) < 0.6) & (t > 0.3) & (t < 0.62)
    img[..., :3] = np.where(core[..., None], pal.CYAN[5], img[..., :3])
    out = outline_of(body)
    img[..., :3] = np.where(out[..., None], pal.VIOLET[0], img[..., :3])
    img[..., 3] = np.where(out, 1.0, img[..., 3])
    return img


def mound_mask() -> np.ndarray:
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    base = 27.0

    def heap(cx, w, h, p=1.55):
        u = np.clip(np.abs(xx - cx) / w, 0, 1)
        top = base - h * (1 - u ** p) ** 0.8
        return (yy >= top) & (yy <= base) & (np.abs(xx - cx) <= w)

    m = heap(14.0, 11.0, 13.0) | heap(22.5, 7.0, 8.0, 1.8) | heap(8.5, 5.5, 6.0, 1.8)
    # flatten the very bottom row into a soft base
    return m


def dust_item(kind: str, refined: bool) -> np.ndarray:
    ramp = pal.DUST_RAMPS[kind]
    mask = mound_mask()
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    # height-field lighting from the top-left
    top = np.where(mask, yy, 1e3)
    col_top = np.min(np.where(mask, yy, 1e3), axis=0)
    depth = np.where(mask, yy - col_top[None, :], 0.0)
    grain = px.fbm(S, S, 9, 91 + len(kind), 2, tile=False)
    light = 0.78 - depth * 0.048 - (xx - 10.0) * 0.006 + (grain - 0.5) * 0.36
    if refined:
        light += 0.14
    hi, lo = px.edge_light(mask.astype(np.float32))
    light = np.where(hi, light + 0.16, light)
    light = np.where(lo, light - 0.14, light)
    lo_i, hi_i = (1, len(ramp) - 2)
    body = ramp_lookup(light, ramp[lo_i:hi_i + 1], 0.25, 0.95, dither=0.55)
    img = px.blank(S)
    img[..., :3] = body[..., :3]
    img[..., 3] = mask.astype(np.float32)
    rng = np.random.default_rng(hash(kind) & 0xFFFF)
    # type-specific accent flecks (see docs/VISUAL_IDENTITY.md)
    accent = {"vital": pal.GOLD[6], "warp": pal.VIOLET[6], "veil": pal.VIOLET[5], "density": pal.VIOLET[6],
              "arcane": pal.CYAN[6], "echo": pal.CYAN[7], "aegis": pal.SILVER[7], "focus": pal.ROSE[6],
              "binding": pal.GOLD[7], "chrono": pal.SILVER[7]}[kind]
    ys, xs = np.where(mask)
    order = rng.permutation(len(ys))
    for i in order[: 9 if refined else 5]:
        img[ys[i], xs[i], :3] = accent
    if refined:
        # sparkles: plus-shaped glints and a soft halo hugging the silhouette
        for cx, cy in ((9, 16), (21, 12), (16, 21)):
            if 0 <= cy < S and img[cy, cx, 3] > 0:
                img[cy, cx, :3] = pal.SILVER[8 if False else 7]
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    if img[cy + dy, cx + dx, 3] > 0:
                        img[cy + dy, cx + dx, :3] = np.minimum(ramp[-1] * 1.05, 1)
        halo = outline_of(mask) 
        img[..., :3] = np.where(halo[..., None], ramp[-2], img[..., :3])
        img[..., 3] = np.where(halo, 0.55, img[..., 3])
    else:
        out = outline_of(mask)
        img[..., :3] = np.where(out[..., None], ramp[0], img[..., :3])
        img[..., 3] = np.where(out, 1.0, img[..., 3])
    if refined:
        out2 = outline_of(mask | outline_of(mask))
        out2 &= ~(mask | outline_of(mask))
        img[..., :3] = np.where(out2[..., None], ramp[0], img[..., :3])
        img[..., 3] = np.where(out2, 0.9, img[..., 3])
    return img


def sigil_item() -> np.ndarray:
    """Inventory icon for the Arcane Sigil: bold pixel-art chalk circle with a glowing core."""
    from .vec import Vec
    backing = Vec(S, 12)
    backing.disc(0, 0, 0.99)
    ring = Vec(S, 12)
    ring.circle(0, 0, 0.84, 0.15)
    ring.circle(0, 0, 0.48, 0.10)
    for a in range(0, 360, 45):
        c, s_ = math.cos(math.radians(a)), math.sin(math.radians(a))
        ring.line((0.48 * c, 0.48 * s_), (0.84 * c, 0.84 * s_), 0.08)
    star = Vec(S, 12)
    star.polygon([(0, -0.36), (0.1, -0.1), (0.36, 0), (0.1, 0.1), (0, 0.36), (-0.1, 0.1), (-0.36, 0), (-0.1, -0.1)])
    img = px.blank(S)
    px.paint(img, (backing.mask() > 0.5).astype(np.float32), px.hexrgb("#1b1230"), 0.85)
    px.paint(img, (ring.mask() > 0.42).astype(np.float32), px.hexrgb("#d8d0f5"))
    px.paint(img, (star.mask() > 0.42).astype(np.float32), px.hexrgb("#7ee6f2"))
    return img


def build_all(assets):
    out = assets / "textures" / "item"
    px.save(crystal_item(), out / "arcane_crystal.png")
    for kind in pal.DUST_RAMPS:
        px.save(dust_item(kind, False), out / f"basic_{kind}_dust.png")
        if kind != "arcane":
            px.save(dust_item(kind, True), out / f"refined_{kind}_dust.png")
    px.save(sigil_item(), out / "arcane_sigil.png")
