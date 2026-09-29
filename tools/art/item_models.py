"""Item models: books (closed, and open in the hands), flat scroll sprites, and plain sprite / block item definitions."""
from __future__ import annotations

import json
from pathlib import Path

from .models import BLOCK_DISPLAY, Model, tex_ns

BOOK_DISPLAY = {
    "gui": {"rotation": [22, 205, 0], "translation": [0, -0.5, 0], "scale": [0.95, 0.95, 0.95]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.75, 0.75, 0.75]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
}

# In the hands the book is open, pages towards the reader. Left and right use the same numbers: the game mirrors the
# left hand itself. In third person the hand's frame has +Y pointing forward and +Z up, so the book lies like a tray
# with its pages up and is tilted back a little towards the reader's face; in first person the camera looks down -Z,
# so the pages face it and the top of the book leans away.
OPEN_BOOK_DISPLAY = {
    "thirdperson_righthand": {"rotation": [40, 0, 0], "translation": [0, 5, 3], "scale": [0.6, 0.6, 0.6]},
    "thirdperson_lefthand": {"rotation": [40, 0, 0], "translation": [0, 5, 3], "scale": [0.6, 0.6, 0.6]},
    "firstperson_righthand": {"rotation": [-30, -12, 0], "translation": [-2, 7, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [-30, -12, 0], "translation": [-2, 7, 0], "scale": [0.4, 0.4, 0.4]},
}

PERSPECTIVES = ("thirdperson_lefthand", "thirdperson_righthand", "firstperson_lefthand", "firstperson_righthand")


def book(cover_tex: str, gem_tex: str = "block/crystal_cyan") -> Model:
    """Closed book, front cover (with the emblem) on the north face."""
    m = Model({
        "cover": tex_ns(cover_tex), "spine": tex_ns("item/book_spine"), "pages": tex_ns("block/pages_side"),
        "gold": tex_ns("block/gilded_trim"), "gem": tex_ns(gem_tex), "ribbon": tex_ns("item/ribbon_gold"),
    }, particle="cover", display=BOOK_DISPLAY)
    plain = {f: "spine" for f in ("up", "down", "south", "east", "west")}
    m.box(3, 1, 9.2, 13, 15, 9.8, {**plain, "north": "spine"}, name="back_cover")
    m.box(3, 1, 6.2, 13, 15, 6.8, {**plain, "north": "cover", "south": "spine"}, uv={"north": [16, 0, 0, 16]}, name="front_cover")
    m.box(3, 1, 6.2, 3.9, 15, 9.8, "spine", name="spine")
    m.box(3.9, 1.6, 6.8, 12.6, 14.4, 9.2, "pages", name="pages")
    for (x, y) in ((12.0, 1.0), (12.0, 13.6), (3.0, 1.0), (3.0, 13.6)):
        m.box(x, y, 6.0, x + 1.0, y + 1.4, 6.8, "gold", name="corner")
    m.box(12.2, 6.4, 5.7, 13.2, 9.6, 6.0, "gold", name="clasp")
    m.box(12.3, 7.2, 5.3, 13.1, 8.8, 5.75, "gem", name="clasp_gem")
    m.box(7, -0.6, 7.4, 8, 1.2, 8.6, "ribbon", name="bookmark")
    return m


def open_book(cover_tex: str, pages_tex: str) -> Model:
    """The book held open, pages facing +Z (the reader): two boards and two stacks of pages fanned out from the spine."""
    m = Model({
        "cover": tex_ns(cover_tex), "spine": tex_ns("item/book_spine"), "edge": tex_ns("block/pages_side"),
        "page": tex_ns(pages_tex), "gold": tex_ns("block/gilded_trim"), "ribbon": tex_ns("item/ribbon_gold"),
    }, particle="cover", display=OPEN_BOOK_DISPLAY)

    def half(side: int) -> None:
        # side +1 is the reader's right half; the left half is its mirror image about the spine (x = 8)
        def span(x0: float, x1: float) -> tuple[float, float]:
            return 8 + side * (x0 - 8), 8 + side * (x1 - 8)

        rot = ("y", -22.5 * side, (8, 8, 8))
        a, b = span(8.0, 15.0)
        m.box(a, 1, 7.4, b, 15, 8.0, {"north": "cover", "south": "spine", "up": "spine", "down": "spine", "east": "spine", "west": "spine"},
              rot=rot, name="board")
        a, b = span(8.3, 14.5)
        m.box(a, 1.6, 8.0, b, 14.4, 9.3, {"south": "page", "up": "edge", "down": "edge", "east": "edge", "west": "edge"},
              rot=rot, name="pages")
        for y0 in (0.7, 13.7):
            a, b = span(13.7, 15.2)
            m.box(a, y0, 7.2, b, y0 + 1.6, 8.2, "gold", rot=rot, name="corner")

    half(1)
    half(-1)
    m.box(7.6, 1, 7.5, 8.4, 15, 9.0, "spine", name="spine")
    m.box(7.3, -1.4, 8.6, 8.7, 2.0, 8.9, "ribbon", name="bookmark")
    return m


def generated(texture: str) -> dict:
    return {"parent": "minecraft:item/generated", "textures": {"layer0": tex_ns(texture)}}


def block_item(block: str) -> dict:
    return {"parent": tex_ns(f"block/{block}")}


def build_all(assets: Path):
    out = assets / "models" / "item"
    out.mkdir(parents=True, exist_ok=True)
    for name, cover, pages in (("selarium_codex", "item/codex_cover", "item/book_pages_codex"),
                               ("warding_grimoire", "item/grimoire_cover", "item/book_pages_grimoire")):
        book(cover).save(out / f"{name}_closed.json")
        open_book(cover, pages).save(out / f"{name}_open.json")
        # closed on the ground, in the inventory and in frames; open in the hands
        composite = {
            "loader": "forge:separate_transforms",
            "base": {"parent": tex_ns(f"item/{name}_closed")},
            "perspectives": {view: {"parent": tex_ns(f"item/{name}_open")} for view in PERSPECTIVES},
        }
        (out / f"{name}.json").write_text(json.dumps(composite, indent=2) + "\n", encoding="utf-8")
    # scrolls are flat sprites; the wax seal of a ward scroll is a second layer tinted with the ward's colour
    for name in ("empty_scroll", "attunement_scroll"):
        (out / f"{name}.json").write_text(json.dumps(generated(f"item/{name}"), indent=2) + "\n", encoding="utf-8")
    ward_scroll = generated("item/ward_scroll")
    ward_scroll["textures"]["layer1"] = tex_ns("item/ward_scroll_seal")
    (out / "ward_scroll.json").write_text(json.dumps(ward_scroll, indent=2) + "\n", encoding="utf-8")
    (out / "arcane_sigil.json").write_text(json.dumps(generated("item/arcane_sigil"), indent=2) + "\n", encoding="utf-8")
    (out / "arcane_crystal.json").write_text(json.dumps(generated("item/arcane_crystal"), indent=2) + "\n", encoding="utf-8")
    for kind in ("arcane", "aegis", "vital", "focus", "binding", "echo", "density", "warp", "veil", "chrono"):
        (out / f"basic_{kind}_dust.json").write_text(json.dumps(generated(f"item/basic_{kind}_dust"), indent=2) + "\n", encoding="utf-8")
        if kind != "arcane":
            (out / f"refined_{kind}_dust.json").write_text(json.dumps(generated(f"item/refined_{kind}_dust"), indent=2) + "\n", encoding="utf-8")
    # legacy ids keep the matching basic dust visual
    for legacy, target in (("arcane_dust", "basic_arcane_dust"), ("aegis_dust", "basic_aegis_dust"), ("vital_dust", "basic_vital_dust"),
                           ("focus_dust", "basic_focus_dust"), ("binding_dust", "basic_binding_dust")):
        (out / f"{legacy}.json").write_text(json.dumps(generated(f"item/{target}"), indent=2) + "\n", encoding="utf-8")
