"""Pixel-art building blocks for world-space effects: chalk circle, rings, marks, particles and shell tiles.

Everything here is drawn straight onto a small pixel grid (32x32 for a full block, 8x8 for small marks, 16x16 for
tiles) with no anti-aliasing, so a circle is the stair-stepped circle of Minecraft: pixel centres inside a radius.
White textures are tinted at render time.
"""
from __future__ import annotations

import math
import random

import numpy as np
from PIL import Image, ImageDraw

from . import px


def new(size: int) -> tuple[Image.Image, ImageDraw.ImageDraw]:
    img = Image.new("L", (size, size), 0)
    return img, ImageDraw.Draw(img)


def center(size: int) -> float:
    return (size - 1) / 2.0


def dist_grid(size: int) -> np.ndarray:
    c = center(size)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    return np.hypot(xx - c, yy - c)


def circle_ring(size: int, r_out: float, r_in: float) -> np.ndarray:
    """Pixels whose centre lies between two radii: a Minecraft circle outline."""
    d = dist_grid(size)
    return (d <= r_out) & (d > r_in)


def circle_disc(size: int, r: float) -> np.ndarray:
    return dist_grid(size) <= r


def line(mask: np.ndarray, x0: float, y0: float, x1: float, y1: float) -> None:
    """Bresenham line onto a boolean mask."""
    x0, y0, x1, y1 = int(round(x0)), int(round(y0)), int(round(x1)), int(round(y1))
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    err = dx + dy
    while True:
        if 0 <= y0 < mask.shape[0] and 0 <= x0 < mask.shape[1]:
            mask[y0, x0] = True
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy


def polar(size: int, radius: float, degrees: float) -> tuple[float, float]:
    c = center(size)
    a = math.radians(degrees)
    return c + radius * math.cos(a), c + radius * math.sin(a)


def stamp(mask: np.ndarray, rows: list[str], x: int, y: int) -> None:
    for j, row in enumerate(rows):
        for i, ch in enumerate(row):
            if ch == "#" and 0 <= y + j < mask.shape[0] and 0 <= x + i < mask.shape[1]:
                mask[y + j, x + i] = True


def white(mask: np.ndarray, alpha: np.ndarray | float = 1.0) -> np.ndarray:
    img = px.blank(mask.shape[1], mask.shape[0])
    img[..., :3] = 1.0
    img[..., 3] = mask.astype(np.float32) * alpha
    return img


# ---------------------------------------------------------------------------------------- runes
# 3x5 pixel runes for rings and floating particles (angular, futhark-like).
RUNES_3X5 = [
    ["#..", "##.", "#.#", "#..", "#.."],
    ["#.#", "#.#", "#.#", "#.#", "###"],
    ["#..", "###", "#..", "#..", "#.."],
    ["#..", "##.", "#..", "##.", "#.."],
    ["##.", "#.#", "##.", "#.#", "#.#"],
    ["..#", ".#.", "#..", ".#.", "..#"],
    ["#.#", ".#.", ".#.", ".#.", "#.#"],
    ["#..", "#.#", "###", "#..", "#.."],
    ["#.#", "#.#", "###", "#.#", "#.#"],
    [".#.", ".#.", "###", ".#.", ".#."],
    [".#.", ".#.", ".#.", ".#.", ".#."],
    ["#.#", "#.#", ".#.", "#.#", "#.#"],
]


