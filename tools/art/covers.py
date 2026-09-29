"""Book cover textures with gilded emblems (Codex and Warding Grimoire) and the scroll ribbon/seal sprites."""
from __future__ import annotations

import numpy as np
from PIL import Image

from . import materials, pal, px
from .blocks import ramp_lookup
from .sigil import make_glyph
from .vec import octagon_norm

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


def seal_mask() -> np.ndarray:
    """Greyscale wax seal (tinted per ward by an ItemColor handler): a chipped octagon with a raised diamond star."""
    img = px.blank(S)
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    dx, dy = xx - 15.5, yy - 15.5
    chips = (px.fbm(S, S, 5, 87, 2, tile=False) - 0.5) * 2.2          # cracked, uneven edge
    d = octagon_norm(dx, dy) * 1.0 + chips
    disc = d < 13.2
    img[..., 3] = disc.astype(np.float32)
    shade = np.clip(0.78 - (xx + yy - 31) / 90.0 + (px.fbm(S, S, 6, 86, 2, tile=False) - 0.5) * 0.25, 0, 1)
    img[..., :3] = shade[..., None]
    rim = disc & (d > 10.9)
    img[..., :3] = np.where(rim[..., None], np.clip(shade[..., None] * 1.22, 0, 1), img[..., :3])
    inner = (d < 9.2) & (d > 8.0)
    img[..., :3] = np.where(inner[..., None], shade[..., None] * 0.62, img[..., :3])
    star = (np.abs(dx) + np.abs(dy) < 6.2) & ((np.abs(dx) < 1.4) | (np.abs(dy) < 1.4) | (np.abs(dx) + np.abs(dy) < 3.4))
    img[..., :3] = np.where(star[..., None], np.clip(shade[..., None] * 1.3, 0, 1), img[..., :3])
    return img


def build_all(assets):
    out = assets / "textures" / "item"
    blk = assets / "textures" / "block"
    px.save(cover("violet", "spectral"), out / "codex_cover.png")
    px.save(cover("indigo", "bulwark"), out / "grimoire_cover.png")
    px.save(spine(), out / "book_spine.png")
    px.save(ribbon("violet"), out / "ribbon_violet.png")
    px.save(ribbon("cyan"), out / "ribbon_cyan.png")
    px.save(ribbon("gold"), out / "ribbon_gold.png")
    px.save(seal_mask(), out / "scroll_seal.png")
