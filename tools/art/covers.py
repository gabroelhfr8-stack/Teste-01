"""Book cover textures with gilded emblems (Codex and Warding Grimoire) and the scroll ribbon/seal sprites."""
from __future__ import annotations

import numpy as np
from PIL import Image

from . import materials, pal, px
from .blocks import ramp_lookup
from .sigil import make_glyph

S = 32


def _emblem(glyph: str, color: str, size: int = 18) -> np.ndarray:
    g = px.to_pil(make_glyph(glyph, 128)).resize((size, size), Image.LANCZOS)
    arr = np.asarray(g, dtype=np.float32) / 255.0
    out = px.blank(size)
    out[..., :3] = px.hexrgb(color)
    out[..., 3] = (arr[..., 3] > 0.5).astype(np.float32)
    return out


def cover(hue: str, glyph: str) -> np.ndarray:
    base = materials.book_cover(seed=81 if hue == "violet" else 82, hue=hue)
    shade = px.blank(S)
    emb = _emblem(glyph, "#ffe497")
    dark = emb.copy()
    dark[..., :3] = px.hexrgb("#4b2a16")
    px.over(base, dark, 7, 8)
    px.over(base, emb, 7, 7)
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
    """Greyscale wax seal (tinted per ward by an ItemColor handler): raised disc with a star."""
    img = px.blank(S)
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    d = np.hypot(xx - 15.5, yy - 15.5)
    disc = d < 13.5
    img[..., 3] = disc.astype(np.float32)
    shade = np.clip(0.78 - (xx + yy - 31) / 90.0 + (px.fbm(S, S, 6, 86, 2, tile=False) - 0.5) * 0.25, 0, 1)
    img[..., :3] = shade[..., None]
    rim = disc & (d > 11.2)
    img[..., :3] = np.where(rim[..., None], np.clip(shade[..., None] * 1.22, 0, 1), img[..., :3])
    inner = (d < 9.4) & (d > 8.2)
    img[..., :3] = np.where(inner[..., None], shade[..., None] * 0.62, img[..., :3])
    star = (np.abs(xx - 15.5) + np.abs(yy - 15.5) < 6.2) & ((np.abs(xx - 15.5) < 1.4) | (np.abs(yy - 15.5) < 1.4) | (np.abs(xx - 15.5) + np.abs(yy - 15.5) < 3.4))
    img[..., :3] = np.where(star[..., None], np.clip(shade[..., None] * 1.3, 0, 1), img[..., :3])
    return img


def build_all(assets):
    out = assets / "textures" / "item"
    blk = assets / "textures" / "block"
    px.save(cover("violet", "ambient_mana"), out / "codex_cover.png")
    px.save(cover("indigo", "bulwark"), out / "grimoire_cover.png")
    px.save(spine(), out / "book_spine.png")
    px.save(ribbon("violet"), out / "ribbon_violet.png")
    px.save(ribbon("cyan"), out / "ribbon_cyan.png")
    px.save(ribbon("gold"), out / "ribbon_gold.png")
    px.save(seal_mask(), out / "scroll_seal.png")
