"""Selarium colour ramps (dark -> light). See docs/VISUAL_IDENTITY.md."""
from .px import palette

VIOLET = palette("#0f0720", "#20104a", "#361c7c", "#5230b0", "#7548dc", "#9a6cf5", "#c2a2ff", "#e6d9ff", "#f7f1ff")
CYAN = palette("#051a24", "#0a3345", "#0f5670", "#1783a6", "#33b4d4", "#6adcf0", "#a8f2fb", "#defbff")
INDIGO = palette("#080720", "#12103a", "#1d1a60", "#2c2688", "#4238b8", "#6459e6", "#958df8", "#cbc7ff")
STONE = palette("#14121d", "#211e2d", "#332f45", "#4b4662", "#68637f", "#8985a1", "#adaac2", "#d2d0e2", "#efeef8")
GOLD = palette("#24140a", "#4b2a16", "#7b4a22", "#ad752d", "#d9a340", "#f2c862", "#ffe497", "#fff5d2")
MOON = palette("#221f38", "#36325a", "#514d7c", "#736f9f", "#9995bd", "#bcb9d6", "#dad8ec", "#f3f2fa")
ROSE = palette("#2a0a22", "#521440", "#8a2266", "#c23a8a", "#ee62ae", "#ff9ccf", "#ffd0e8")
GREEN = palette("#06200f", "#0d3d1c", "#17652d", "#26944a", "#43c46a", "#7fe79a", "#c0f7cc")
RED = palette("#2a0a12", "#541424", "#8a1e3a", "#c22e50", "#ee5474", "#ff8fa4", "#ffd0d8")
AMBER = palette("#2a1505", "#55290a", "#8a4610", "#c26d18", "#ee9a2a", "#ffc55c", "#ffe9a8")
SILVER = palette("#1b1b26", "#30303f", "#4d4d62", "#71718a", "#9a9ab2", "#c3c3d6", "#e4e4f0", "#f8f8fd")
EMERALD = palette("#031b18", "#07352f", "#0d5e52", "#14907c", "#24c4a4", "#5ee8c8", "#a4f7e4")
SHADOW = palette("#050308", "#0f0a18", "#1b1230", "#2b1c4c", "#3f2a6c", "#5a3f92", "#7f63b8")
PEARL = palette("#3a3326", "#6b5f45", "#a0906a", "#cfbd8c", "#ecdcaa", "#fdf1c8", "#ffffe8")

DUST_RAMPS = {
    "arcane": VIOLET, "aegis": SILVER, "vital": GREEN, "focus": ROSE, "binding": AMBER,
    "echo": CYAN, "density": INDIGO, "warp": EMERALD, "veil": SHADOW, "chrono": PEARL,
}
