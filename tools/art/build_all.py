#!/usr/bin/env python3
"""Regenerate every procedurally generated Selarium asset.

    python3 tools/art/build_all.py            # from the repository root

Requires: pip install pillow numpy
"""
from __future__ import annotations

import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[2]))

from tools.art import blocks, books, covers, data_assets, item_models, items, machines, materials, pix, px, sigil, vfx, walls  # noqa: E402


def main() -> None:
    assets = px.ASSETS
    steps = [
        ("block textures", blocks.build_all), ("materials", materials.build_all), ("ward walls", walls.build_all),
        ("machine + crystal models", machines.build_all), ("covers / ribbons", covers.build_all), ("open books / scrolls", books.build_all),
        ("item sprites", items.build_all), ("item models", item_models.build_all), ("misc model json", data_assets.build_all),
        ("sigil glyphs", sigil.build_all), ("pixel effects", pix.build_all), ("mana fluid", vfx.build_all),
    ]
    for label, fn in steps:
        fn(assets)
        print(f"  ok  {label}")
    print("done")


if __name__ == "__main__":
    main()
