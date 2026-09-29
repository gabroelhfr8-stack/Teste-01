"""Block models for the three workshop machines and the crystal growth stages."""
from __future__ import annotations

from .models import BLOCK_DISPLAY, Model, tex_ns


def arcane_grinder(lit: bool = False) -> Model:
    m = Model({
        "stone": tex_ns("block/ritual_stone"), "dark": tex_ns("block/ritual_stone_dark"),
        "metal": tex_ns("block/arcane_metal"), "gold": tex_ns("block/gilded_trim"),
        "rune": tex_ns("block/rune_panel_lit" if lit else "block/rune_panel"),
        "cyan": tex_ns("block/crystal_cyan_lit" if lit else "block/crystal_cyan"),
        "violet": tex_ns("block/crystal_violet"),
    }, particle="dark")
    # plinth
    m.box(0, 0, 0, 16, 2, 16, "dark", name="plinth")
    m.box(0.5, 2, 0.5, 15.5, 3, 15.5, "gold", name="plinth_trim")
    # basin walls (front = north)
    m.box(2, 3, 2, 14, 9, 4, {"north": "rune", "south": "stone", "east": "stone", "west": "stone", "up": "stone", "down": "stone"},
          uv={"north": [0, 0, 16, 16]}, name="wall_n")
    m.box(2, 3, 12, 14, 9, 14, "stone", name="wall_s")
    m.box(2, 3, 4, 4, 9, 12, "stone", name="wall_w")
    m.box(12, 3, 4, 14, 9, 12, "stone", name="wall_e")
    # basin floor and grinding stones
    m.box(4, 3, 4, 12, 5, 12, "metal", name="floor")
    m.octagon(8, 8, 5, 6.2, 3.6, "dark", name="lower_stone")
    m.octagon(8, 8, 6.2, 7.4, 2.9, "stone", name="runner_stone")
    m.box(7.3, 5, 7.3, 8.7, 12, 8.7, "metal", name="axle")
    m.box(6.4, 8, 6.4, 9.6, 8.8, 9.6, "gold", name="axle_collar")
    # focus crystals
    m.shard(8, 8, 12, 3.6, 2.6, "cyan", lean=None, taper=2, glow=True)
    m.shard(6.2, 8.2, 12, 2.2, 1.4, "violet", lean=("z", 22.5), taper=1)
    m.shard(9.8, 7.8, 12, 2.2, 1.4, "violet", lean=("z", -22.5), taper=1)
    # corner pillars with gilded caps
    for (x, z) in ((1, 1), (13, 1), (1, 13), (13, 13)):
        m.box(x, 3, z, x + 2, 11, z + 2, "stone", name="pillar")
        m.box(x - 0.5, 11, z - 0.5, x + 2.5, 12, z + 2.5, "gold", name="pillar_cap")
    # front output tray and lip
    m.box(5, 3, 0, 11, 4, 2, "metal", name="tray")
    m.box(4.5, 4, 0, 5.5, 5, 1.5, "gold", name="tray_lip_l")
    m.box(10.5, 4, 0, 11.5, 5, 1.5, "gold", name="tray_lip_r")
    # side crank (east)
    m.box(14, 6.4, 7.4, 15.4, 7.6, 8.6, "metal", name="crank_axle")
    m.box(14.8, 6.8, 7.4, 15.8, 11.4, 8.4, "gold", name="crank_arm")
    m.box(14.4, 10.6, 6.6, 16, 12, 9.2, "cyan", name="crank_knob", glow=True)
    return m


