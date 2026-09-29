"""VFX sprites: particles, ward shell textures, beams, ring waves and the mana fluid.

All are white/tintable (except the fluid) so the client can colour them per ward.
"""
from __future__ import annotations

import math

import numpy as np

from . import pal, px
from .blocks import ramp_lookup
from .runes import rune_strokes
from .vec import Vec, octagon_norm


def _white(alpha: np.ndarray) -> np.ndarray:
    img = px.blank(alpha.shape[1], alpha.shape[0])
    img[..., :3] = 1.0
    img[..., 3] = np.clip(alpha, 0, 1)
    return img


def _grid(w, h):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    return (xx + 0.5) / w * 2 - 1, (yy + 0.5) / h * 2 - 1


def wisp(frame: int, size: int = 32) -> np.ndarray:
    """A tall kite of light with a thin vertical glint: the flame of a sigil, not a soft ball."""
    x, y = _grid(size, size)
    ax, ay = np.abs(x), np.abs(y)
    pulse = 0.88 + 0.12 * math.sin(frame * math.pi / 2)
    d = ax / 0.50 + ay / 0.80                                    # 1.0 on the boundary of a diamond
    core = np.exp(-(d / (0.46 * pulse)) ** 2)
    halo = np.exp(-(d / (1.0 * pulse)) ** 2) * 0.6
    reach = [0.72, 0.95, 0.85, 0.6][frame % 4]
    needle = np.exp(-(x / 0.07) ** 2) * np.clip(1.0 - ay / reach, 0, 1) * 0.75
    cross = np.exp(-(y / 0.07) ** 2) * np.clip(1.0 - ax / (reach * 0.55), 0, 1) * 0.4
    edge = np.clip((1.0 - np.maximum(ax, ay)) / 0.12, 0, 1)
    return _white((core + halo + needle + cross) * edge)


def spark(frame: int, size: int = 32) -> np.ndarray:
    x, y = _grid(size, size)
    s = [0.55, 0.85, 1.0, 0.7][frame % 4]
    long_, short = 0.62 * s, 0.045 + 0.02 * s
    a = np.exp(-((x / long_) ** 2 + (y / short) ** 2))
    b = np.exp(-((y / long_) ** 2 + (x / short) ** 2))
    d = math.sqrt(0.5)
    xr, yr = (x + y) * d, (y - x) * d
    diag = np.exp(-((xr / (long_ * 0.5)) ** 2 + (yr / (short * 1.2)) ** 2)) * 0.6 + np.exp(-((yr / (long_ * 0.5)) ** 2 + (xr / (short * 1.2)) ** 2)) * 0.6
    core = np.exp(-((np.abs(x) + np.abs(y)) / 0.15) ** 2)          # a small diamond, not a dot
    return _white(np.maximum.reduce([a, b, diag]) + core)


def rune_sprite(index: int, size: int = 32) -> np.ndarray:
    v = Vec(size, 8)
    for stroke in rune_strokes(index, 0, 0, 1.15, 0):
        v.polyline(stroke, 0.16)
    core = v.mask()
    glow = v.glow(2.4)
    return _white(np.clip(core + glow * 0.5 * (1 - core), 0, 1))


def ring_wave(size: int = 128) -> np.ndarray:
    """One expanding octagonal pulse with a fading trail behind its leading edge."""
    x, y = _grid(size, size)
    r = octagon_norm(x, y)
    band = np.exp(-((r - 0.9) / 0.045) ** 2)
    trail = np.exp(-((r - 0.78) / 0.14) ** 2) * 0.28 * (r < 0.9)
    edge = np.clip((1.0 - r) / 0.08, 0, 1)
    return _white((band + trail) * edge)


def glow_disc(size: int = 64) -> np.ndarray:
    """A soft octagonal glow (the halo behind the focus crystal and in the tank)."""
    x, y = _grid(size, size)
    r = octagon_norm(x, y)
    return _white(np.exp(-(r / 0.42) ** 2) * np.clip((1 - r) / 0.25, 0, 1))


def beam(w: int = 32, h: int = 128) -> np.ndarray:
    x, y = _grid(w, h)
    across = np.exp(-(x / 0.42) ** 2)
    core = np.exp(-(x / 0.12) ** 2) * 0.6
    fade = np.clip((1 - (y + 1) / 2), 0, 1) ** 1.3          # bright at the bottom, gone at the top
    return _white((across * 0.55 + core) * fade)


def shell_hex(size: int = 128) -> np.ndarray:
    """Tileable soft hexagon lattice with per-cell brightness variation (shimmering when scrolled)."""
    cols, rows = 4, 4
    sx, sy = size / cols, size / rows
    pts = []
    for j in range(rows):
        for i in range(cols):
            pts.append(((i + 0.5 * (j % 2)) * sx + sx / 2, j * sy + sy / 2))
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    best = np.full((size, size), 1e9, dtype=np.float32)
    second = np.full((size, size), 1e9, dtype=np.float32)
    cell = np.zeros((size, size), dtype=np.int32)
    for k, (px_, py_) in enumerate(pts):
        for ox in (-size, 0, size):
            for oy in (-size, 0, size):
                d = np.hypot((xx - (px_ + ox)) * 1.0, (yy - (py_ + oy)) * (sx / sy))
                closer = d < best
                second = np.where(closer, best, np.minimum(second, d))
                cell = np.where(closer, k, cell)
                best = np.where(closer, d, best)
    edge = np.clip(1.0 - (second - best) / 2.6, 0, 1)
    rng = np.random.default_rng(5)
    bright = rng.random(len(pts)).astype(np.float32)[cell % len(pts)]
    interior = 0.05 + 0.20 * bright ** 2
    return _white(np.maximum(edge * 0.95, interior))


