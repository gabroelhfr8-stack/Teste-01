"""Sigil art: ward glyphs, dust component marks, rotating rings and the ground base circle.

Everything is generated as white-on-transparent RGBA so the client can tint it per ward /
dust colour and blend it additively.
"""
from __future__ import annotations

import math

import numpy as np

from . import px
from .runes import RUNES, rune_strokes
from .vec import Vec, add, ngon, rot, scale, star_polygon

WARD_ORDER = [
    "ambient_mana", "whispering", "spectral", "bulwark", "rejuvenation", "featherweight", "grounding",
    "magnetism", "banishment", "eclipse", "fertility", "citadel", "disruption", "cloaking",
    "accelerating", "efficiency", "crushing", "inversion", "aqualung", "transmutation", "tangible",
    "sanctuary", "bounty", "immortal", "drain", "soul_chain", "stasis", "maelstrom", "decay",
    "deflection", "silence", "phasing",
]
DUST_ORDER = ["arcane", "aegis", "vital", "focus", "binding", "echo", "density", "warp", "veil", "chrono"]

W = 0.05      # main stroke
WF = 0.032    # fine stroke


def finish(v: Vec, halo: float = 5.0, halo_strength: float = 0.32) -> np.ndarray:
    """White glyph with a soft halo baked into alpha (helps additive blending)."""
    core = v.mask()
    glow = v.glow(halo)
    alpha = np.clip(core + glow * halo_strength * (1 - core), 0, 1)
    img = px.blank(v.size)
    img[..., :3] = 1.0
    img[..., 3] = alpha
    return img


def heart_points(cx, cy, size, steps=64):
    pts = []
    for i in range(steps + 1):
        t = 2 * math.pi * i / steps
        x = 16 * math.sin(t) ** 3
        y = -(13 * math.cos(t) - 5 * math.cos(2 * t) - 2 * math.cos(3 * t) - math.cos(4 * t))
        pts.append((cx + x / 17.0 * size, cy + y / 17.0 * size))
    return pts


def ellipse_points(cx, cy, rx, ry, angle=0.0, steps=48):
    pts = []
    for i in range(steps + 1):
        t = 2 * math.pi * i / steps
        p = rot((rx * math.cos(t), ry * math.sin(t)), angle)
        pts.append((cx + p[0], cy + p[1]))
    return pts


def arrow(v, a, b, width=W, head=0.11, spread=28):
    v.line(a, b, width)
    ang = math.degrees(math.atan2(b[1] - a[1], b[0] - a[0]))
    for s in (-spread, spread):
        tip = (b[0] - head * math.cos(math.radians(ang + s)), b[1] - head * math.sin(math.radians(ang + s)))
        v.line(b, tip, width)


def sparkle(v, cx, cy, r, width=WF):
    v.line((cx - r, cy), (cx + r, cy), width)
    v.line((cx, cy - r), (cx, cy + r), width)
    v.line((cx - r * 0.5, cy - r * 0.5), (cx + r * 0.5, cy + r * 0.5), width * 0.7)
    v.line((cx - r * 0.5, cy + r * 0.5), (cx + r * 0.5, cy - r * 0.5), width * 0.7)


# ------------------------------------------------------------------------------ glyphs
def g_ambient_mana(v):
    pts = [(0, -0.5), (0.3, -0.24), (0.3, 0.24), (0, 0.5), (-0.3, 0.24), (-0.3, -0.24)]
    v.polygon_outline(pts, W)
    v.polyline([(-0.3, -0.24), (0, 0.0), (0.3, -0.24)], WF)
    v.line((0, 0.0), (0, 0.5), WF)
    for a in (200, 320, 80):
        p = (0.55 * math.cos(math.radians(a)), 0.55 * math.sin(math.radians(a)))
        v.disc(p[0], p[1], 0.05)


def g_whispering(v):
    v.disc(0, 0, 0.07)
    for r in (0.2, 0.34, 0.48):
        v.arc(0, 0, r, -48, 48, WF)
        v.arc(0, 0, r, 132, 228, WF)


def g_spectral(v):
    v.bezier((-0.52, 0), (0, -0.62), (0.52, 0), W)
    v.bezier((-0.52, 0), (0, 0.62), (0.52, 0), W)
    v.circle(0, 0, 0.17, WF)
    v.disc(0, 0, 0.075)
    for x in (-0.28, 0, 0.28):
        v.line((x, -0.42 - abs(x) * 0.25), (x * 1.15, -0.55 - abs(x) * 0.25), WF)


