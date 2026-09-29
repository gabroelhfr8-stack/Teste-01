"""Open-book page spreads and the 2D scroll sprites.

Everything is 16x16 and hand-placed pixel by pixel, like the vanilla item and book textures it sits next to.
"""
from __future__ import annotations

import numpy as np

from . import pix, px

S = 16

INK_OUTLINE = "#3a2412"
PAPER = {
    "hi": "#fdf1c8", "light": "#f0dcaa", "mid": "#dcbd7e", "shade": "#b9955a", "dark": "#8f6b3a",
}


def _c(hex_color: str) -> np.ndarray:
    return px.hexrgb(hex_color)


def _put(img: np.ndarray, x: int, y: int, color, alpha: float = 1.0) -> None:
    if 0 <= x < img.shape[1] and 0 <= y < img.shape[0]:
        img[y, x, :3] = color
        img[y, x, 3] = alpha


# ------------------------------------------------------------------------------------------- open book
def pages_spread(ink: str, seed: int) -> np.ndarray:
    """A whole open book, spine down the middle: the pages element of the model samples it with world-aligned UVs.

    The left page carries a small sigil drawing and a few lines, the right page is all text. Gutter shading darkens
    both pages towards the fold.
    """
    rng = np.random.default_rng(seed)
    img = px.blank(S)
    img[..., 3] = 1.0
    tone = _c(PAPER["light"])
    for y in range(S):
        for x in range(S):
            gutter = min(abs(x - 7.5), 7.5) / 7.5            # 0 at the fold, 1 at the outer edge
            shade = 0.80 + 0.20 * min(1.0, gutter * 1.6)
            grain = 1.0 + (rng.random() - 0.5) * 0.06
            img[y, x, :3] = tone * shade * grain
    # the fold itself and the darker edge of each page block
    for y in range(S):
        img[y, 7, :3] = _c(PAPER["shade"])
        img[y, 8, :3] = _c(PAPER["shade"])
        img[y, 6, :3] = _c(PAPER["mid"]) * 0.98
        img[y, 9, :3] = _c(PAPER["mid"]) * 0.98
    ink_c = _c(ink)
    # right page: lines of text of varying length
    for row, y in enumerate((3, 5, 7, 9, 11, 13)):
        length = (5, 4, 5, 3, 5, 2)[row]
        for x in range(9, 9 + length):
            _put(img, x, y, ink_c, 0.9)
    # left page: a little sigil (pixel circle with a cross) above two lines of text
    disc = pix.circle_ring(8, 3.4, 2.2)
    for yy in range(8):
        for xx in range(8):
            if disc[yy, xx]:
                _put(img, 1 + xx, 2 + yy, ink_c, 0.95)
    for dx, dy in ((0, -1), (0, 1), (-1, 0), (1, 0), (0, 0)):
        _put(img, 4 + dx + 1, 5 + dy, ink_c, 0.95)
    for y, length in ((11, 5), (13, 3)):
        for x in range(2, 2 + length):
            _put(img, x, y, ink_c, 0.9)
    # a dry, warm border on the outer edges so each page reads as a separate sheet
    for y in range(S):
        img[y, 0, :3] = _c(PAPER["mid"])
        img[y, S - 1, :3] = _c(PAPER["mid"])
    for x in range(S):
        img[0, x, :3] = _c(PAPER["mid"])
        img[S - 1, x, :3] = _c(PAPER["mid"])
    return img


# ------------------------------------------------------------------------------------------- scrolls
def _scroll_masks() -> tuple[np.ndarray, np.ndarray, np.ndarray]:
    top = np.zeros((S, S), dtype=bool)
    sheet = np.zeros((S, S), dtype=bool)
    bottom = np.zeros((S, S), dtype=bool)
    top[1, 3:13] = True                       # a roll: three rows of cylinder with rounded ends
    top[2:4, 2:14] = True
    sheet[4:12, 3:13] = True
    bottom[12:14, 2:14] = True
    bottom[14, 3:13] = True
    return top, sheet, bottom


