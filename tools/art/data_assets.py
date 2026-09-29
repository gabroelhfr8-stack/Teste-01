"""Small hand-authored model / blockstate JSON files that do not need the DSL."""
from __future__ import annotations

import json
from pathlib import Path


def w(path: Path, data: dict):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


def build_all(assets: Path):
    models = assets / "models" / "block"
    states = assets / "blockstates"
    # temporary ward blocks
    w(models / "temporary_citadel_wall.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "selarium:block/temporary_citadel_wall"}})
    w(models / "tangible_barrier_block.json", {"parent": "minecraft:block/cube_all", "textures": {"all": "selarium:block/tangible_barrier_block"}})
    w(states / "temporary_citadel_wall.json", {"variants": {"": {"model": "selarium:block/temporary_citadel_wall"}}})
    w(states / "tangible_barrier_block.json", {"variants": {"": {"model": "selarium:block/tangible_barrier_block"}}})
    # the sigil block itself is invisible (drawn by a block-entity renderer); only its particle texture matters
    w(models / "arcane_sigil.json", {"textures": {"particle": "selarium:block/ritual_stone"}})
    # grinder: 4 facings x lit
    variants = {}
    for facing, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for lit in ("false", "true"):
            entry = {"model": "selarium:block/arcane_grinder_lit" if lit == "true" else "selarium:block/arcane_grinder"}
            if y:
                entry["y"] = y
            variants[f"facing={facing},lit={lit}"] = entry
    w(states / "arcane_grinder.json", {"variants": variants})
    w(assets / "models" / "item" / "arcane_grinder.json", {"parent": "selarium:block/arcane_grinder"})
    w(assets / "models" / "item" / "mana_tank.json", {"parent": "selarium:block/mana_tank"})
    w(assets / "models" / "item" / "inscription_bench.json", {"parent": "selarium:block/inscription_bench"})
    for stage in ("small", "medium", "large"):
        w(assets / "models" / "item" / f"{stage}_arcane_crystal_bud.json", {"parent": f"selarium:block/{stage}_arcane_crystal_bud"})
    w(assets / "models" / "item" / "arcane_crystal_cluster.json", {"parent": "selarium:block/arcane_crystal_cluster"})