# ------------------------------------------------------------------------------------- sigil textures
def base_circle(size: int = 32) -> np.ndarray:
    """The chalk circle on the ground: two rings, an inner circle, eight spokes and tick marks."""
    rng = random.Random(7)
    m = np.zeros((size, size), dtype=bool)
    m |= circle_ring(size, 15.6, 14.3)
    m |= circle_ring(size, 12.5, 11.5)
    m |= circle_ring(size, 5.3, 4.3)
    for k in range(8):
        a = k * 45.0
        x0, y0 = polar(size, 5.6, a)
        x1, y1 = polar(size, 11.2, a)
        line(m, x0, y0, x1, y1)
    for k in range(16):
        a = k * 22.5 + 11.25
        x0, y0 = polar(size, 12.9, a)
        x1, y1 = polar(size, 13.6, a)
        line(m, x0, y0, x1, y1)
    img = px.blank(size)
    chalk = px.hexrgb("#e8e0f6")
    shade = np.array([[0.82 + 0.18 * rng.random() for _ in range(size)] for _ in range(size)], dtype=np.float32)
    img[..., :3] = chalk * shade[..., None]
    img[..., 3] = m.astype(np.float32)
    for _ in range(26):                     # chalk dust around the lines
        x, y = rng.randrange(size), rng.randrange(size)
        if not m[y, x] and math.hypot(x - center(size), y - center(size)) < 15.8:
            img[y, x, :3] = chalk * 0.9
            img[y, x, 3] = 0.55
    return img


def ring_outer(size: int = 32) -> np.ndarray:
    m = circle_ring(size, 15.6, 14.4) | circle_ring(size, 13.4, 12.6)
    for k in range(8):                       # studs on the outer ring
        x, y = polar(size, 14.0, k * 45.0 + 22.5)
        xi, yi = int(round(x)), int(round(y))
        m[yi, xi] = True
    x, y = polar(size, 11.6, -90.0)          # one marker, so a quarter turn is visible
    stamp(m, ["##", "##"], int(round(x - 1)), int(round(y - 1)))
    return white(m)


def ring_runes(size: int = 32) -> np.ndarray:
    m = circle_ring(size, 15.6, 14.6) | circle_ring(size, 10.2, 9.3)
    for k in range(12):
        x, y = polar(size, 12.15, k * 30.0 - 90.0)
        stamp(m, RUNES_3X5[k % len(RUNES_3X5)], int(round(x - 1)), int(round(y - 2)))
    return white(m)


def ring_star(size: int = 32) -> np.ndarray:
    """A compass rose: two squares turned by 45 degrees inside a circle, with one marked corner."""
    m = circle_ring(size, 14.9, 14.0)
    square = [polar(size, 13.6, k * 90.0 - 90.0) for k in range(4)]
    diamond = [polar(size, 13.6, k * 90.0 - 45.0) for k in range(4)]
    for shape in (square, diamond):
        for k in range(4):
            a, b = shape[k], shape[(k + 1) % 4]
            line(m, a[0], a[1], b[0], b[1])
    m |= circle_ring(size, 4.6, 3.7)
    x, y = polar(size, 8.6, -90.0)
    stamp(m, ["##", "##"], int(round(x - 1)), int(round(y - 1)))
    return white(m)


def ring_dash(size: int = 32) -> np.ndarray:
    c = center(size)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    ang = (np.degrees(np.arctan2(yy - c, xx - c)) + 360.0) % 360.0
    m = circle_ring(size, 12.2, 11.2) & ((ang % 15.0) < 9.0)
    for k in range(6):                       # small arrowheads pointing round the circle
        x, y = polar(size, 7.4, k * 60.0 - 90.0)
        stamp(m, ["#.#", ".#."], int(round(x - 1)), int(round(y - 1)))
    x, y = polar(size, 12.2, -90.0)          # one long dash at the top
    stamp(m, ["#####"], int(round(x - 2)), int(round(y)))
    return white(m)


# ---------------------------------------------------------------------------------------- dust marks
def _mark(draw_fn, size: int = 8) -> np.ndarray:
    img, d = new(size)
    draw_fn(d)
    return white(np.asarray(img) > 0)


