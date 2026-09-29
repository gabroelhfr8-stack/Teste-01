"""Small numpy pixel-art toolkit: palettes, tileable noise, voronoi facets, ordered dithering."""
from __future__ import annotations

from pathlib import Path

import numpy as np
from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "selarium"


def hexrgb(value: str) -> np.ndarray:
    value = value.lstrip("#")
    return np.array([int(value[i:i + 2], 16) for i in (0, 2, 4)], dtype=np.float32) / 255.0


def palette(*hexes: str) -> np.ndarray:
    return np.stack([hexrgb(h) for h in hexes])


def lerp(a, b, t):
    return a + (b - a) * t


def smoothstep(t):
    t = np.clip(t, 0.0, 1.0)
    return t * t * (3.0 - 2.0 * t)


# --------------------------------------------------------------------------- images
def blank(w: int, h: int | None = None) -> np.ndarray:
    return np.zeros((h or w, w, 4), dtype=np.float32)


def solid(w: int, h: int, color) -> np.ndarray:
    img = blank(w, h)
    img[..., :3] = color
    img[..., 3] = 1.0
    return img


def to_pil(img: np.ndarray) -> Image.Image:
    arr = (np.clip(img, 0.0, 1.0) * 255.0 + 0.5).astype(np.uint8)
    return Image.fromarray(arr, "RGBA")


def save(img: np.ndarray, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    to_pil(img).save(path, optimize=True)


def load(path: Path) -> np.ndarray:
    return np.asarray(Image.open(path).convert("RGBA"), dtype=np.float32) / 255.0


def over(dst: np.ndarray, src: np.ndarray, x: int = 0, y: int = 0) -> np.ndarray:
    """Alpha-composite `src` onto `dst` at (x, y), in place."""
    h, w = src.shape[:2]
    H, W = dst.shape[:2]
    x0, y0 = max(x, 0), max(y, 0)
    x1, y1 = min(x + w, W), min(y + h, H)
    if x0 >= x1 or y0 >= y1:
        return dst
    s = src[y0 - y:y1 - y, x0 - x:x1 - x]
    d = dst[y0:y1, x0:x1]
    sa = s[..., 3:4]
    da = d[..., 3:4]
    oa = sa + da * (1 - sa)
    safe = np.where(oa > 1e-6, oa, 1.0)
    d[..., :3] = (s[..., :3] * sa + d[..., :3] * da * (1 - sa)) / safe
    d[..., 3:4] = oa
    return dst


def paint(dst: np.ndarray, mask: np.ndarray, color, alpha: float = 1.0) -> np.ndarray:
    """Composite a solid colour through a [0,1] mask."""
    layer = np.zeros_like(dst)
    layer[..., :3] = color
    layer[..., 3] = np.clip(mask, 0, 1) * alpha
    return over(dst, layer)


def rect_mask(w: int, h: int, x0: int, y0: int, x1: int, y1: int) -> np.ndarray:
    m = np.zeros((h, w), dtype=np.float32)
    m[max(y0, 0):min(y1, h), max(x0, 0):min(x1, w)] = 1.0
    return m


# ----------------------------------------------------------------------------- noise
def _rng(seed: int) -> np.random.Generator:
    return np.random.default_rng(seed)


def value_noise(w: int, h: int, cells: int, seed: int, tile: bool = True) -> np.ndarray:
    """Smooth value noise in [0,1]; tileable when `tile`."""
    rng = _rng(seed)
    cx = cy = max(1, cells)
    lat = rng.random((cy + 1, cx + 1)).astype(np.float32)
    if tile:
        lat[-1, :] = lat[0, :]
        lat[:, -1] = lat[:, 0]
    ys = np.arange(h, dtype=np.float32) / h * cy
    xs = np.arange(w, dtype=np.float32) / w * cx
    y0 = np.floor(ys).astype(int)
    x0 = np.floor(xs).astype(int)
    fy = smoothstep(ys - y0)[:, None]
    fx = smoothstep(xs - x0)[None, :]
    a = lat[y0][:, x0]
    b = lat[y0][:, x0 + 1]
    c = lat[y0 + 1][:, x0]
    d = lat[y0 + 1][:, x0 + 1]
    return lerp(lerp(a, b, fx), lerp(c, d, fx), fy)


def fbm(w: int, h: int, cells: int, seed: int, octaves: int = 4, tile: bool = True) -> np.ndarray:
    total = np.zeros((h, w), dtype=np.float32)
    amp, norm = 1.0, 0.0
    for octave in range(octaves):
        total += amp * value_noise(w, h, cells * (2 ** octave), seed + octave * 101, tile)
        norm += amp
        amp *= 0.5
    return total / norm


def stretched_noise(w: int, h: int, cx: int, cy: int, seed: int, tile: bool = True) -> np.ndarray:
    """Anisotropic value noise (different lattice density per axis) for grain / bark."""
    rng = _rng(seed)
    lat = rng.random((cy + 1, cx + 1)).astype(np.float32)
    if tile:
        lat[-1, :] = lat[0, :]
        lat[:, -1] = lat[:, 0]
    ys = np.arange(h, dtype=np.float32) / h * cy
    xs = np.arange(w, dtype=np.float32) / w * cx
    y0, x0 = np.floor(ys).astype(int), np.floor(xs).astype(int)
    fy, fx = smoothstep(ys - y0)[:, None], smoothstep(xs - x0)[None, :]
    return lerp(lerp(lat[y0][:, x0], lat[y0][:, x0 + 1], fx), lerp(lat[y0 + 1][:, x0], lat[y0 + 1][:, x0 + 1], fx), fy)


def voronoi(w: int, h: int, points: int, seed: int, tile: bool = True):
    """Returns (cell_id, d1, d2) with d1/d2 distances (pixels) to the nearest / second nearest seed."""
    rng = _rng(seed)
    pts = rng.random((points, 2)).astype(np.float32) * [w, h]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float32)
    dists = []
    offsets = [(0, 0)]
    if tile:
        offsets = [(dx * w, dy * h) for dx in (-1, 0, 1) for dy in (-1, 0, 1)]
    for ox, oy in offsets:
        for index, (px_, py_) in enumerate(pts):
            dists.append((np.hypot(xx - (px_ + ox), yy - (py_ + oy)), index))
    stack = np.stack([d for d, _ in dists])
    ids = np.array([i for _, i in dists])
    order = np.argsort(stack, axis=0)
    d1 = np.take_along_axis(stack, order[:1], 0)[0]
    d2 = None
    # second nearest seed that is a *different* cell
    best_id = ids[order[0]]
    d2 = np.full((h, w), 1e9, dtype=np.float32)
    for k in range(1, order.shape[0]):
        cand = np.take_along_axis(stack, order[k:k + 1], 0)[0]
        cid = ids[order[k]]
        take = (cid != best_id) & (d2 > 1e8)
        d2 = np.where(take, cand, d2)
    return best_id.astype(np.int32), d1, d2