def shield_pts(w=0.38, top=-0.42, mid=0.06, bottom=0.5, steps=14):
    pts = [(-w, top), (w, top), (w, mid)]
    for i in range(1, steps + 1):
        t = i / steps
        pts.append(((1 - t) ** 2 * w + 2 * (1 - t) * t * (w * 0.95), (1 - t) ** 2 * mid + 2 * (1 - t) * t * (bottom * 0.75) + t * t * bottom))
    # mirror
    right = pts[2:]
    left = [(-x, y) for x, y in reversed(right)]
    pts = [(-w, top), (w, top)] + right[0:] + [(0, bottom)] + left[1:]
    # tidy: build explicit outline
    outline = [(-w, top), (w, top), (w, mid)]
    for i in range(1, steps + 1):
        t = i / steps
        x = (1 - t) ** 2 * w + t * t * 0 + 2 * (1 - t) * t * (w * 0.9)
        y = (1 - t) ** 2 * mid + 2 * (1 - t) * t * (bottom * 0.8) + t * t * bottom
        outline.append((x, y))
    for i in range(steps - 1, -1, -1):
        t = i / steps
        x = -((1 - t) ** 2 * w + 2 * (1 - t) * t * (w * 0.9))
        y = (1 - t) ** 2 * mid + 2 * (1 - t) * t * (bottom * 0.8) + t * t * bottom
        outline.append((x, y))
    return outline


def g_bulwark(v):
    v.polygon_outline(shield_pts(), W)
    v.line((0, -0.3), (0, 0.36), WF)
    v.line((-0.24, -0.06), (0.24, -0.06), WF)


def g_rejuvenation(v):
    v.polyline(heart_points(0, -0.02, 0.5), W, close=True)
    v.line((0, -0.2), (0, 0.16), WF)
    v.line((-0.18, -0.02), (0.18, -0.02), WF)


def g_featherweight(v):
    tilt = 32

    def T(p):
        return rot(p, tilt)

    # outline (lens) and curved spine of an upright feather, then tilted
    left, right = [], []
    for i in range(33):
        t = i / 32
        y = 0.52 - t * 1.04
        half = 0.30 * math.sin(math.pi * min(1.0, t * 0.92 + 0.04)) ** 0.85
        bow = 0.05 * math.sin(math.pi * t)
        left.append(T((-half + bow, y)))
        right.append(T((half + bow, y)))
    v.polyline(left, WF)
    v.polyline(right, WF)
    v.polyline([T((0.05 * math.sin(math.pi * i / 32), 0.56 - i / 32 * 1.12)) for i in range(33)], W)
    for k in range(7):
        t = 0.16 + k * 0.105
        y = 0.52 - t * 1.04
        bow = 0.05 * math.sin(math.pi * t)
        half = 0.30 * math.sin(math.pi * min(1.0, t * 0.92 + 0.04)) ** 0.85
        v.line(T((bow, y)), T((-half * 0.95 + bow, y - 0.13)), WF * 0.8)
        v.line(T((bow, y)), T((half * 0.95 + bow, y - 0.13)), WF * 0.8)


def g_grounding(v):
    arrow(v, (0, -0.5), (0, 0.18), W, 0.13)
    v.line((-0.48, 0.3), (0.48, 0.3), W)
    for x in (-0.36, -0.12, 0.12, 0.36):
        v.line((x, 0.32), (x - 0.09, 0.46), WF)
    v.arc(0, 0.3, 0.2, 200, 340, WF)


def g_magnetism(v):
    v.arc(0, 0.05, 0.32, 0, 180, W)
    v.line((-0.32, 0.05), (-0.32, -0.3), W)
    v.line((0.32, 0.05), (0.32, -0.3), W)
    for x in (-0.32, 0.32):
        v.line((x - 0.1, -0.3), (x + 0.1, -0.3), W * 1.5)
        for a in (235, 270, 305):
            d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
            v.line((x + d[0] * 0.13, -0.36 + d[1] * 0.13), (x + d[0] * 0.24, -0.36 + d[1] * 0.24), WF)
    # attraction lines converging between the poles
    v.polyline([(-0.14, -0.5), (0.0, -0.4), (0.14, -0.5)], WF * 0.8)