def marks() -> dict[str, np.ndarray]:
    def arcane(d):
        d.polygon([(3, 0), (4, 0), (4, 2), (7, 3), (7, 4), (4, 5), (4, 7), (3, 7), (3, 5), (0, 4), (0, 3), (3, 2)], fill=255)

    def aegis(d):
        d.polygon([(0, 0), (7, 0), (7, 4), (5, 6), (4, 7), (3, 7), (2, 6), (0, 4)], outline=255, fill=0)
        d.line([(3, 1), (3, 5)], fill=255)
        d.line([(4, 1), (4, 5)], fill=255)

    def vital(d):
        d.polygon([(7, 0), (7, 3), (5, 6), (2, 6), (0, 4), (1, 1), (4, 0)], fill=255)
        d.line([(0, 7), (4, 3)], fill=0)

    def focus(d):
        d.ellipse([1, 1, 6, 6], outline=255)
        d.point([(3, 3), (4, 3), (3, 4), (4, 4)], fill=255)
        d.point([(3, 0), (4, 0), (3, 7), (4, 7), (0, 3), (0, 4), (7, 3), (7, 4)], fill=255)

    def binding(d):
        d.ellipse([0, 1, 4, 5], outline=255)
        d.ellipse([3, 2, 7, 6], outline=255)

    def echo(d):
        d.arc([0, 0, 7, 7], 200, 340, fill=255)
        d.arc([2, 2, 5, 5], 200, 340, fill=255)
        d.point([(3, 6), (4, 6)], fill=255)

    def density(d):
        d.polygon([(3, 0), (4, 0), (7, 3), (7, 4), (4, 7), (3, 7), (0, 4), (0, 3)], outline=255, fill=0)
        d.point([(3, 3), (4, 3), (3, 4), (4, 4)], fill=255)

    def warp(d):
        d.line([(1, 3), (1, 1), (5, 1), (6, 2), (6, 5), (3, 6), (2, 5), (2, 4), (4, 3), (5, 4)], fill=255)

    def veil(d):
        d.ellipse([0, 0, 7, 7], fill=255)
        d.ellipse([3, 0, 9, 6], fill=0)

    def chrono(d):
        d.ellipse([0, 0, 7, 7], outline=255)
        d.line([(3, 3), (3, 1)], fill=255)
        d.line([(4, 4), (5, 4)], fill=255)
        d.point([(3, 4), (4, 3)], fill=255)

    return {name: _mark(fn) for name, fn in (
        ("arcane", arcane), ("aegis", aegis), ("vital", vital), ("focus", focus), ("binding", binding),
        ("echo", echo), ("density", density), ("warp", warp), ("veil", veil), ("chrono", chrono))}


# ------------------------------------------------------------------------------------------ particles
def wisp(frame: int, size: int = 8) -> np.ndarray:
    """A small pixel orb with a bright core, like vanilla's glow and end-rod particles."""
    c = center(size)
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float32)
    d = np.hypot(xx - c, yy - c)
    swell = [0.0, 0.35, 0.6, 0.35][frame % 4]
    core = d <= 1.1 + swell * 0.3
    mid = d <= 2.1 + swell
    halo = d <= 3.4
    alpha = np.where(core, 1.0, np.where(mid, 0.62, np.where(halo, 0.22, 0.0)))
    img = px.blank(size)
    img[..., :3] = 1.0
    img[..., 3] = alpha
    return img


def spark(frame: int, size: int = 8) -> np.ndarray:
    m = np.zeros((size, size), dtype=bool)
    c = center(size)
    arm = [2, 3, 3, 2][frame % 4]
    lo, hi = int(c - arm + 0.5), int(c + arm + 0.5)
    m[int(c - 0.5):int(c + 1.5), max(0, lo):min(size, hi + 1)] = True
    m[max(0, lo):min(size, hi + 1), int(c - 0.5):int(c + 1.5)] = True
    core = np.zeros((size, size), dtype=bool)
    core[int(c - 0.5):int(c + 1.5), int(c - 0.5):int(c + 1.5)] = True
    img = white(m, 0.85)
    img[..., 3] = np.where(core, 1.0, img[..., 3])
    return img


def rune_particle(index: int, size: int = 8) -> np.ndarray:
    m = np.zeros((size, size), dtype=bool)
    stamp(m, RUNES_3X5[(index * 3 + 1) % len(RUNES_3X5)], 2, 1)
    return white(m)


def glow_halo(size: int = 16) -> np.ndarray:
    """Pixel glow for the focus crystal and the tank surface: three brightness rings."""
    d = dist_grid(size)
    alpha = np.where(d <= 2.0, 0.85, np.where(d <= 4.2, 0.5, np.where(d <= 6.2, 0.22, np.where(d <= 7.7, 0.08, 0.0))))
    img = px.blank(size)
    img[..., :3] = 1.0
    img[..., 3] = alpha
    return img