BAYER4 = (np.array([[0, 8, 2, 10], [12, 4, 14, 6], [3, 11, 1, 9], [15, 7, 13, 5]], dtype=np.float32) + 0.5) / 16.0


def dither_index(values: np.ndarray, steps: int, amount: float = 0.0) -> np.ndarray:
    """Quantise a [0,1] field to `steps` integer levels, optionally with ordered dithering."""
    h, w = values.shape
    bay = np.tile(BAYER4, (h // 4 + 1, w // 4 + 1))[:h, :w]
    v = np.clip(values, 0, 1) * (steps - 1)
    if amount > 0:
        v = v + (bay - 0.5) * amount
    return np.clip(np.floor(v + 0.5), 0, steps - 1).astype(np.int32)


def apply_palette(index: np.ndarray, colors: np.ndarray) -> np.ndarray:
    h, w = index.shape
    img = np.ones((h, w, 4), dtype=np.float32)
    img[..., :3] = colors[np.clip(index, 0, len(colors) - 1)]
    return img


def edge_light(mask: np.ndarray, size: int = 1):
    """Top-left highlight / bottom-right shadow masks for a solid mask (classic pixel-art bevel)."""
    m = mask > 0.5
    up = np.zeros_like(m)
    left = np.zeros_like(m)
    down = np.zeros_like(m)
    right = np.zeros_like(m)
    for s in range(1, size + 1):
        up[s:, :] |= ~m[:-s, :]
        left[:, s:] |= ~m[:, :-s]
        down[:-s, :] |= ~m[s:, :]
        right[:, :-s] |= ~m[:, s:]
    hi = m & (up | left)
    lo = m & (down | right) & ~hi
    return hi, lo


def outline(mask: np.ndarray, thickness: int = 1) -> np.ndarray:
    """Pixels just outside `mask` (4-neighbourhood)."""
    m = mask > 0.5
    out = np.zeros_like(m)
    for _ in range(thickness):
        grown = m.copy()
        grown[1:, :] |= m[:-1, :]
        grown[:-1, :] |= m[1:, :]
        grown[:, 1:] |= m[:, :-1]
        grown[:, :-1] |= m[:, 1:]
        out |= grown & ~m
        m = grown
    return out


def upscale(img: np.ndarray, factor: int) -> np.ndarray:
    return np.repeat(np.repeat(img, factor, axis=0), factor, axis=1)


def contact_sheet(images, cols: int, cell: int, out: Path, bg=(0.15, 0.15, 0.18), labels=None) -> None:
    from PIL import ImageDraw
    rows = (len(images) + cols - 1) // cols
    pad = 6
    sheet = Image.new("RGB", (cols * (cell + pad) + pad, rows * (cell + 16 + pad) + pad), tuple(int(c * 255) for c in bg))
    draw = ImageDraw.Draw(sheet)
    for i, img in enumerate(images):
        pil = to_pil(img) if isinstance(img, np.ndarray) else img
        scale = cell / max(pil.width, pil.height)
        resample = Image.NEAREST if scale >= 1 else Image.LANCZOS
        pil = pil.resize((max(1, int(pil.width * scale)), max(1, int(pil.height * scale))), resample)
        r, c = divmod(i, cols)
        x, y = pad + c * (cell + pad), pad + r * (cell + 16 + pad)
        chk = Image.new("RGBA", (cell, cell))
        cd = ImageDraw.Draw(chk)
        for yy in range(0, cell, 8):
            for xx in range(0, cell, 8):
                cd.rectangle([xx, yy, xx + 7, yy + 7], fill=(70, 70, 78, 255) if (xx // 8 + yy // 8) % 2 else (56, 56, 64, 255))
        chk.alpha_composite(pil, ((cell - pil.width) // 2, (cell - pil.height) // 2))
        sheet.paste(chk.convert("RGB"), (x, y))
        if labels:
            draw.text((x, y + cell + 2), str(labels[i])[:28], fill=(225, 225, 235))
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out)