def g_banishment(v):
    v.circle(0, 0, 0.25, W)
    v.line((-0.18, 0.18), (0.18, -0.18), W)
    for a in (45, 135, 225, 315):
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        arrow(v, scale(d, 0.36), scale(d, 0.6), WF, 0.09, 35)


def g_eclipse(v):
    v.disc(0, 0, 0.27)
    v.erase_disc(0.11, -0.07, 0.24)
    for i in range(16):
        a = math.radians(i * 22.5)
        r0, r1 = 0.37, 0.5 if i % 2 == 0 else 0.44
        v.line((r0 * math.cos(a), r0 * math.sin(a)), (r1 * math.cos(a), r1 * math.sin(a)), WF)


def leaf(v, base, tip, width, w=WF):
    mid = ((base[0] + tip[0]) / 2, (base[1] + tip[1]) / 2)
    dx, dy = tip[0] - base[0], tip[1] - base[1]
    n = (-dy, dx)
    ln = math.hypot(*n) or 1
    n = (n[0] / ln * width, n[1] / ln * width)
    v.bezier(base, (mid[0] + n[0], mid[1] + n[1]), tip, w)
    v.bezier(base, (mid[0] - n[0], mid[1] - n[1]), tip, w)


def g_fertility(v):
    v.cubic((0, 0.46), (0.02, 0.3), (-0.02, 0.1), (0, -0.05), W)
    leaf(v, (0, 0.1), (-0.4, -0.2), 0.14)
    leaf(v, (0, -0.05), (0.4, -0.32), 0.14)
    v.arc(0, 0.46, 0.3, 200, 340, WF)
    v.disc(0, 0.46, 0.05)


def g_citadel(v):
    pts = [(-0.32, 0.46), (-0.32, -0.32), (-0.2, -0.32), (-0.2, -0.2), (-0.07, -0.2), (-0.07, -0.32), (0.07, -0.32),
           (0.07, -0.2), (0.2, -0.2), (0.2, -0.32), (0.32, -0.32), (0.32, 0.46)]
    v.polygon_outline(pts, W)
    v.line((-0.1, 0.46), (-0.1, 0.2), WF)
    v.arc(0, 0.2, 0.1, 180, 360, WF)
    v.line((0.1, 0.2), (0.1, 0.46), WF)


def g_disruption(v):
    v.arc(0, 0, 0.42, 20, 100, WF)
    v.arc(0, 0, 0.42, 140, 220, WF)
    v.arc(0, 0, 0.42, 260, 340, WF)
    v.polyline([(0.12, -0.48), (-0.12, -0.04), (0.08, -0.04), (-0.12, 0.48)], W)


def g_cloaking(v):
    v.cubic((-0.34, 0.44), (-0.4, -0.12), (-0.22, -0.5), (0, -0.5), W)
    v.cubic((0.34, 0.44), (0.4, -0.12), (0.22, -0.5), (0, -0.5), W)
    v.polyline([(-0.34, 0.44), (-0.22, 0.52), (-0.11, 0.44), (0.0, 0.52), (0.11, 0.44), (0.22, 0.52), (0.34, 0.44)], WF)
    v.polyline(ellipse_points(0, -0.08, 0.15, 0.21), WF)
    v.disc(-0.06, -0.1, 0.03)
    v.disc(0.06, -0.1, 0.03)


def g_accelerating(v):
    for k, y in enumerate((0.24, 0.0, -0.24)):
        v.polyline([(-0.34, y + 0.13), (0, y - 0.13), (0.34, y + 0.13)], W if k < 2 else W * 1.15)
    v.disc(0, 0.44, 0.045)


def gear_points(teeth, r_out, r_in, hub=None):
    pts = []
    step = 360.0 / teeth
    for i in range(teeth):
        base = i * step - 90
        for off, r in ((-0.36, r_in), (-0.2, r_out), (0.2, r_out), (0.36, r_in)):
            a = math.radians(base + off * step)
            pts.append((r * math.cos(a), r * math.sin(a)))
    return pts


def g_efficiency(v):
    v.polygon_outline(gear_points(8, 0.46, 0.34), W)
    v.circle(0, 0, 0.13, WF)
    v.polyline([(0.03, -0.1), (-0.05, 0.02), (0.04, 0.02), (-0.03, 0.12)], WF)