# -------------------------------------------------------------------------------------------- tiles
def _frame(size: int = 16, edge: float = 0.78, corner: float = 1.0) -> np.ndarray:
    a = np.zeros((size, size), dtype=np.float32)
    a[0, :] = a[-1, :] = a[:, 0] = a[:, -1] = edge
    for y, x in ((0, 0), (0, size - 1), (size - 1, 0), (size - 1, size - 1)):
        a[y, x] = corner
    return a


def shell_tile(style: str, size: int = 16) -> np.ndarray:
    """One block face of the field shell: a one-pixel frame (so the shell reads as a grid of blocks) plus an interior."""
    rng = np.random.default_rng({"soft": 11, "hex": 12, "runes": 13}[style])
    a = _frame(size, 0.7 if style != "hex" else 0.6)
    inner = np.zeros((size, size), dtype=np.float32)
    if style == "soft":
        for _ in range(9):
            x, y = int(rng.integers(2, size - 2)), int(rng.integers(2, size - 2))
            inner[y, x] = 0.30
        for _ in range(4):
            x, y = int(rng.integers(3, size - 3)), int(rng.integers(3, size - 3))
            inner[y, x] = 0.55
            inner[y, x + 1] = 0.25
    elif style == "hex":
        m = np.zeros((size, size), dtype=bool)
        # a pixel hexagon in the middle, and its six spokes pointing at the frame
        for (x0, y0, x1, y1) in ((5, 3, 10, 3), (10, 3, 12, 8), (12, 8, 10, 12), (10, 12, 5, 12), (5, 12, 3, 8), (3, 8, 5, 3)):
            line(m, x0, y0, x1, y1)
        inner = np.maximum(inner, m.astype(np.float32) * 0.38)
        inner[7:9, 7:9] = 0.5
    else:
        m = np.zeros((size, size), dtype=bool)
        stamp(m, RUNES_3X5[int(rng.integers(0, len(RUNES_3X5)))], 6, 5)
        inner = np.maximum(inner, m.astype(np.float32) * 0.6)
    a = np.maximum(a, inner)
    img = px.blank(size)
    img[..., :3] = 1.0
    img[..., 3] = a
    return img


def ring_tile(size: int = 16) -> np.ndarray:
    """A floor tile of the field's outline and of the pulse rings: bright frame, faint fill, brighter corners."""
    a = np.full((size, size), 0.16, dtype=np.float32)
    a = np.maximum(a, _frame(size, 0.9, 1.0))
    a[1, 1:-1] = np.maximum(a[1, 1:-1], 0.35)
    a[-2, 1:-1] = np.maximum(a[-2, 1:-1], 0.35)
    a[1:-1, 1] = np.maximum(a[1:-1, 1], 0.35)
    a[1:-1, -2] = np.maximum(a[1:-1, -2], 0.35)
    img = px.blank(size)
    img[..., :3] = 1.0
    img[..., 3] = a
    return img


def build_all(assets) -> None:
    base = assets / "textures" / "vfx" / "sigil"
    px.save(base_circle(), base / "base_circle.png")
    px.save(ring_outer(), base / "ring_outer.png")
    px.save(ring_runes(), base / "ring_runes.png")
    px.save(ring_star(), base / "ring_star.png")
    px.save(ring_dash(), base / "ring_dash.png")
    for name, img in marks().items():
        px.save(img, base / "component" / f"{name}.png")
    part = assets / "textures" / "particle"
    for f in range(4):
        px.save(wisp(f), part / f"wisp_{f}.png")
        px.save(spark(f), part / f"spark_{f}.png")
    for i in range(8):
        px.save(rune_particle(i), part / f"rune_{i}.png")
    px.save(ring_tile(), part / "ring.png")
    v = assets / "textures" / "vfx"
    px.save(glow_halo(), v / "glow.png")
    for style in ("soft", "hex", "runes"):
        px.save(shell_tile(style), v / f"shell_{style}.png")
    px.save(ring_tile(), v / "ring_tile.png")