def mana_tank() -> Model:
    m = Model({
        "metal": tex_ns("block/arcane_metal"), "gold": tex_ns("block/gilded_trim"),
        "glass": tex_ns("block/glass_cyan"), "gauge": tex_ns("block/gauge"), "cyan": tex_ns("block/crystal_cyan"),
        "violet": tex_ns("block/crystal_violet"),
    }, particle="metal")
    m.box(1, 0, 1, 15, 2, 15, "metal", name="foot")
    m.box(2, 2, 2, 14, 4, 14, "gold", name="base_ring")
    m.box(3, 4, 4, 13, 13, 12, "glass", name="glass_x")
    m.box(4, 4, 3, 12, 13, 13, "glass", name="glass_z")
    for (x, z) in ((2.5, 2.5), (12, 2.5), (2.5, 12), (12, 12)):
        m.box(x, 3, z, x + 1.5, 14, z + 1.5, "metal", name="post")
    m.box(2.5, 12.6, 2.5, 13.5, 14, 13.5, "gold", name="top_ring")
    m.box(3.5, 14, 3.5, 12.5, 15, 12.5, "metal", name="cap")
    m.box(7, 15, 7, 9, 16, 9, "gold", name="valve")
    # front gauge plate (north)
    m.box(6.5, 4.5, 1.5, 9.5, 12.5, 2.5, {"north": "gauge", "south": "metal", "east": "metal", "west": "metal", "up": "metal", "down": "metal"},
          uv={"north": [0, 0, 16, 16]}, name="gauge")
    # crystal feet
    for (x, z) in ((1.2, 1.2), (13.6, 1.2), (1.2, 13.6), (13.6, 13.6)):
        m.box(x, 0, z, x + 1.2, 1, z + 1.2, "violet", name="foot_gem")
    return m


def inscription_bench() -> Model:
    m = Model({
        "wood": tex_ns("block/arcane_planks"), "dark": tex_ns("block/moon_wood_dark"),
        "gold": tex_ns("block/gilded_trim"), "leather": tex_ns("block/leather_inlay"),
        "cover": tex_ns("block/book_cover_violet"), "pages": tex_ns("block/pages_side"),
        "parch": tex_ns("block/parchment"), "ink": tex_ns("block/ink"), "cyan": tex_ns("block/crystal_cyan"),
        "violet": tex_ns("block/crystal_violet"), "glass": tex_ns("block/glass_cyan"), "metal": tex_ns("block/arcane_metal"),
    }, particle="wood")
    # legs and stretchers
    for (x, z) in ((0.5, 0.5), (13.5, 0.5), (0.5, 13.5), (13.5, 13.5)):
        m.box(x, 0, z, x + 2, 10, z + 2, "dark", name="leg")
    m.box(2.5, 2.5, 1.0, 13.5, 3.5, 2.0, "dark", name="brace_n")
    m.box(2.5, 2.5, 14.0, 13.5, 3.5, 15.0, "dark", name="brace_s")
    # drawer cabinet
    m.box(3, 3.5, 2.5, 13, 9.5, 13.5, {"north": "wood", "south": "wood", "east": "wood", "west": "wood", "up": "wood", "down": "wood"}, name="cabinet")
    m.box(4, 4.5, 2.2, 12, 6.2, 2.5, "dark", name="drawer_a")
    m.box(4, 6.9, 2.2, 12, 8.6, 2.5, "dark", name="drawer_b")
    m.box(7, 5.0, 1.8, 9, 5.7, 2.3, "gold", name="handle_a")
    m.box(7, 7.4, 1.8, 9, 8.1, 2.3, "gold", name="handle_b")
    # desk top with leather inlay
    m.box(0, 10, 0, 16, 11, 16, {"up": "wood", "down": "wood", "north": "wood", "south": "wood", "east": "wood", "west": "wood"}, name="desk")
    m.box(0.5, 10, 0.5, 15.5, 11, 15.5, "wood", name="desk_shadow", skip=("up", "down"))
    m.box(1.5, 11, 1.5, 14.5, 11.4, 14.5, {"up": "leather", "north": "gold", "south": "gold", "east": "gold", "west": "gold"},
          uv={"up": [0, 0, 16, 16]}, name="blotter")
    # slanted reading stand with open book
    origin = (8, 11.4, 8)
    m.box(3, 11.4, 6, 13, 12.2, 12, "dark", rot=("x", -22.5, origin), name="stand")
    m.box(2.6, 12.2, 5.6, 13.4, 12.8, 12.4, {"up": "cover", "down": "cover", "north": "cover", "south": "cover", "east": "cover", "west": "cover"},
          rot=("x", -22.5, origin), uv={"up": [0, 0, 16, 16]}, name="book_cover")
    m.box(3.2, 12.8, 6.0, 7.9, 13.4, 12.0, {"up": "parch", "north": "pages", "south": "pages", "west": "pages", "east": "pages"},
          rot=("x", -22.5, origin), uv={"up": [0, 0, 16, 16]}, name="page_l")
    m.box(8.1, 12.8, 6.0, 12.8, 13.4, 12.0, {"up": "parch", "north": "pages", "south": "pages", "west": "pages", "east": "pages"},
          rot=("x", -22.5, origin), uv={"up": [0, 0, 16, 16]}, name="page_r")
    # inkwell and quill
    m.box(12, 11.4, 2.4, 14.6, 13.2, 5, "glass", name="inkwell")
    m.box(12.4, 11.4, 2.8, 14.2, 12.6, 4.6, "ink", name="ink")
    m.box(12.8, 13.2, 3.0, 13.8, 13.8, 4.0, "gold", name="ink_cap")
    m.box(13.1, 13.8, 3.3, 13.5, 16, 3.7, "parch", rot=("z", 22.5, (13.3, 13.8, 3.5)), name="quill")
    # lamp crystal, west corner
    m.shard(3, 3.4, 11.4, 4.0, 2.4, "violet", lean=("z", 22.5), taper=2)
    m.shard(4.8, 2.8, 11.4, 2.6, 1.6, "cyan", lean=("x", -22.5), taper=1)
    # scroll rack
    m.box(0.2, 5, 4, 1.4, 6, 12, "dark", name="rack")
    for k, z in enumerate((4.6, 7.0, 9.4)):
        m.box(0.0, 6, z, 1.6, 8, z + 2, "parch", name="scroll")
    return m


