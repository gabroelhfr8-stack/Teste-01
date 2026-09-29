"""A small invented runic alphabet (loosely futhark-inspired) as polylines on a 2 x 4 grid."""
from __future__ import annotations

import math

# Each rune: list of strokes; each stroke: list of (x, y) with x in [0, 2], y in [0, 4] (y down).
RUNES = [
    [[(0, 0), (0, 4)], [(0, 1.4), (2, 0.4)], [(0, 3), (2, 2)]],                       # fehu
    [[(0, 4), (0, 0), (2, 1.2), (2, 4)]],                                              # uruz
    [[(0, 0), (0, 4)], [(0, 1), (2, 2), (0, 3)]],                                      # thurs
    [[(0, 0), (0, 4)], [(0, 0.6), (2, 1.6)], [(0, 2), (2, 3)]],                        # ansuz
    [[(0, 0), (0, 4)], [(0, 0), (2, 1), (0, 2.2)], [(0.4, 2.2), (2, 4)]],              # raido
    [[(2, 0.4), (0, 2), (2, 3.6)]],                                                     # kenaz
    [[(0, 0), (2, 4)], [(2, 0), (0, 4)]],                                              # gebo
    [[(0, 0), (0, 4)], [(0, 0), (2, 1.2), (0, 2.4)]],                                  # wunjo
    [[(0, 0), (0, 4)], [(2, 0), (2, 4)], [(0, 1.3), (2, 2.7)]],                        # hagal
    [[(1, 0), (1, 4)], [(0, 1.4), (2, 2.8)]],                                          # naud
    [[(1, 0), (1, 4)]],                                                                 # isa
    [[(1, 0), (1, 4)], [(1, 0), (2, 1)], [(1, 4), (0, 3)]],                            # eihwaz
    [[(0, 4), (0, 0), (2, 0.8), (0, 1.8)]],                                            # perth
    [[(1, 0), (1, 4)], [(1, 2), (0, 0.4)], [(1, 2), (2, 0.4)]],                        # algiz
    [[(2, 0), (0, 1.3), (2, 2.7), (0, 4)]],                                            # sowilo
    [[(1, 0), (1, 4)], [(0, 1.2), (1, 0), (2, 1.2)]],                                  # tiwaz
    [[(0, 0), (0, 4)], [(0, 0), (2, 1), (0, 2), (2, 3), (0, 4)]],                      # berkano
    [[(0, 0), (0, 4)], [(2, 0), (2, 4)], [(0, 0), (1, 1.2), (2, 0)]],                  # ehwaz
    [[(0, 0), (0, 4)], [(2, 0), (2, 4)], [(0, 0), (2, 2)], [(2, 0), (0, 2)]],          # mannaz
    [[(0, 0), (0, 4)], [(0, 0), (2, 1.3)]],                                            # laguz
    [[(1, 0), (2, 2), (1, 4), (0, 2), (1, 0)]],                                        # ingwaz
    [[(0, 0), (2, 4)], [(0, 4), (2, 0)], [(0, 0), (0, 4)], [(2, 0), (2, 4)]],          # dagaz
    [[(1, 0), (2, 1.5), (1, 3), (0, 1.5), (1, 0)], [(0, 4), (1, 3), (2, 4)]],          # othala
    [[(0, 0.6), (1, 0), (2, 0.6)], [(1, 0), (1, 4)], [(0, 3.4), (1, 4), (2, 3.4)]],    # lantern
]


def rune_strokes(index: int, cx: float, cy: float, height: float, angle_deg: float = 0.0):
    """Return the rune's strokes transformed to normalised canvas coordinates.

    `height` is the rune height in normalised units; `angle_deg` rotates around (cx, cy)
    (0 = upright, glyph "up" pointing to -y).
    """
    rune = RUNES[index % len(RUNES)]
    s = height / 4.0
    a = math.radians(angle_deg)
    out = []
    for stroke in rune:
        pts = []
        for x, y in stroke:
            lx, ly = (x - 1.0) * s, (y - 2.0) * s
            pts.append((cx + lx * math.cos(a) - ly * math.sin(a), cy + lx * math.sin(a) + ly * math.cos(a)))
        out.append(pts)
    return out
