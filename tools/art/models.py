"""A tiny DSL that emits Minecraft block/item model JSON (multi-element cuboid models)."""
from __future__ import annotations

import json
import math
from pathlib import Path

FACES = ("down", "up", "north", "south", "west", "east")

BLOCK_DISPLAY = {
    "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.625, 0.625, 0.625]},
    "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
    "fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [0.5, 0.5, 0.5]},
    "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375, 0.375, 0.375]},
    "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
    "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4, 0.4, 0.4]},
}


def _r(v):
    return int(v) if float(v).is_integer() else round(float(v), 3)


class Model:
    def __init__(self, textures: dict[str, str], particle: str | None = None, display: dict | None = None,
                 ambient_occlusion: bool | None = None, gui_light: str | None = None):
        self.textures = dict(textures)
        if particle:
            self.textures["particle"] = particle if particle.startswith(("#", "selarium:", "minecraft:")) else self.textures[particle]
        self.display = BLOCK_DISPLAY if display is None else display
        self.elements: list[dict] = []
        self.ambient_occlusion = ambient_occlusion
        self.gui_light = gui_light

    # ------------------------------------------------------------------ primitives
    def box(self, x0, y0, z0, x1, y1, z1, tex, *, faces=None, uv=None, rot=None, shade=True, name=None,
            skip=(), glow=False):
        """Add a cuboid.

        tex   : texture variable name for every face, or dict face -> variable.
        uv    : dict face -> [u0, v0, u1, v1] (0..16) overriding the automatic world-aligned mapping.
        rot   : (axis, angle, (ox, oy, oz)) element rotation, angle a multiple of 22.5 in [-45, 45].
        skip  : faces to omit (hidden faces).
        """
        lo = [min(x0, x1), min(y0, y1), min(z0, z1)]
        hi = [max(x0, x1), max(y0, y1), max(z0, z1)]
        el: dict = {"from": [_r(v) for v in lo], "to": [_r(v) for v in hi], "faces": {}}
        if name:
            el["name"] = name
        for face in FACES:
            if face in skip:
                continue
            var = tex.get(face) if isinstance(tex, dict) else tex
            if var is None:
                continue
            entry = {"texture": "#" + var.lstrip("#")}
            if uv and face in uv:
                entry["uv"] = [_r(v) for v in uv[face]]
            if glow:
                # Forge per-face light data: renders the face fullbright (ignored on loaders without it)
                entry["forge_data"] = {"block_light": 15, "sky_light": 15}
            el["faces"][face] = entry
        if rot:
            axis, angle, origin = rot
            el["rotation"] = {"angle": _r(angle), "axis": axis, "origin": [_r(v) for v in origin]}
        if not shade:
            el["shade"] = False
        self.elements.append(el)
        return self

    def octagon(self, cx, cz, y0, y1, r, tex, *, axis="y", skip=(), **kw):
        """Regular octagonal prism of apothem r, built from four rectangles at 0/45/90/135 deg.

        axis "y": vertical prism centred on (cx, cz) spanning y0..y1.
        axis "x"/"z": horizontal prism (used for scroll rollers); then (cx, cz, y0, y1) are reinterpreted:
              axis "x": cx = centre y, cz = centre z, y0..y1 = x range
              axis "z": cx = centre x, cz = centre y, y0..y1 = z range
        The long side of each rectangle spans two opposite octagon faces so the union is exact.
        """
        s = 2.0 * r / (1.0 + math.sqrt(2.0))          # octagon side length
        half_long, half_short = r, s / 2.0
        for k, angle in enumerate((0.0, 45.0, 0.0, 45.0)):
            long_first = k in (0, 1)
            a, b = (half_long, half_short) if long_first else (half_short, half_long)
            faces_skip = tuple(skip)
            if axis == "y":
                origin = (cx, y0, cz)
                lo, hi = (cx - a, y0, cz - b), (cx + a, y1, cz + b)
                if k:
                    faces_skip += ("up", "down")
            elif axis == "x":
                origin = ((y0 + y1) / 2, cx, cz)
                lo, hi = (y0, cx - a, cz - b), (y1, cx + a, cz + b)
                if k:
                    faces_skip += ("east", "west")
            else:  # z
                origin = (cx, cz, (y0 + y1) / 2)
                lo, hi = (cx - a, cz - b, y0), (cx + a, cz + b, y1)
                if k:
                    faces_skip += ("north", "south")
            rot = (axis, angle, origin) if angle else None
            self.box(*lo, *hi, tex, rot=rot, skip=faces_skip, **kw)
        return self

    def shard(self, cx, cz, y0, height, width, tex, tip_tex=None, lean=None, taper=3, octagonal=None, glow=False):
        """A crystal shard: stacked tapering segments plus a pointed tip.

        Vertical shards use exact octagonal prisms; leaning shards (lean=(axis, angle)) use plain boxes
        because a cuboid element can only carry a single rotation.
        """
        tip_tex = tip_tex or tex
        if octagonal is None:
            octagonal = lean is None
        rot = None
        if lean:
            axis, angle = lean
            rot = (axis, angle, (cx, y0, cz))
        seg_h = height / (taper + 1)
        w = width
        y = y0
        for i in range(taper):
            below = ("down",) if i > 0 else ()
            if octagonal:
                self.octagon(cx, cz, y, y + seg_h, w / 2, tex, skip=below, glow=glow)
            else:
                self.box(cx - w / 2, y, cz - w / 2, cx + w / 2, y + seg_h, cz + w / 2, tex, rot=rot, skip=below, glow=glow)
            y += seg_h
            w = max(0.8, w * 0.8)
        if octagonal:
            self.octagon(cx, cz, y, y + seg_h, w / 3.2, tip_tex, skip=("down",), glow=glow)
        else:
            self.box(cx - w / 4, y, cz - w / 4, cx + w / 4, y + seg_h, cz + w / 4, tip_tex, rot=rot, skip=("down",), glow=glow)
        return self

    # ------------------------------------------------------------------ output
    def to_dict(self) -> dict:
        out: dict = {}
        if self.ambient_occlusion is not None:
            out["ambientocclusion"] = self.ambient_occlusion
        if self.gui_light:
            out["gui_light"] = self.gui_light
        out["textures"] = self.textures
        out["elements"] = self.elements
        if self.display:
            out["display"] = self.display
        return out

    def save(self, path: Path):
        path.parent.mkdir(parents=True, exist_ok=True)
        text = json.dumps(self.to_dict(), indent=2)
        path.write_text(text + "\n", encoding="utf-8")


def tex_ns(name: str) -> str:
    return name if ":" in name else f"selarium:{name}"
