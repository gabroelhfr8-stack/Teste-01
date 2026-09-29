#!/usr/bin/env python3
"""Static validation of Selarium assets and data.

Runs with the standard library only (no Minecraft, Gradle or Pillow needed) so it can
gate every push in CI.  It replaces the old PowerShell validator and adds checks for
model geometry, particles, sounds, recipes, loot tables and translations.

Usage:  python3 tools/validate_assets.py [--warnings]
"""
from __future__ import annotations

import json
import re
import struct
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src" / "main" / "resources"
ASSETS = RES / "assets" / "selarium"
DATA = RES / "data" / "selarium"
JAVA = ROOT / "src" / "main" / "java" / "com" / "seleris" / "selarium"
NS = "selarium"

VALID_FACES = {"north", "south", "east", "west", "up", "down"}
VALID_ROTATIONS = {-45.0, -22.5, 0.0, 22.5, 45.0}
# Texture keys defined by the vanilla parents we are allowed to inherit from.
VANILLA_PARENT_KEYS = {
    "block/block": set(),
    "minecraft:block/block": set(),
    "block/cube_all": {"all", "particle"},
    "minecraft:block/cube_all": {"all", "particle"},
    "block/cube_column": {"end", "side", "particle"},
    "minecraft:block/cube_column": {"end", "side", "particle"},
    "block/cross": {"cross", "particle"},
    "minecraft:block/cross": {"cross", "particle"},
    "block/leaves": {"all", "particle"},
    "minecraft:block/leaves": {"all", "particle"},
    "item/generated": {"layer0", "layer1", "layer2", "particle"},
    "minecraft:item/generated": {"layer0", "layer1", "layer2", "particle"},
    "item/handheld": {"layer0", "layer1", "particle"},
    "minecraft:item/handheld": {"layer0", "layer1", "particle"},
}

errors: list[str] = []
warnings: list[str] = []
stats: dict[str, int] = {}


def err(msg: str) -> None:
    errors.append(msg)


def warn(msg: str) -> None:
    warnings.append(msg)


def rel(path: Path) -> str:
    return str(path.relative_to(ROOT))


