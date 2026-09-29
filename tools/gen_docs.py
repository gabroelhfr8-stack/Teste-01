#!/usr/bin/env python3
"""Generate docs/WARDS.md from the Java sources and the pt_br translations.

The table is derived from WardDefinitions (dust requirements), WardStyles (category) and the
default values in SelariumCommonConfig, so it can never drift from the code.

    python3 tools/gen_docs.py
"""
from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
JAVA = ROOT / "src" / "main" / "java" / "com" / "seleris" / "selarium"
LANG = ROOT / "src" / "main" / "resources" / "assets" / "selarium" / "lang" / "pt_br.json"
OUT = ROOT / "docs" / "WARDS.md"

CATEGORY_TITLES = {
    "SOURCE": "Fonte de mana",
    "DETECTION": "Detecção",
    "BUFF": "Benefícios para aliados",
    "UTILITY": "Utilidades",
    "HOSTILE": "Efeitos contra invasores",
    "STRUCTURE": "Estruturas e proteção de área",
    "EVENT": "Reações a eventos",
}
DUST_PT = {"ARCANE": "Arcana", "AEGIS": "Égide", "VITAL": "Vital", "FOCUS": "Foco", "BINDING": "Vínculo",
           "ECHO": "Eco", "DENSITY": "Densidade", "WARP": "Dobra", "VEIL": "Véu", "CHRONO": "Crono"}


def ward_types() -> list[str]:
    text = (JAVA / "ward" / "WardType.java").read_text(encoding="utf-8")
    return [m for m in re.findall(r'^\s+([A-Z_]+)\("[a-z_]+"\)', text, re.M) if m != "NONE"]


def requirements() -> dict[str, tuple[str, list[tuple[str, int]]]]:
    """ward -> (tier, [(dust, count)])"""
    text = (JAVA / "ward" / "WardDefinitions.java").read_text(encoding="utf-8")
    result = {}
    # mvp(WardType.X, WardTier.T, 340, req(...), req(...))
    for m in re.finditer(r"mvp\(WardType\.([A-Z_]+),\s*WardTier\.([A-Z]+),\s*\d+,(.*?)\),?\n", text, re.S):
        reqs = [(d, int(c) if c else 1) for d, c in re.findall(r"req\(DustType\.([A-Z]+)(?:,\s*(\d+))?\)", m.group(3))]
        result[m.group(1)] = (m.group(2), reqs)
    # definition(WardType.X, WardTier.T, prio, List.of(req(...)...), ...)
    for m in re.finditer(r"definition\(\s*WardType\.([A-Z_]+),\s*WardTier\.([A-Z]+),\s*\d+,\s*List\.of\((.*?)\),\s*\(\)", text, re.S):
        reqs = [(d, int(c) if c else 1) for d, c in re.findall(r"req\(DustType\.([A-Z]+)(?:,\s*(\d+))?\)", m.group(3))]
        result[m.group(1)] = (m.group(2), reqs)
    return result


def categories() -> dict[str, str]:
    text = (JAVA / "ward" / "WardStyles.java").read_text(encoding="utf-8")
    return dict(re.findall(r"put\(WardType\.([A-Z_]+),\s*WardCategory\.([A-Z]+),", text))


def config_defaults() -> dict[str, dict[str, int]]:
    text = (JAVA / "config" / "SelariumCommonConfig.java").read_text(encoding="utf-8")
    out: dict[str, dict[str, int]] = {}
    for m in re.finditer(r'mvpWardConfigs\.put\(WardType\.([A-Z_]+),\s*defineMvpWard\(builder,\s*"[a-z_]+",\s*(\d+),\s*(\d+),\s*(\d+),\s*(\d+),\s*(\d+),', text):
        out[m.group(1)] = {"range": int(m.group(2)), "interval": int(m.group(3)), "duration": int(m.group(4)),
                           "cooldown": int(m.group(5)), "mana": int(m.group(6))}
    for ward in ("whispering", "spectral", "bulwark", "rejuvenation", "featherweight", "grounding", "magnetism",
                 "banishment", "eclipse", "ambient"):
        entry = {}
        for key, name in (("range", "Range"), ("interval", "TickInterval"), ("duration", "DurationTicks"), ("cooldown", "CooldownTicks")):
            m = re.search(rf'"{ward}Ward{name}",\s*(\d+)', text)
            if m:
                entry[key] = int(m.group(1))
        m = re.search(rf'"{ward}WardManaCost\w*",\s*(\d+)', text)
        if m:
            entry["mana"] = int(m.group(1))
        m = re.search(r'"ambientWardActivationCost",\s*(\d+)', text) if ward == "ambient" else None
        if m:
            entry["activation"] = int(m.group(1))
        if ward == "ambient":
            entry.setdefault("mana", 0)
        out["AMBIENT_MANA" if ward == "ambient" else ward.upper()] = entry
    return out


def seconds(ticks: int | None) -> str:
    if ticks is None:
        return "—"
    return f"{ticks / 20:g}s"


def main() -> None:
    lang = json.loads(LANG.read_text(encoding="utf-8"))
    types = ward_types()
    reqs = requirements()
    cats = categories()
    cfg = config_defaults()

    lines = [
        "# Referência das proteções (Wards)",
        "",
        "> Gerado automaticamente por `tools/gen_docs.py` a partir do código-fonte — não edite à mão.",
        "",
        "Cada proteção é resolvida quando o sigilo contém **todas** as poeiras exigidas. Proteções *refinadas* exigem "
        "poeiras **refinadas** (exceto a Arcana). Os valores abaixo são os padrões da configuração `selarium-common.toml`; "
        "o servidor pode alterá-los.",
        "",
        f"Total: **{len(types)} proteções**. O alcance é um raio **esférico** em blocos, o mesmo volume desenhado pela casca "
        "translúcida do campo.",
        "",
    ]
    for category, title in CATEGORY_TITLES.items():
        members = [t for t in types if cats.get(t) == category]
        if not members:
            continue
        lines += [f"## {title}", "", "| Proteção | Poeiras necessárias | Alcance | Ciclo | Custo/ciclo | Duração | Recarga |",
                  "|---|---|---:|---:|---:|---:|---:|"]
        for ward in members:
            name = lang.get(f"ward.selarium.{ward.lower()}", ward)
            tier, dusts = reqs.get(ward, ("BASIC", []))
            parts = []
            for dust, count in dusts:
                label = DUST_PT.get(dust, dust)
                if tier == "REFINED" and dust != "ARCANE":
                    label = lang.get(f"dust.selarium.refined_{dust.lower()}", label)
                parts.append(f"{count}× {label}" if count > 1 else label)
            values = cfg.get(ward, {})
            mana = values.get("mana")
            cost = "—" if mana is None else str(mana)
            if "activation" in values:
                cost += f" (+{values['activation']} ao ativar)"
            lines.append(f"| **{name}** | {', '.join(parts)} | {values.get('range', '—')} | {seconds(values.get('interval'))} "
                         f"| {cost} | {seconds(values.get('duration'))} | {seconds(values.get('cooldown'))} |")
        lines.append("")
        notes = []
        for ward in members:
            note = lang.get(f"screen.selarium.codex.ward_note.{ward.lower()}")
            if note:
                notes.append(f"- **{lang.get(f'ward.selarium.{ward.lower()}', ward)}** — {note}")
        if notes:
            lines += notes + [""]
    OUT.write_text("\n".join(lines).rstrip() + "\n", encoding="utf-8")
    print(f"wrote {OUT.relative_to(ROOT)} ({len(types)} wards)")


if __name__ == "__main__":
    main()
