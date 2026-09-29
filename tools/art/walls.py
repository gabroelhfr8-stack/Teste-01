"""Animated ward blocks (Citadel wall, Tangible barrier), the Phasing block and lit machine decals."""
from __future__ import annotations

import json
import math

import numpy as np

from . import materials, pal, px
from .blocks import ramp_lookup

S = 32


def _save_animated(assets, name: str, frames: list[np.ndarray], frametime: int, interpolate: bool = True):
    strip = np.concatenate(frames, axis=0)
    path = assets / "textures" / "block" / f"{name}.png"
    px.save(strip, path)
    meta = {"animation": {"frametime": frametime, "interpolate": interpolate}}
    (path.parent / f"{name}.png.mcmeta").write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")


def citadel_wall(frames_n: int = 8) -> list[np.ndarray]:
    """Dark ashlar with glowing cyan mortar; a pulse of light travels along the seams."""
    base = materials.ritual_stone(seed=91, dark=True)
    yy, xx = np.mgrid[0:S, 0:S]
    seam = np.zeros((S, S), dtype=bool)
    for r in range(4):
        seam[r * 8, :] = True
        off = 0 if r % 2 == 0 else 8
        for bx in range(2):
            seam[r * 8:r * 8 + 8, (bx * 16 + off) % S] = True
    frames = []
    for f in range(frames_n):
        img = base.copy()
        phase = f / frames_n * 2 * math.pi
        wave = 0.5 + 0.5 * np.sin((xx + yy) / S * 2 * math.pi - phase)
        glow = np.clip(0.35 + 0.65 * wave, 0, 1)
        idx = np.clip((glow * 5).astype(int), 0, len(pal.CYAN) - 1)
        col = pal.CYAN[np.clip(idx + 1, 0, len(pal.CYAN) - 1)]
        img[..., :3] = np.where(seam[..., None], col, img[..., :3])
        # faint bloom on the brick pixels next to a seam
        near = np.zeros_like(seam)
        near[1:, :] |= seam[:-1, :]
        near[:-1, :] |= seam[1:, :]
        near[:, 1:] |= seam[:, :-1]
        near[:, :-1] |= seam[:, 1:]
        bloom = (near & ~seam)[..., None] * (glow[..., None] * 0.22)
        img[..., :3] = np.clip(img[..., :3] + bloom * px.hexrgb("#6adcf0"), 0, 1)
        frames.append(img)
    return frames


def citadel_glow(frames_n: int = 8) -> list[np.ndarray]:
    """Only the glowing seams of the citadel wall (the rest transparent), drawn as an emissive overlay."""
    yy, xx = np.mgrid[0:S, 0:S]
    seam = np.zeros((S, S), dtype=bool)
    for r in range(4):
        seam[r * 8, :] = True
        off = 0 if r % 2 == 0 else 8
        for bx in range(2):
            seam[r * 8:r * 8 + 8, (bx * 16 + off) % S] = True
    frames = []
    for f in range(frames_n):
        phase = f / frames_n * 2 * math.pi
        wave = 0.5 + 0.5 * np.sin((xx + yy) / S * 2 * math.pi - phase)
        glow = np.clip(0.35 + 0.65 * wave, 0, 1)
        idx = np.clip((glow * 5).astype(int), 0, len(pal.CYAN) - 1)
        img = px.blank(S)
        img[..., :3] = pal.CYAN[np.clip(idx + 1, 0, len(pal.CYAN) - 1)]
        img[..., 3] = seam.astype(np.float32)
        frames.append(img)
    return frames