def shell_runes(w: int = 256, h: int = 64) -> np.ndarray:
    """Tileable (horizontal) band of runes between two guide lines."""
    canvas = Vec(max(w, h), 6)
    # draw on a square canvas then crop: coordinates normalised to the canvas
    n = canvas.size
    img = px.blank(w, h)
    mask = np.zeros((h, w), dtype=np.float32)
    count = 8
    for i in range(count):
        v = Vec(64, 8)
        for stroke in rune_strokes(i * 3 + 1, 0, 0, 1.3, 0):
            v.polyline(stroke, 0.16)
        m = v.mask()
        cx = int((i + 0.5) * w / count)
        x0, y0 = cx - 32, h // 2 - 32
        for yy in range(64):
            for xx in range(64):
                ty, tx = y0 + yy, (x0 + xx) % w
                if 0 <= ty < h:
                    mask[ty, tx] = max(mask[ty, tx], m[yy, xx])
    mask[3:5, :] = np.maximum(mask[3:5, :], 0.8)
    mask[h - 5:h - 3, :] = np.maximum(mask[h - 5:h - 3, :], 0.8)
    from PIL import Image, ImageFilter
    glow = np.asarray(Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.2)), dtype=np.float32) / 255.0
    return _white(np.clip(mask + glow * 0.5 * (1 - mask), 0, 1))


def shell_facet(size: int = 128) -> np.ndarray:
    """A triangle with a bright rim, an inset second rim and glinting corners.

    The dome renderer maps one of these onto every facet of the shell (corners at (0,0), (1,0), (0.5,1)),
    which is what draws the cut-crystal edges.
    """
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    u, v = (xx + 0.5) / size, (yy + 0.5) / size
    lc = v
    lb = u - 0.5 * v
    la = 1.0 - lb - lc
    inside = (la >= 0) & (lb >= 0) & (lc >= 0)
    lam = np.stack([la, lb, lc])
    lam_sorted = np.sort(lam, axis=0)
    rim = lam_sorted[0]
    corner = lam_sorted[1]
    edge = np.exp(-(rim / 0.022) ** 2)
    inset = 0.30 * np.exp(-((rim - 0.075) / 0.012) ** 2)
    glint = 0.55 * np.exp(-(corner / 0.05) ** 2) * (rim < 0.03)
    body = 0.06 + 0.05 * px.fbm(size, size, 4, 411, 3)
    alpha = np.clip(edge + inset + glint + body, 0, 1) * inside
    return _white(alpha)


def ring_edge(w: int = 128, h: int = 32) -> np.ndarray:
    """Strip laid along the field's outline on the ground: a bright line, a soft inner fall-off and rune ticks."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    u, v = (xx + 0.5) / w, (yy + 0.5) / h
    line = np.exp(-(v / 0.09) ** 2)
    glow = 0.32 * np.exp(-(v / 0.5) ** 2)
    tick = np.clip(1.0 - np.abs(u - 0.5) / (0.10 * np.clip(1.0 - v / 0.8, 0.0, 1.0) + 1e-4), 0, 1) * (v < 0.8)
    tick = np.where(tick > 0, 0.55 + 0.45 * tick, 0.0)
    fade = np.clip((1.0 - v) / 0.1, 0, 1)
    return _white(np.clip(line + glow + tick * 0.9, 0, 1) * fade)


def mana_fluid(size: int = 64) -> np.ndarray:
    """Tileable swirling caustics used for the tank fill; scrolled by the block-entity renderer."""
    n1 = px.fbm(size, size, 4, 201, 3)
    n2 = px.fbm(size, size, 6, 202, 3)
    warp = n1 * 0.6 + n2 * 0.4
    caustic = 1.0 - np.abs(np.sin((warp - 0.5) * 9.0))
    light = np.clip(0.42 + 0.34 * caustic + (n2 - 0.5) * 0.3, 0, 1)
    body = ramp_lookup(light, pal.CYAN[1:8], 0.15, 0.95, dither=0.0)
    img = px.blank(size)
    img[..., :3] = body[..., :3]
    violet = np.clip((n1 - 0.55) * 2.0, 0, 1)[..., None] * px.hexrgb("#7548dc") * 0.35
    img[..., :3] = np.clip(img[..., :3] * (1 - violet.max(axis=2, keepdims=True) * 0.5) + violet, 0, 1)
    img[..., 3] = 0.82
    return img


def shell_soft(size: int = 128) -> np.ndarray:
    """Tileable soft drifting clouds: the plain energy dome."""
    n = px.fbm(size, size, 3, 301, 4)
    m = px.fbm(size, size, 6, 302, 3)
    alpha = np.clip(0.28 + (n - 0.5) * 1.1 + (m - 0.5) * 0.4, 0, 1) ** 1.4
    return _white(alpha)


def build_all(assets):
    part = assets / "textures" / "particle"
    for f in range(4):
        px.save(wisp(f), part / f"wisp_{f}.png")
        px.save(spark(f), part / f"spark_{f}.png")
    for i, r in enumerate((0, 3, 5, 7, 10, 13, 17, 21)):
        px.save(rune_sprite(r), part / f"rune_{i}.png")
    px.save(ring_wave(), part / "ring.png")
    v = assets / "textures" / "vfx"
    px.save(glow_disc(), v / "glow.png")
    px.save(beam(), v / "beam.png")
    px.save(shell_hex(), v / "shell_hex.png")
    px.save(shell_runes(), v / "shell_runes.png")
    px.save(mana_fluid(), v / "mana_fluid.png")
    px.save(shell_soft(), v / "shell_soft.png")
    px.save(shell_facet(), v / "shell_facet.png")
    px.save(ring_edge(), v / "ring_edge.png")
