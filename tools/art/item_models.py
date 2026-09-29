"""3D item models: books, scrolls, and plain sprite / block item definitions."""
from __future__ import annotations

import json
from pathlib import Path

from .models import BLOCK_DISPLAY, Model, tex_ns

BOOK_DISPLAY = {
    "gui": {"rotation": [22, 205, 0], "translation": [0, -0.5, 0], "scale": [0.95, 0.95, 0.95]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.5, 0.5, 0.5]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.75, 0.75, 0.75]},
    "head": {"rotation": [0, 180, 0], "translation": [0, 13, 7], "scale": [1, 1, 1]},
    "thirdperson_righthand": {"rotation": [0, -30, 0], "translation": [0, 3, 1], "scale": [0.55, 0.55, 0.55]},
    "thirdperson_lefthand": {"rotation": [0, -30, 0], "translation": [0, 3, 1], "scale": [0.55, 0.55, 0.55]},
    "firstperson_righthand": {"rotation": [0, 20, 0], "translation": [-1, 3, 1], "scale": [0.68, 0.68, 0.68]},
    "firstperson_lefthand": {"rotation": [0, 20, 0], "translation": [1, 3, 1], "scale": [0.68, 0.68, 0.68]},
}


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


def scroll(ribbon_tex: str | None, seal: bool) -> Model:
    m = Model({
        "parch": tex_ns("block/parchment"), "wood": tex_ns("block/moon_wood_dark"), "gold": tex_ns("block/gilded_trim"),
        "seal": tex_ns("item/scroll_seal"),
        **({"ribbon": tex_ns(ribbon_tex)} if ribbon_tex else {}),
    }, particle="parch", display=BOOK_DISPLAY)
    m.box(3.2, 3, 7.5, 12.8, 13, 8.5, "parch", name="sheet")
    m.octagon(14, 8, 2.0, 14.0, 1.5, "parch", axis="x", name="roll_top")
    m.octagon(2, 8, 2.0, 14.0, 1.5, "parch", axis="x", name="roll_bottom")
    for x0 in (1.0, 14.0):
        m.box(x0, 12.2, 7.2, x0 + 1.0, 15.8, 8.8, "wood", name="knob_top")
        m.box(x0, 0.2, 7.2, x0 + 1.0, 3.8, 8.8, "wood", name="knob_bottom")
    if ribbon_tex:
        m.box(3.0, 6.0, 7.3, 13.0, 7.6, 8.7, "ribbon", name="ribbon")
    if seal:
        m.box(6.4, 4.2, 8.6, 9.6, 7.4, 9.1, {"north": "seal", "south": "seal", "up": "seal", "down": "seal", "east": "seal", "west": "seal"},
              uv={"north": [0, 0, 16, 16], "south": [0, 0, 16, 16]}, name="seal")
        # tintindex 0 on every seal face so the client can colour it per ward
        for face in m.elements[-1]["faces"].values():
            face["tintindex"] = 0
        m.box(6.4, 4.2, 6.9, 9.6, 7.4, 7.4, {"north": "seal", "south": "seal", "up": "seal", "down": "seal", "east": "seal", "west": "seal"},
              uv={"north": [0, 0, 16, 16], "south": [0, 0, 16, 16]}, name="seal_back")
        for face in m.elements[-1]["faces"].values():
            face["tintindex"] = 0
    return m


def generated(texture: str) -> dict:
    return {"parent": "minecraft:item/generated", "textures": {"layer0": tex_ns(texture)}}


def block_item(block: str) -> dict:
    return {"parent": tex_ns(f"block/{block}")}


def build_all(assets: Path):
    out = assets / "models" / "item"
    out.mkdir(parents=True, exist_ok=True)
    book("item/codex_cover").save(out / "selarium_codex.json")
    book("item/grimoire_cover").save(out / "warding_grimoire.json")
    scroll(None, False).save(out / "empty_scroll.json")
    scroll("item/ribbon_violet", True).save(out / "ward_scroll.json")
    scroll("item/ribbon_cyan", False).save(out / "attunement_scroll.json")
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