def g_crushing(v):
    v.polygon_outline([(-0.52, -0.3), (-0.52, 0.3), (-0.14, 0.0)], W)
    v.polygon_outline([(0.52, -0.3), (0.52, 0.3), (0.14, 0.0)], W)
    v.polygon_outline([(-0.06, -0.06), (0.06, -0.06), (0.06, 0.06), (-0.06, 0.06)], WF)
    for sx in (-1, 1):
        v.line((0.0, -0.4), (0.0, -0.5), WF)
    v.line((0, -0.34), (0, -0.48), WF)
    v.line((0, 0.34), (0, 0.48), WF)


def g_inversion(v):
    arrow(v, (-0.17, 0.44), (-0.17, -0.4), W, 0.13)
    arrow(v, (0.17, -0.44), (0.17, 0.4), W, 0.13)
    v.arc(0, 0, 0.5, 300, 360, WF)
    v.arc(0, 0, 0.5, 120, 180, WF)


def g_aqualung(v):
    for k, y in enumerate((0.0, 0.18, 0.36)):
        pts = [(x / 20.0, y + 0.05 * math.sin(x / 20.0 * 9 + k)) for x in range(-9, 10)]
        v.polyline(pts, W if k == 0 else WF)
    v.circle(-0.14, -0.22, 0.07, WF)
    v.circle(0.06, -0.34, 0.1, WF)
    v.circle(0.2, -0.16, 0.05, WF)


def g_transmutation(v):
    v.circle(0, 0, 0.44, WF)
    v.polygon_outline(ngon(3, 0.36), W)
    v.polygon_outline(ngon(3, 0.18, start=90), WF)
    v.disc(0, 0.02, 0.04)


def g_tangible(v):
    hexa = ngon(6, 0.46)
    v.polygon_outline(hexa, W)
    for i in (1, 3, 5):
        v.line((0, 0), hexa[i], WF)


def g_sanctuary(v):
    v.arc(0, 0.3, 0.5, 180, 360, W)
    v.line((-0.5, 0.3), (0.5, 0.3), WF)
    house = [(-0.17, 0.3), (-0.17, 0.02), (0, -0.14), (0.17, 0.02), (0.17, 0.3)]
    v.polyline(house, W)
    v.polyline([(-0.06, 0.3), (-0.06, 0.16), (0.06, 0.16), (0.06, 0.3)], WF)
    for a in (215, 250, 290, 325):
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        v.line(add((0, 0.3), scale(d, 0.58)), add((0, 0.3), scale(d, 0.68)), WF)


def g_bounty(v):
    v.circle(0, 0, 0.36, W)
    v.circle(0, 0, 0.28, WF)
    v.polygon_outline(star_polygon(5, 2, 0.17), WF)
    sparkle(v, 0.44, -0.4, 0.11)
    sparkle(v, -0.44, 0.34, 0.08)


def g_immortal(v):
    pts = []
    for i in range(121):
        t = 2 * math.pi * i / 120
        d = 1 + math.sin(t) ** 2
        pts.append((0.5 * math.cos(t) / d, 0.5 * math.sin(t) * math.cos(t) / d))
    v.polyline(pts, W, close=True)
    v.disc(0, 0, 0.045)
    v.arc(0, 0.02, 0.32, 60, 120, WF)
    v.arc(0, -0.02, 0.32, 240, 300, WF)
    sparkle(v, 0.0, -0.36, 0.08)


def g_drain(v):
    v.spiral(1.7, 0.06, 0.4, W, start=-1.2)
    arrow(v, (0, -0.52), (0, -0.2), WF, 0.09)
    v.disc(0, 0.02, 0.05)


def stadium(cx, cy, half_len, r, angle=0.0, steps=18):
    pts = []
    for i in range(steps + 1):
        a = -90 + 180 * i / steps
        pts.append((half_len + r * math.cos(math.radians(a)), r * math.sin(math.radians(a))))
    for i in range(steps + 1):
        a = 90 + 180 * i / steps
        pts.append((-half_len + r * math.cos(math.radians(a)), r * math.sin(math.radians(a))))
    return [add((cx, cy), rot(p, angle)) for p in pts]


def stadium(cx, cy, half_len, r, angle=0.0, steps=18):
    pts = []
    for i in range(steps + 1):
        a = -90 + 180 * i / steps
        pts.append((half_len + r * math.cos(math.radians(a)), r * math.sin(math.radians(a))))
    for i in range(steps + 1):
        a = 90 + 180 * i / steps
        pts.append((-half_len + r * math.cos(math.radians(a)), r * math.sin(math.radians(a))))
    return [add((cx, cy), rot(p, angle)) for p in pts]