CRYSTAL_GUI = {  # (scale, y translation) so every growth stage is centred and readable in the inventory
    "small": (0.95, 4.75), "medium": (0.82, 2.9), "large": (0.66, 1.0), "cluster": (0.6, 0.0),
}


def _crystal_display(stage: str) -> dict:
    scale, ty = CRYSTAL_GUI[stage]
    display = {k: dict(v) for k, v in BLOCK_DISPLAY.items()}
    display["gui"] = {"rotation": [30, 225, 0], "translation": [0, ty, 0], "scale": [scale, scale, scale]}
    display["ground"] = {"rotation": [0, 0, 0], "translation": [0, 2, 0], "scale": [0.3, 0.3, 0.3]}
    return display


def crystal_stage(stage: str) -> Model:
    m = Model({
        "violet": tex_ns("block/crystal_violet"), "cyan": tex_ns("block/crystal_cyan"),
        "rock": tex_ns("block/arcane_geode_stone"),
    }, particle="violet", display=_crystal_display(stage))
    cfg = {
        "small": [(8, 8, 3.2, 2.6, ("z", 0), "violet")],
        "medium": [(8, 8, 6.5, 3.2, ("z", 0), "violet"), (5.2, 9.5, 3.6, 2.2, ("z", -22.5), "cyan")],
        "large": [(8, 8, 9.5, 3.8, ("z", 0), "violet"), (4.8, 9.6, 5.4, 2.6, ("z", -22.5), "cyan"),
                  (11.4, 6.6, 5.0, 2.6, ("z", 22.5), "violet")],
        "cluster": [(8, 8, 12.5, 4.2, ("z", 0), "violet"), (4.2, 9.2, 8.0, 3.0, ("z", -22.5), "cyan"),
                    (11.8, 6.4, 7.0, 3.0, ("z", 22.5), "violet"), (9.6, 11.6, 5.6, 2.4, ("x", 22.5), "cyan"),
                    (5.6, 4.6, 5.2, 2.4, ("x", -22.5), "violet")],
    }[stage]
    base = {"small": (5, 5, 11, 11, 1.6), "medium": (4, 4, 12, 12, 2), "large": (3, 3, 13, 13, 2.4), "cluster": (2, 2, 14, 14, 3)}[stage]
    m.box(base[0], 0, base[1], base[2], base[4], base[3], "rock", name="base")
    for (cx, cz, h, w, lean, tex) in cfg:
        m.shard(cx, cz, base[4] - 0.4, h, w, tex, lean=lean if lean[1] else None, taper=3 if h > 6 else 2)
    return m


def build_all(assets):
    out = assets / "models" / "block"
    arcane_grinder().save(out / "arcane_grinder.json")
    arcane_grinder(lit=True).save(out / "arcane_grinder_lit.json")
    mana_tank().save(out / "mana_tank.json")
    inscription_bench().save(out / "inscription_bench.json")
    for stage in ("small", "medium", "large"):
        crystal_stage(stage).save(out / f"{stage}_arcane_crystal_bud.json")
    crystal_stage("cluster").save(out / "arcane_crystal_cluster.json")