def scroll_base(ribbon: tuple[str, str, str] | None, sparkle: str | None = None) -> np.ndarray:
    """A parchment scroll: two rolled ends, a sheet between them, optionally tied with a ribbon."""
    top, sheet, bottom = _scroll_masks()
    img = px.blank(S)
    cyl = [_c(PAPER["hi"]), _c(PAPER["mid"]), _c(PAPER["shade"])]         # light, mid, shadow rows of a roll
    for y in range(S):
        for x in range(S):
            if top[y, x]:
                img[y, x, :3] = cyl[min(2, y - 1)]
                img[y, x, 3] = 1.0
            elif bottom[y, x]:
                img[y, x, :3] = cyl[min(2, y - 12)]
                img[y, x, 3] = 1.0
            elif sheet[y, x]:
                edge = x in (3, 12)
                img[y, x, :3] = _c(PAPER["shade"]) if edge else (_c(PAPER["mid"]) if y == 4 else _c(PAPER["light"]))
                img[y, x, 3] = 1.0
    # spiral ends of the rolls
    for (x, y) in ((2, 2), (2, 3), (13, 2), (13, 3), (2, 12), (13, 12)):
        _put(img, x, y, _c(PAPER["dark"]))
    for (x, y) in ((3, 2), (12, 2), (3, 12), (12, 12)):
        _put(img, x, y, _c(PAPER["shade"]))
    # a couple of faint lines of writing on the sheet
    ink = _c("#8f6b3a")
    for y, x0, x1 in ((5, 5, 10), (10, 5, 9)):
        for x in range(x0, x1 + 1):
            _put(img, x, y, ink, 0.85)
    if ribbon:
        hi, mid, low = (_c(c) for c in ribbon)
        for x in range(2, 14):
            end = x in (2, 13)
            _put(img, x, 7, low if end else hi)
            _put(img, x, 8, low if end else mid)
        _put(img, 13, 9, low)
        _put(img, 12, 9, mid)                # the loose end of the bow
    if sparkle:
        c = _c(sparkle)
        for (x, y) in ((7, 5), (6, 6), (8, 6), (7, 6), (7, 10)):
            _put(img, x, y, c)
    return _outlined(img)


def _outlined(img: np.ndarray) -> np.ndarray:
    mask = img[..., 3] > 0.5
    ring = px.outline(mask)
    for y in range(S):
        for x in range(S):
            if ring[y, x]:
                _put(img, x, y, _c(INK_OUTLINE))
    return img


def scroll_seal() -> np.ndarray:
    """Greyscale wax seal for the ItemColor handler to tint per ward: a pixel disc with a raised cross."""
    img = px.blank(S)
    disc = pix.circle_disc(S, 3.4)
    yy, xx = np.mgrid[0:S, 0:S].astype(np.float32)
    c = pix.center(S)
    light = np.clip(0.86 - ((xx - c) + (yy - c)) * 0.045, 0.45, 1.0)
    for y in range(S):
        for x in range(S):
            if disc[y, x]:
                img[y, x, :3] = light[y, x]
                img[y, x, 3] = 1.0
    ring = px.outline(disc)
    for y in range(S):
        for x in range(S):
            if ring[y, x]:
                img[y, x, :3] = 0.42
                img[y, x, 3] = 1.0
    cross = [(7, 6), (8, 6), (6, 7), (7, 7), (8, 7), (9, 7), (6, 8), (7, 8), (8, 8), (9, 8), (7, 9), (8, 9)]
    for (x, y) in cross:                     # the stamp: pressed in, so darker than the wax around it
        if img[y, x, 3] > 0.5:
            img[y, x, :3] = np.clip(light[y, x] - 0.3, 0.0, 1.0)
    for (x, y) in ((5, 5), (6, 5), (5, 6)):  # a glint on the upper left of the blob
        if img[y, x, 3] > 0.5:
            img[y, x, :3] = 1.0
    return img


def build_all(assets):
    out = assets / "textures" / "item"
    px.save(pages_spread("#4a2f7a", 91), out / "book_pages_codex.png")
    px.save(pages_spread("#27437a", 92), out / "book_pages_grimoire.png")
    px.save(scroll_base(None), out / "empty_scroll.png")
    px.save(scroll_base(("#7548dc", "#5230b0", "#361c7c")), out / "ward_scroll.png")
    px.save(scroll_seal(), out / "ward_scroll_seal.png")
    px.save(scroll_base(("#33b4d4", "#1783a6", "#0f5670"), sparkle="#defbff"), out / "attunement_scroll.png")