def barrier(frames_n: int = 16) -> list[np.ndarray]:
    """Translucent violet force field: scrolling hex lattice with drifting sparkles."""
    rng = np.random.default_rng(93)
    cols, rows = 2, 2
    sx, sy = S / cols, S / rows
    pts = [((i + 0.5 * (j % 2)) * sx + sx / 2, j * sy + sy / 2) for j in range(rows) for i in range(cols)]
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    frames = []
    sparkle_pos = [(int(rng.integers(0, S)), int(rng.integers(0, S)), float(rng.random())) for _ in range(7)]
    for f in range(frames_n):
        shift = f * (S / frames_n)
        best = np.full((S, S), 1e9, dtype=np.float32)
        second = np.full((S, S), 1e9, dtype=np.float32)
        cell = np.zeros((S, S), dtype=np.int32)
        for k, (px_, py_) in enumerate(pts):
            for ox in (-S, 0, S):
                for oy in (-S, 0, S):
                    d = np.hypot(xx - (px_ + ox), (yy - shift) - (py_ + oy))
                    closer = d < best
                    second = np.where(closer, best, np.minimum(second, d))
                    cell = np.where(closer, k, cell)
                    best = np.where(closer, d, best)
        edge = np.clip(1.0 - (second - best) / 1.7, 0, 1)
        img = px.blank(S)
        pulse = 0.5 + 0.5 * math.sin(f / frames_n * 2 * math.pi)
        fill = 0.14 + 0.05 * pulse
        img[..., :3] = pal.VIOLET[5]
        img[..., 3] = fill
        edge_col = pal.CYAN[6] * 0.7 + pal.VIOLET[7] * 0.3
        img[..., :3] = np.where((edge > 0.5)[..., None], edge_col, img[..., :3])
        img[..., 3] = np.where(edge > 0.5, 0.78, img[..., 3])
        for (sx_, sy_, ph) in sparkle_pos:
            y = int((sy_ + f * 2 * (0.5 + ph)) % S)
            if 0 <= y < S:
                img[y, sx_, :3] = pal.SILVER[8 - 1]
                img[y, sx_, 3] = 0.95
        frames.append(img)
    return frames


def phasing_block() -> np.ndarray:
    """Ghostly frame block: glowing runic corners around a translucent violet core."""
    img = px.blank(S)
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    d = np.maximum(np.abs(xx - 15.5), np.abs(yy - 15.5))
    n = px.fbm(S, S, 5, 94, 3)
    core = ramp_lookup(n, pal.VIOLET[3:8], 0.2, 0.9, dither=0.0)
    img[..., :3] = core[..., :3]
    img[..., 3] = 0.28 + 0.14 * n
    frame = d > 13.5
    img[..., :3] = np.where(frame[..., None], pal.VIOLET[6], img[..., :3])
    img[..., 3] = np.where(frame, 0.95, img[..., 3])
    inner = (d > 12.5) & (d <= 13.5)
    img[..., :3] = np.where(inner[..., None], pal.VIOLET[3], img[..., :3])
    img[..., 3] = np.where(inner, 0.9, img[..., 3])
    for cx, cy in ((3, 3), (28, 3), (3, 28), (28, 28)):
        for dx in range(-2, 3):
            for dy in range(-2, 3):
                if abs(dx) + abs(dy) <= 2:
                    img[cy + dy, cx + dx, :3] = pal.CYAN[7 if dx == 0 and dy == 0 else 5]
                    img[cy + dy, cx + dx, 3] = 1
    return img


def lit_variants():
    lit_panel = materials.rune_panel(seed=70)
    # brighten runes to full glow and add bloom rows
    lit_panel[..., :3] = np.where((lit_panel[..., :3].sum(axis=2, keepdims=True) > 1.6), pal.CYAN[7], lit_panel[..., :3])
    lit_panel[3, :, :3] = pal.CYAN[5]
    lit_panel[-4, :, :3] = pal.CYAN[5]
    crystal = materials.crystal_cyan(seed=66)
    crystal[..., :3] = np.clip(crystal[..., :3] * 1.25 + 0.12, 0, 1)
    return lit_panel, crystal


def build_all(assets):
    _save_animated(assets, "temporary_citadel_wall", citadel_wall(), 4)
    _save_animated(assets, "temporary_citadel_wall_glow", citadel_glow(), 4)
    _save_animated(assets, "tangible_barrier_block", barrier(), 2, True)
    px.save(phasing_block(), assets / "textures" / "block" / "phasing_block.png")
    panel, crystal = lit_variants()
    px.save(panel, assets / "textures" / "block" / "rune_panel_lit.png")
    px.save(crystal, assets / "textures" / "block" / "crystal_cyan_lit.png")