def load_json(path: Path):
    try:
        return json.loads(path.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        err(f"{rel(path)}: invalid JSON ({exc})")
        return None


def png_size(path: Path) -> tuple[int, int] | None:
    try:
        with path.open("rb") as handle:
            head = handle.read(24)
        if head[:8] != b"\x89PNG\r\n\x1a\n":
            err(f"{rel(path)}: not a PNG file")
            return None
        return struct.unpack(">II", head[16:24])
    except OSError as exc:
        err(f"{rel(path)}: unreadable ({exc})")
        return None


def split_ref(ref: str) -> tuple[str, str]:
    if ":" in ref:
        namespace, path = ref.split(":", 1)
        return namespace, path
    return "minecraft", ref


# --------------------------------------------------------------------------- JSON
def check_all_json() -> dict[Path, object]:
    docs: dict[Path, object] = {}
    for path in sorted(RES.rglob("*.json")) + sorted(RES.rglob("*.mcmeta")):
        doc = load_json(path)
        if doc is not None:
            docs[path] = doc
    stats["json files"] = len(docs)
    return docs


# ------------------------------------------------------------------------- models
def model_path(ref: str) -> Path | None:
    namespace, path = split_ref(ref)
    if namespace != NS:
        return None
    return ASSETS / "models" / f"{path}.json"


def texture_path(ref: str) -> Path | None:
    namespace, path = split_ref(ref)
    if namespace != NS:
        return None
    return ASSETS / "textures" / f"{path}.png"


def merged_texture_keys(doc: dict, seen: set[str] | None = None) -> set[str]:
    """Texture variables visible to a model, following selarium parents."""
    seen = seen or set()
    keys = set(doc.get("textures", {}))
    parent = doc.get("parent")
    if parent:
        if parent in VANILLA_PARENT_KEYS:
            keys |= VANILLA_PARENT_KEYS[parent]
        else:
            path = model_path(parent)
            if path and path.exists() and str(path) not in seen:
                seen.add(str(path))
                parent_doc = load_json(path)
                if isinstance(parent_doc, dict):
                    keys |= merged_texture_keys(parent_doc, seen)
    return keys


def check_models(docs: dict[Path, object]) -> set[str]:
    used_textures: set[str] = set()
    count = 0
    for path, doc in docs.items():
        if "models" not in path.parts or not isinstance(doc, dict):
            continue
        count += 1
        name = rel(path)
        parent = doc.get("parent")
        if parent:
            target = model_path(parent)
            if target is not None and not target.exists():
                err(f"{name}: missing parent model {parent}")
            elif target is None and parent not in VANILLA_PARENT_KEYS:
                warn(f"{name}: unverified vanilla parent {parent}")
        keys = merged_texture_keys(doc)
        for var, ref in doc.get("textures", {}).items():
            if isinstance(ref, str) and ref.startswith("#"):
                if ref[1:] not in keys:
                    err(f"{name}: texture variable {var} -> {ref} is undefined")
                continue
            target = texture_path(ref)
            if target is None:
                if not str(ref).startswith("minecraft:"):
                    warn(f"{name}: foreign texture {ref}")
                continue
            used_textures.add(str(target))
            if not target.exists():
                err(f"{name}: missing texture {ref}")
        for index, element in enumerate(doc.get("elements", [])):
            where = f"{name} element {index}"
            lo, hi = element.get("from"), element.get("to")
            if not (isinstance(lo, list) and isinstance(hi, list) and len(lo) == 3 and len(hi) == 3):
                err(f"{where}: from/to must be 3-vectors")
                continue
            for a, b in zip(lo, hi):
                if not (-16 <= a <= 32 and -16 <= b <= 32):
                    err(f"{where}: coordinate out of [-16, 32]")
                    break
            rotation = element.get("rotation")
            if rotation:
                if float(rotation.get("angle", 0)) not in VALID_ROTATIONS:
                    err(f"{where}: rotation angle {rotation.get('angle')} is not a multiple of 22.5 in [-45, 45]")
                if rotation.get("axis") not in {"x", "y", "z"}:
                    err(f"{where}: bad rotation axis")
            faces = element.get("faces", {})
            if not faces:
                err(f"{where}: no faces")
            for face_name, face in faces.items():
                if face_name not in VALID_FACES:
                    err(f"{where}: bad face {face_name}")
                    continue
                tex = face.get("texture", "")
                if not tex.startswith("#"):
                    err(f"{where}: face {face_name} must reference a #variable")
                elif tex[1:] not in keys:
                    err(f"{where}: face {face_name} uses undefined #{tex[1:]}")
                uv = face.get("uv")
                if uv is not None and (len(uv) != 4 or any(not (0 <= value <= 16) for value in uv)):
                    err(f"{where}: face {face_name} uv out of [0, 16]")
    stats["models"] = count
    return used_textures


def check_blockstates(docs: dict[Path, object]) -> None:
    count = 0
    for path, doc in docs.items():
        if "blockstates" not in path.parts or not isinstance(doc, dict):
            continue
        count += 1

        def visit(node):
            if isinstance(node, dict):
                model = node.get("model")
                if isinstance(model, str):
                    target = model_path(model)
                    if target is not None and not target.exists():
                        err(f"{rel(path)}: missing model {model}")
                for value in node.values():
                    visit(value)
            elif isinstance(node, list):
                for value in node:
                    visit(value)

        visit(doc)
    stats["blockstates"] = count


# --------------------------------------------------------------------- registries
def read(path: Path) -> str:
    return path.read_text(encoding="utf-8") if path.exists() else ""


def registered_blocks() -> list[str]:
    return re.findall(r'BLOCKS\.register\(\s*"([a-z0-9_]+)"', read(JAVA / "registry" / "SelariumBlocks.java"))


def registered_items() -> list[str]:
    text = read(JAVA / "registry" / "SelariumItems.java")
    names = re.findall(r'(?:ITEMS\.register|registerDust|registerLegacyDust|registerBlockItem)\(\s*"([a-z0-9_]+)"', text)
    return sorted(set(names))


def enum_names(java_file: str, pattern: str = r'\b[A-Z][A-Z_]+\("([a-z_]+)"') -> list[str]:
    return re.findall(pattern, read(JAVA / java_file))


def check_registry_assets(en: dict, used_textures: set[str]) -> None:
    blocks = registered_blocks()
    items = registered_items()
    stats["registered blocks"] = len(blocks)
    stats["registered items"] = len(items)
    if not blocks or not items:
        err("could not parse registered blocks/items from Java sources")
        return
    for name in blocks:
        if not (ASSETS / "blockstates" / f"{name}.json").exists():
            err(f"block {name}: missing blockstate")
        if f"block.{NS}.{name}" not in en:
            err(f"block {name}: missing lang key block.{NS}.{name}")
    for name in items:
        if not (ASSETS / "models" / "item" / f"{name}.json").exists():
            err(f"item {name}: missing item model")
        key = f"block.{NS}.{name}" if name in blocks else f"item.{NS}.{name}"
        if key not in en:
            err(f"item {name}: missing lang key {key}")


def check_loot_tables(blocks: list[str]) -> None:
    no_loot = {"temporary_citadel_wall", "tangible_barrier_block"}
    for name in blocks:
        if name in no_loot:
            continue
        if not (DATA / "loot_tables" / "blocks" / f"{name}.json").exists():
            err(f"block {name}: missing loot table")


def check_recipes(items: list[str]) -> None:
    known = set(items) | set(registered_blocks())
    count = 0
    for path in sorted((DATA / "recipes").rglob("*.json")):
        doc = load_json(path)
        if not isinstance(doc, dict):
            continue
        count += 1
        if "type" not in doc:
            err(f"{rel(path)}: recipe has no type")

        def visit(node):
            if isinstance(node, dict):
                for key in ("item", "result"):
                    value = node.get(key)
                    if isinstance(value, str) and value.startswith(NS + ":"):
                        if value.split(":", 1)[1] not in known:
                            err(f"{rel(path)}: unknown item {value}")
                    elif isinstance(value, dict):
                        visit(value)
                for value in node.values():
                    visit(value)
            elif isinstance(node, list):
                for value in node:
                    visit(value)

        visit(doc)
    stats["recipes"] = count


# ------------------------------------------------------------------- particles etc
def check_particles(used_textures: set[str]) -> None:
    folder = ASSETS / "particles"
    if not folder.exists():
        return
    count = 0
    for path in sorted(folder.glob("*.json")):
        doc = load_json(path)
        if not isinstance(doc, dict):
            continue
        count += 1
        for ref in doc.get("textures", []):
            namespace, name = split_ref(ref)
            if namespace != NS:
                continue
            target = ASSETS / "textures" / "particle" / f"{name}.png"
            used_textures.add(str(target))
            if not target.exists():
                err(f"{rel(path)}: missing particle texture {ref}")
    stats["particle descriptors"] = count


def check_sounds() -> None:
    path = ASSETS / "sounds.json"
    doc = load_json(path)
    if not isinstance(doc, dict):
        return
    for event, body in doc.items():
        for sound in body.get("sounds", []):
            name = sound["name"] if isinstance(sound, dict) else sound
            kind = sound.get("type", "file") if isinstance(sound, dict) else "file"
            namespace, rest = split_ref(name)
            if kind == "file" and namespace == NS:
                if not (ASSETS / "sounds" / f"{rest}.ogg").exists():
                    err(f"sounds.json: {event} -> missing file {name}")
        subtitle = body.get("subtitle")
        if subtitle and subtitle not in EN_KEYS:
            err(f"sounds.json: {event} subtitle key {subtitle} missing from en_us")
    stats["sound events"] = len(doc)


def check_textures(used_textures: set[str]) -> None:
    count = 0
    for path in sorted((ASSETS / "textures").rglob("*.png")):
        count += 1
        size = png_size(path)
        meta = path.with_suffix(".png.mcmeta")
        if meta.exists() and size is not None:
            doc = load_json(meta)
            anim = doc.get("animation") if isinstance(doc, dict) else None
            if anim is not None and size[1] % size[0] != 0:
                err(f"{rel(path)}: animated texture height must be a multiple of width")
    stats["textures"] = count


# ------------------------------------------------------------------------- lang
EN_KEYS: dict = {}


def check_lang() -> dict:
    global EN_KEYS
    en = load_json(ASSETS / "lang" / "en_us.json") or {}
    pt = load_json(ASSETS / "lang" / "pt_br.json") or {}
    EN_KEYS = en
    for key in sorted(set(pt) - set(en)):
        err(f"lang: key missing in en_us: {key}")
    for key in sorted(set(en) - set(pt)):
        err(f"lang: key missing in pt_br: {key}")
    for key, value in en.items():
        if not isinstance(value, str):
            err(f"lang: en_us {key} is not a string")
    # Every literal translation key used from Java must exist.
    used = set()
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        # Only complete literals: "a.b.c" followed by ',' or ')' (not "prefix." + suffix).
        for match in re.finditer(r'(?:translatable|translatableWithFallback)\(\s*"([A-Za-z0-9_.]+)"\s*[,)]', text):
            if NS in match.group(1):  # vanilla keys such as gui.done are not ours to check
                used.add((match.group(1), path))
    for key, path in sorted(used, key=lambda pair: pair[0]):
        if key not in en:
            err(f"{rel(path)}: translation key {key} missing from en_us")
    # Generated keys.
    for ward in enum_names("ward/WardType.java"):
        if f"ward.{NS}.{ward}" not in en:
            err(f"lang: missing ward.{NS}.{ward}")
    dusts = enum_names("dust/DustType.java")
    for purity in enum_names("dust/DustPurity.java", r'\b[A-Z][A-Z_]+\("([a-z_]+)",'):
        for dust in dusts:
            if purity == "refined" and dust == "arcane":
                continue
            if f"dust.{NS}.{purity}_{dust}" not in en:
                err(f"lang: missing dust.{NS}.{purity}_{dust}")
    stats["translations"] = len(en)
    return en


def check_advancements(items: list[str], en: dict, used_textures: set[str]) -> None:
    """Icons, titles, parents and backgrounds of the advancement tree."""
    folder = DATA / "advancements"
    if not folder.is_dir():
        return
    known = set(items) | set(registered_blocks())
    ids = {path.stem for path in folder.glob("*.json")}
    count = 0
    for path in sorted(folder.glob("*.json")):
        doc = load_json(path)
        if not isinstance(doc, dict):
            continue
        count += 1
        display = doc.get("display", {})
        icon = display.get("icon", {}).get("item", "")
        namespace, name = split_ref(icon)
        if namespace == NS and name not in known:
            err(f"{rel(path)}: unknown icon item {icon}")
        for field in ("title", "description"):
            key = display.get(field, {}).get("translate", "")
            if key not in en:
                err(f"{rel(path)}: {field} translation key '{key}' is missing")
        parent = doc.get("parent")
        if parent:
            namespace, name = split_ref(parent)
            if namespace == NS and name not in ids:
                err(f"{rel(path)}: unknown parent {parent}")
        elif path.stem != "root":
            err(f"{rel(path)}: only root.json may lack a parent")
        background = display.get("background")
        target = namespace_path(background) if background else None
        if target is not None:
            used_textures.add(str(target))
            if not target.exists():
                err(f"{rel(path)}: missing background {background}")
        if "criteria" not in doc or not doc["criteria"]:
            err(f"{rel(path)}: no criteria")
        for criterion in doc.get("criteria", {}).values():
            for item in criterion.get("conditions", {}).get("items", []):
                for ref in item.get("items", []):
                    namespace, name = split_ref(ref)
                    if namespace == NS and name not in known:
                        err(f"{rel(path)}: criterion references unknown item {ref}")
    stats["advancements"] = count


def namespace_path(ref: str) -> Path | None:
    """Resource path of a `selarium:...` reference that already includes the folder (e.g. textures/...)."""
    namespace, path = split_ref(ref)
    return ASSETS / path if namespace == NS else None


def check_versions() -> None:
    gradle = read(ROOT / "build.gradle")
    toml = read(RES / "META-INF" / "mods.toml")
    a = re.search(r"^version\s*=\s*'([^']+)'", gradle, re.M)
    b = re.search(r'^version="([^"]+)"', toml, re.M)
    if a and b and a.group(1) != b.group(1):
        err(f"version mismatch: build.gradle {a.group(1)} vs mods.toml {b.group(1)}")


def check_dynamic_texture_references(used_textures: set[str]) -> None:
    """Textures referenced only from Java (renderers, GUI, particles).

    Three spellings are understood: a full literal ("textures/vfx/x.png"), a helper call with a
    path relative to textures/ (tex("vfx/x.png"), texture("vfx/x.png")) and a computed name
    ("vfx/sigil/glyph/" + name + ".png"), which marks every texture in that folder as used.
    """
    textures = ASSETS / "textures"
    for path in JAVA.rglob("*.java"):
        text = path.read_text(encoding="utf-8")
        literals = [m.group(1) for m in re.finditer(r'"(textures/[a-z0-9_/]+\.png)"', text)]
        literals += ["textures/" + m.group(1) for m in re.finditer(r'\b(?:tex|texture)\("([a-z0-9_/]+\.png)"\)', text)]
        for name in literals:
            target = ASSETS / name
            used_textures.add(str(target))
            if not target.exists():
                err(f"{rel(path)}: missing texture {name}")
        for m in re.finditer(r'"((?:textures/)?[a-z0-9_]+(?:/[a-z0-9_]+)*/)"\s*\+[^;]*?"\.png"', text):
            folder = textures / m.group(1).removeprefix("textures/")
            if not folder.is_dir():
                err(f"{rel(path)}: computed texture folder {m.group(1)} does not exist")
                continue
            used_textures.update(str(png) for png in folder.glob("*.png"))


def report_unused(used_textures: set[str]) -> None:
    for path in sorted((ASSETS / "textures").rglob("*.png")):
        if str(path) not in used_textures:
            warn(f"unused texture: {rel(path)}")


def main() -> int:
    show_warnings = "--warnings" in sys.argv
    docs = check_all_json()
    used_textures = check_models(docs)
    check_blockstates(docs)
    en = check_lang()
    check_registry_assets(en, used_textures)
    check_loot_tables(registered_blocks())
    check_recipes(registered_items())
    check_advancements(registered_items(), en, used_textures)
    check_particles(used_textures)
    check_sounds()
    check_textures(used_textures)
    check_dynamic_texture_references(used_textures)
    check_versions()
    report_unused(used_textures)

    summary = ", ".join(f"{value} {key}" for key, value in stats.items())
    print(f"[validate_assets] {summary}")
    if show_warnings:
        for message in warnings:
            print(f"  warning: {message}")
    else:
        print(f"[validate_assets] {len(warnings)} warnings (run with --warnings to list)")
    if errors:
        for message in sorted(set(errors)):
            print(f"  ERROR: {message}", file=sys.stderr)
        print(f"[validate_assets] FAILED with {len(set(errors))} error(s)", file=sys.stderr)
        return 1
    print("[validate_assets] OK - all references resolved")
    return 0


if __name__ == "__main__":
    sys.exit(main())
