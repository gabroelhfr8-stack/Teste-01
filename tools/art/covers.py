"""Book cover textures with gilded emblems (Codex and Warding Grimoire), the spine, gem and bookmark ribbon."""
from __future__ import annotations

import numpy as np
from PIL import Image

from . import materials, pal, px
from .blocks import ramp_lookup
from .sigil import make_glyph

S = 32


def _emblem(glyph: str, color: str, size: int = 22) -> np.ndarray:
    """The ward glyph with thickened strokes, so it survives the reduction to a 32px cover."""
    from PIL import ImageFilter
    big = make_glyph(glyph, 256)
    alpha = Image.fromarray((np.clip(big[..., 3], 0, 1) * 255).astype(np.uint8), "L").filter(ImageFilter.MaxFilter(3))
    small = np.asarray(alpha.resize((size, size), Image.LANCZOS), dtype=np.float32) / 255.0
    out = px.blank(size)
    out[..., :3] = px.hexrgb(color)
    out[..., 3] = (small > 0.35).astype(np.float32)
    return out


def _halo(emblem: np.ndarray, color: str, radius: float = 2.2) -> np.ndarray:
    """Soft coloured glow around the emblem."""
    from PIL import ImageFilter
    pad = 6
    canvas = np.zeros((emblem.shape[0] + pad * 2, emblem.shape[1] + pad * 2), dtype=np.float32)
    canvas[pad:-pad, pad:-pad] = emblem[..., 3]
    glow = np.asarray(Image.fromarray((canvas * 255).astype(np.uint8), "L").filter(ImageFilter.GaussianBlur(radius)), dtype=np.float32) / 255.0
    out = px.blank(canvas.shape[0])
    out[..., :3] = px.hexrgb(color)
    out[..., 3] = np.clip(glow * 1.6, 0, 0.7)
    return out


def cover(hue: str, glyph: str) -> np.ndarray:
    base = materials.book_cover(seed=81 if hue == "violet" else 82, hue=hue)
    emb = _emblem(glyph, "#ffe497")
    ox = (S - emb.shape[0]) // 2
    oy = (S - emb.shape[1]) // 2 - 1
    px.over(base, _halo(emb, "#6adcf0"), ox - 6, oy - 6)
    dark = emb.copy()
    dark[..., :3] = px.hexrgb("#3a1f10")
    px.over(base, dark, ox, oy + 1)
    px.over(base, emb, ox, oy)
    # gilded corner studs
    for (x, y) in ((3, 3), (28, 3), (3, 28), (28, 28)):
        base[y, x, :3] = pal.GOLD[6]
        base[y + 1, x + 1, :3] = pal.GOLD[3]
    return base


def spine() -> np.ndarray:
    base = materials.book_cover(seed=83, hue="violet")
    for y in (4, 5, 26, 27):
        base[y, :, :3] = pal.GOLD[5]
    return base


def gem() -> np.ndarray:
    from .materials import crystal_cyan
    return crystal_cyan(seed=84)


def ribbon(hue: str) -> np.ndarray:
    ramp = {"violet": pal.VIOLET, "cyan": pal.CYAN, "gold": pal.GOLD}[hue]
    img = px.blank(S)
    img[..., 3] = 1
    n = px.stretched_noise(S, S, 2, 10, 85)
    img[..., :3] = ramp_lookup(n, ramp[2:7], 0.2, 0.9, dither=0.2)[..., :3]
    img[:, 0, :3] = ramp[6]
    img[:, -1, :3] = ramp[1]
    return img


def build_all(assets):
    out = assets / "textures" / "item"
    blk = assets / "textures" / "block"
    px.save(cover("violet", "spectral"), out / "codex_cover.png")
    px.save(cover("indigo", "bulwark"), out / "grimoire_cover.png")
    px.save(spine(), out / "book_spine.png")
    px.save(ribbon("gold"), out / "ribbon_gold.png")