def g_soul_chain(v):
    a = stadium(-0.16, 0.16, 0.2, 0.135, -45)
    b = stadium(0.16, -0.16, 0.2, 0.135, -45)
    v.polyline(a, W, close=True)
    v.polyline(b, 0.11, close=True, fill=0)   # clear a halo so link B passes over link A
    v.polyline(b, W, close=True)
    v.line((-0.5, 0.5), (-0.42, 0.42), WF)
    v.line((0.5, -0.5), (0.42, -0.42), WF)


def g_stasis(v):
    v.line((-0.3, -0.44), (0.3, -0.44), W)
    v.line((-0.3, 0.44), (0.3, 0.44), W)
    v.polyline([(-0.26, -0.44), (-0.26, -0.3), (0.0, 0.0), (-0.26, 0.3), (-0.26, 0.44)], WF)
    v.polyline([(0.26, -0.44), (0.26, -0.3), (0.0, 0.0), (0.26, 0.3), (0.26, 0.44)], WF)
    v.polygon([(-0.18, 0.44), (0.18, 0.44), (0.0, 0.16)])
    v.line((0, -0.02), (0, 0.16), WF)


def g_maelstrom(v):
    v.spiral(2.0, 0.04, 0.48, W, start=0.0)
    v.spiral(2.0, 0.04, 0.48, WF, start=math.pi)


def g_decay(v):
    v.arc(0, -0.08, 0.3, 160, 380, W)
    v.polyline([(-0.27, 0.0), (-0.16, 0.16), (-0.16, 0.34), (0.16, 0.34), (0.16, 0.16), (0.27, 0.0)], W)
    v.disc(-0.11, -0.08, 0.075)
    v.disc(0.11, -0.08, 0.075)
    v.polygon([(0, 0.02), (-0.04, 0.12), (0.04, 0.12)])
    for x in (-0.08, 0, 0.08):
        v.line((x, 0.24), (x, 0.34), WF * 0.7)
    for x in (-0.22, 0.02, 0.24):
        v.line((x, 0.42), (x, 0.52), WF)


def g_deflection(v):
    v.arc(0, 0.62, 0.52, 215, 325, W)
    arrow(v, (-0.5, -0.42), (-0.02, 0.06), WF, 0.1)
    arrow(v, (0.02, 0.06), (0.5, -0.42), W, 0.11)
    sparkle(v, 0, 0.1, 0.1)


def g_silence(v):
    v.cubic((-0.3, 0.26), (-0.3, -0.05), (-0.22, -0.34), (0, -0.34), W)
    v.cubic((0.3, 0.26), (0.3, -0.05), (0.22, -0.34), (0, -0.34), W)
    v.line((-0.38, 0.26), (0.38, 0.26), W)
    v.disc(0, 0.36, 0.05)
    v.line((0, -0.34), (0, -0.44), WF)
    # mute slash with a cleared halo
    v.line((-0.46, -0.46), (0.46, 0.46), 0.11, fill=0)
    v.line((-0.46, -0.46), (0.46, 0.46), W)


def g_phasing(v):
    v.polygon_outline([(-0.14, -0.44), (0.14, -0.44), (0.14, 0.44), (-0.14, 0.44)], W)
    for y in (-0.3, -0.14, 0.02, 0.18, 0.34):
        v.line((-0.14, y), (0.14, y - 0.08), WF * 0.8)
    v.line((-0.52, 0.0), (-0.3, 0.0), W)
    v.line((0.3, 0.0), (0.44, 0.0), W)
    arrow(v, (0.3, 0.0), (0.5, 0.0), W, 0.1)
    v.line((-0.16, 0.0), (0.16, 0.0), 0.11, fill=0)


def g_incomplete(v):
    for i in range(12):
        v.arc(0, 0, 0.42, i * 30 + 4, i * 30 + 20, WF)
    v.line((0, -0.2), (0, 0.2), W)
    v.disc(0, 0.32, 0.04)


