"""Mana fluid texture. The other effect sprites (rings, particles, shell tiles) are pixel art: see pix.py."""
from __future__ import annotations

import numpy as np

from . import pal, px
from .blocks import ramp_lookup


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


def build_all(assets):
    px.save(mana_fluid(), assets / "textures" / "vfx" / "mana_fluid.png")