GLYPHS = {
    "ambient_mana": g_ambient_mana, "whispering": g_whispering, "spectral": g_spectral, "bulwark": g_bulwark,
    "rejuvenation": g_rejuvenation, "featherweight": g_featherweight, "grounding": g_grounding,
    "magnetism": g_magnetism, "banishment": g_banishment, "eclipse": g_eclipse, "fertility": g_fertility,
    "citadel": g_citadel, "disruption": g_disruption, "cloaking": g_cloaking, "accelerating": g_accelerating,
    "efficiency": g_efficiency, "crushing": g_crushing, "inversion": g_inversion, "aqualung": g_aqualung,
    "transmutation": g_transmutation, "tangible": g_tangible, "sanctuary": g_sanctuary, "bounty": g_bounty,
    "immortal": g_immortal, "drain": g_drain, "soul_chain": g_soul_chain, "stasis": g_stasis,
    "maelstrom": g_maelstrom, "decay": g_decay, "deflection": g_deflection, "silence": g_silence,
    "phasing": g_phasing, "incomplete": g_incomplete,
}


def make_glyph(name: str, size: int = 256) -> np.ndarray:
    v = Vec(size)
    GLYPHS[name](v)
    return finish(v)


# -------------------------------------------------------------------- component marks
def c_arcane(v):
    pts = [(0, -0.7), (0.17, -0.17), (0.7, 0), (0.17, 0.17), (0, 0.7), (-0.17, 0.17), (-0.7, 0), (-0.17, -0.17)]
    v.polygon(pts)


def c_aegis(v):
    v.polygon_outline([(p[0] * 1.6, p[1] * 1.6) for p in shield_pts()], 0.16)


def c_vital(v):
    leaf(v, (0, 0.6), (0, -0.6), 0.5, 0.16)
    v.line((0, 0.6), (0, -0.2), 0.12)


def c_focus(v):
    v.circle(0, 0, 0.42, 0.16)
    v.disc(0, 0, 0.12)
    for a in (0, 90, 180, 270):
        d = (math.cos(math.radians(a)), math.sin(math.radians(a)))
        v.line(scale(d, 0.55), scale(d, 0.8), 0.14)


def c_binding(v):
    v.circle(-0.22, 0, 0.42, 0.15)
    v.circle(0.22, 0, 0.42, 0.15)


def c_echo(v):
    for r in (0.22, 0.44, 0.68):
        v.arc(0, 0.25, r, 215, 325, 0.14)
    v.disc(0, 0.25, 0.08)


def c_density(v):
    v.polygon([(0, -0.7), (0.5, 0), (0, 0.7), (-0.5, 0)])
    v.polygon([(0, -0.3), (0.2, 0), (0, 0.3), (-0.2, 0)], fill=0)


def c_warp(v):
    v.spiral(1.6, 0.05, 0.62, 0.15, start=0.5)


def c_veil(v):
    v.disc(0, 0, 0.62)
    v.erase_disc(0.28, -0.12, 0.55)


def c_chrono(v):
    v.circle(0, 0, 0.62, 0.15)
    v.line((0, 0), (0, -0.4), 0.14)
    v.line((0, 0), (0.28, 0.16), 0.14)


COMPONENTS = {"arcane": c_arcane, "aegis": c_aegis, "vital": c_vital, "focus": c_focus, "binding": c_binding,
              "echo": c_echo, "density": c_density, "warp": c_warp, "veil": c_veil, "chrono": c_chrono}


def make_component(name: str, size: int = 64) -> np.ndarray:
    v = Vec(size, 6)
    COMPONENTS[name](v)
    return finish(v, halo=2.0, halo_strength=0.3)


# ------------------------------------------------------------------------- rings
def ring_marks(v: Vec, r_in: float, r_out: float, count: int, long_every: int = 5, width: float = WF * 0.7):
    for i in range(count):
        a = math.radians(360.0 * i / count)
        r1 = r_out if i % long_every == 0 else (r_in + r_out) / 2 + 0.01
        v.line((r_in * math.cos(a), r_in * math.sin(a)), (r1 * math.cos(a), r1 * math.sin(a)), width)


def make_ring_outer(size: int = 256) -> np.ndarray:
    v = Vec(size)
    v.circle(0, 0, 0.97, 0.022)
    v.circle(0, 0, 0.90, 0.014)
    ring_marks(v, 0.905, 0.965, 72, 6)
    for a in (0, 90, 180, 270):
        c = (0.935 * math.cos(math.radians(a)), 0.935 * math.sin(math.radians(a)))
        v.polygon([add(c, rot((0, -0.05), a)), add(c, rot((0.035, 0), a)), add(c, rot((0, 0.05), a)), add(c, rot((-0.035, 0), a))])
    return finish(v, halo=4.0, halo_strength=0.28)


def make_ring_runes(size: int = 256) -> np.ndarray:
    v = Vec(size)
    v.circle(0, 0, 0.88, 0.014)
    v.circle(0, 0, 0.70, 0.014)
    count = 24
    for i in range(count):
        a = 360.0 * i / count
        c = (0.79 * math.cos(math.radians(a)), 0.79 * math.sin(math.radians(a)))
        for stroke in rune_strokes(i, c[0], c[1], 0.11, a + 90):
            v.polyline(stroke, 0.017)
    return finish(v, halo=3.5, halo_strength=0.3)


def make_ring_star(size: int = 256) -> np.ndarray:
    v = Vec(size)
    v.polygon_outline(star_polygon(8, 3, 0.68), 0.016)
    v.circle(0, 0, 0.70, 0.012)
    v.polygon_outline(ngon(8, 0.30, start=-67.5), 0.014)
    v.circle(0, 0, 0.22, 0.012)
    for p in ngon(8, 0.68):
        v.disc(p[0], p[1], 0.022)
    return finish(v, halo=3.5, halo_strength=0.3)


def make_ring_dash(size: int = 256) -> np.ndarray:
    v = Vec(size)
    for i in range(24):
        v.arc(0, 0, 0.58, i * 15 + 1, i * 15 + 10, 0.02)
    for a in range(0, 360, 60):
        c = (0.5 * math.cos(math.radians(a)), 0.5 * math.sin(math.radians(a)))
        v.polygon([add(c, rot((0, -0.05), a)), add(c, rot((0.04, 0.035), a)), add(c, rot((-0.04, 0.035), a))])
    return finish(v, halo=3.0, halo_strength=0.25)


def make_base_circle(size: int = 256) -> np.ndarray:
    """The chalk-and-dust circle drawn on the ground before any ward is resolved."""
    rng = np.random.default_rng(7)
    v = Vec(size)
    v.circle(0, 0, 0.95, 0.03)
    v.circle(0, 0, 0.87, 0.016)
    v.circle(0, 0, 0.30, 0.02)
    for a in range(0, 360, 45):
        c1 = (0.30 * math.cos(math.radians(a)), 0.30 * math.sin(math.radians(a)))
        c2 = (0.86 * math.cos(math.radians(a)), 0.86 * math.sin(math.radians(a)))
        v.line(c1, c2, 0.012)
    ring_marks(v, 0.885, 0.935, 48, 6, 0.012)
    core = v.mask()
    grain = px.fbm(size, size, 24, 3, 3, tile=False)
    speckle = (rng.random((size, size)) > 0.985).astype(np.float32) * (v.glow(4.0) > 0.03)
    alpha = np.clip(core * (0.82 + 0.35 * (grain - 0.5)) + speckle * 0.55 + v.glow(2.2) * 0.1 * (1 - core), 0, 1)
    img = px.blank(size)
    tint = px.hexrgb("#cfc8ee")
    img[..., :3] = tint * (0.9 + 0.15 * grain[..., None])
    img[..., 3] = alpha
    return img


def small_icon(img, size: int = 32):
    """White silhouette of a glyph at GUI size; only alpha is resampled so no dark fringes appear."""
    from PIL import Image
    alpha = Image.fromarray((np.clip(img[..., 3], 0, 1) * 255 + 0.5).astype(np.uint8), "L").resize((size, size), Image.LANCZOS)
    out = px.blank(size)
    out[..., :3] = 1.0
    out[..., 3] = np.asarray(alpha, dtype=np.float32) / 255.0
    return out


def build_all(assets: "px.Path") -> None:
    base = assets / "textures" / "vfx" / "sigil"
    for name in WARD_ORDER + ["incomplete"]:
        glyph = make_glyph(name)
        px.save(glyph, base / "glyph" / f"{name}.png")
        if name != "incomplete":
            px.save(small_icon(glyph), assets / "textures" / "gui" / "ward_glyph" / f"{name}.png")
    for name in DUST_ORDER:
        px.save(make_component(name), base / "component" / f"{name}.png")
    px.save(make_ring_outer(), base / "ring_outer.png")
    px.save(make_ring_runes(), base / "ring_runes.png")
    px.save(make_ring_star(), base / "ring_star.png")
    px.save(make_ring_dash(), base / "ring_dash.png")
    px.save(make_base_circle(), base / "base_circle.png")
