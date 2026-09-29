"""Anti-aliased *angular* vector drawing on a supersampled canvas, in normalised [-1, 1] coordinates.

    v = Vec(256)                    # 256x256 output, drawn at 4x
    v.polyline([(0, -0.5), (0, 0.5)], width=0.03)
    mask = v.mask()                 # float32 [0,1], shape (256, 256)

Selarium's shape rule is that nothing is a true circle: every "circle" is a polygon (an octagon by default, a
hexagon or diamond when small), arcs are chains of straight facets, curves are sampled coarsely and strokes end in
mitred corners instead of round caps. Ask for a different look with ``sides=`` or ``FACET_DEG``.
"""
from __future__ import annotations

import math
import random

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

FACET_DEG = 50.0        # widest angular step of an arc; a full turn is therefore an octagon
MITER_LIMIT = 2.6       # longer miters are bevelled so acute corners do not spike


def auto_sides(r: float) -> int:
    """Default facet count for a circle of normalised radius ``r``: small shapes get fewer sides."""
    if r >= 0.2:
        return 8
    if r >= 0.09:
        return 6
    return 4


def default_start(sides: int) -> float:
    """Start angle (degrees) that puts a flat edge on top for an octagon and a corner on top otherwise."""
    return -90.0 + 180.0 / sides if sides == 8 else -90.0


class Vec:
    def __init__(self, size: int, supersample: int = 4):
        self.size = size
        self.ss = supersample
        self.n = size * supersample
        self.img = Image.new("L", (self.n, self.n), 0)
        self.d = ImageDraw.Draw(self.img)

    # coordinate helpers -----------------------------------------------------------
    def px(self, p):
        return ((p[0] * 0.5 + 0.5) * self.n, (p[1] * 0.5 + 0.5) * self.n)

    def wpx(self, width):
        return max(1, int(round(width * 0.5 * self.n)))

    # strokes -----------------------------------------------------------------------
    def polyline(self, pts, width=0.02, close=False, fill=255, round_caps=True):
        """Stroke a polyline with butt caps and mitred joins (``round_caps`` is accepted and ignored)."""
        pp = [self.px(p) for p in pts]
        cleaned = [pp[0]]
        for p in pp[1:]:
            if math.hypot(p[0] - cleaned[-1][0], p[1] - cleaned[-1][1]) > 1e-6:
                cleaned.append(p)
        if close and len(cleaned) > 2 and math.hypot(cleaned[0][0] - cleaned[-1][0], cleaned[0][1] - cleaned[-1][1]) <= 1e-6:
            cleaned.pop()
        pp = cleaned
        hw = self.wpx(width) / 2.0
        if len(pp) == 1:
            x, y = pp[0]
            self.d.polygon([(x, y - hw), (x + hw, y), (x, y + hw), (x - hw, y)], fill=fill)
            return
        count = len(pp)
        segments = [(pp[i], pp[(i + 1) % count]) for i in range(count if close else count - 1)]
        for a, b in segments:
            dx, dy = b[0] - a[0], b[1] - a[1]
            length = math.hypot(dx, dy)
            nx, ny = -dy / length * hw, dx / length * hw
            self.d.polygon([(a[0] + nx, a[1] + ny), (b[0] + nx, b[1] + ny), (b[0] - nx, b[1] - ny), (a[0] - nx, a[1] - ny)], fill=fill)
        joints = range(count) if close else range(1, count - 1)
        for i in joints:
            p0, p1, p2 = pp[i - 1], pp[i], pp[(i + 1) % count]
            self._miter(p0, p1, p2, hw, fill)

    def _miter(self, p0, p1, p2, hw, fill):
        d1 = (p1[0] - p0[0], p1[1] - p0[1])
        d2 = (p2[0] - p1[0], p2[1] - p1[1])
        l1, l2 = math.hypot(*d1), math.hypot(*d2)
        if l1 < 1e-6 or l2 < 1e-6:
            return
        d1 = (d1[0] / l1, d1[1] / l1)
        d2 = (d2[0] / l2, d2[1] / l2)
        cross = d1[0] * d2[1] - d1[1] * d2[0]
        if abs(cross) < 1e-4:
            return
        side = -1.0 if cross > 0 else 1.0          # the outer side of the turn
        n1 = (-d1[1] * side * hw, d1[0] * side * hw)
        n2 = (-d2[1] * side * hw, d2[0] * side * hw)
        q1 = (p1[0] + n1[0], p1[1] + n1[1])
        q2 = (p1[0] + n2[0], p1[1] + n2[1])
        dot = d1[0] * d2[0] + d1[1] * d2[1]
        # miter length relative to the half width is 1 / cos(turn / 2)
        cos_half = math.sqrt(max(0.0, (1.0 + dot) / 2.0))
        if cos_half > 1e-3 and 1.0 / cos_half <= MITER_LIMIT:
            k = 1.0 / (1.0 + (n1[0] * n2[0] + n1[1] * n2[1]) / (hw * hw))
            tip = (p1[0] + (n1[0] + n2[0]) * k, p1[1] + (n1[1] + n2[1]) * k)
            self.d.polygon([p1, q1, tip, q2], fill=fill)
        else:
            self.d.polygon([p1, q1, q2], fill=fill)

    def line(self, a, b, width=0.02, fill=255):
        self.polyline([a, b], width, fill=fill)

    # facets ------------------------------------------------------------------------
    def ring_points(self, cx, cy, r, sides=None, start=None, wobble=None):
        """Vertices of the polygon that stands in for a circle. ``wobble=(degrees, fraction, seed)`` chips it."""
        n = sides or auto_sides(r)
        a0 = default_start(n) if start is None else start
        rng = random.Random(wobble[2]) if wobble else None
        pts = []
        for i in range(n):
            a = a0 + 360.0 * i / n
            rr = r
            if rng:
                a += (rng.random() - 0.5) * 2.0 * wobble[0]
                rr *= 1.0 + (rng.random() - 0.5) * 2.0 * wobble[1]
            pts.append((cx + rr * math.cos(math.radians(a)), cy + rr * math.sin(math.radians(a))))
        return pts

    def circle(self, cx, cy, r, width=0.02, fill=255, sides=None, start=None, wobble=None):
        self.polyline(self.ring_points(cx, cy, r, sides, start, wobble), width, close=True, fill=fill)

    def disc(self, cx, cy, r, fill=255, sides=None, start=None, wobble=None):
        self.polygon(self.ring_points(cx, cy, r, sides, start, wobble), fill=fill)

    def arc(self, cx, cy, r, a0, a1, width=0.02, fill=255, steps=None):
        """Arc from angle a0 to a1 (degrees, 0 = +x, 90 = +y i.e. down on screen), drawn as straight facets."""
        steps = steps or max(1, int(math.ceil(abs(a1 - a0) / FACET_DEG - 1e-6)))
        pts = [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * i / steps)),
                cy + r * math.sin(math.radians(a0 + (a1 - a0) * i / steps))) for i in range(steps + 1)]
        self.polyline(pts, width, fill=fill)

    def polygon(self, pts, fill=255):
        self.d.polygon([self.px(p) for p in pts], fill=fill)

    def polygon_outline(self, pts, width=0.02, fill=255):
        self.polyline(pts, width, close=True, fill=fill)

    def erase_disc(self, cx, cy, r, sides=None):
        self.disc(cx, cy, r, fill=0, sides=sides)

    def bezier(self, p0, p1, p2, width=0.02, steps=6, fill=255):
        pts = []
        for i in range(steps + 1):
            t = i / steps
            pts.append(((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0],
                        (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]))
        self.polyline(pts, width, fill=fill)

    def cubic(self, p0, p1, p2, p3, width=0.02, steps=8, fill=255):
        pts = []
        for i in range(steps + 1):
            t = i / steps
            u = 1 - t
            pts.append((u ** 3 * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t ** 3 * p3[0],
                        u ** 3 * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t ** 3 * p3[1]))
        self.polyline(pts, width, fill=fill)

    def spiral(self, turns=2.5, r0=0.03, r1=0.4, width=0.02, start=0.0, cx=0.0, cy=0.0, steps=None):
        """A spiral of straight facets (eight per turn), so it reads as an angular whorl."""
        steps = steps or max(8, int(round(turns * 8)))
        pts = []
        for i in range(steps + 1):
            t = i / steps
            a = start + t * turns * 2 * math.pi
            r = r0 + (r1 - r0) * t
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
        self.polyline(pts, width)

    # output ------------------------------------------------------------------------
    def mask(self, blur: float = 0.0) -> np.ndarray:
        img = self.img
        if blur > 0:
            img = img.filter(ImageFilter.GaussianBlur(blur * self.ss))
        small = img.resize((self.size, self.size), Image.LANCZOS)
        return np.asarray(small, dtype=np.float32) / 255.0

    def glow(self, radius: float) -> np.ndarray:
        """Gaussian-blurred mask (radius in output pixels) for soft halos."""
        img = self.img.filter(ImageFilter.GaussianBlur(radius * self.ss))
        small = img.resize((self.size, self.size), Image.LANCZOS)
        return np.asarray(small, dtype=np.float32) / 255.0


def rot(p, deg):
    a = math.radians(deg)
    return (p[0] * math.cos(a) - p[1] * math.sin(a), p[0] * math.sin(a) + p[1] * math.cos(a))


def add(p, q):
    return (p[0] + q[0], p[1] + q[1])


def scale(p, s):
    return (p[0] * s, p[1] * s)


def ngon(n, r, start=-90.0, center=(0.0, 0.0)):
    return [add(center, (r * math.cos(math.radians(start + 360.0 * i / n)), r * math.sin(math.radians(start + 360.0 * i / n)))) for i in range(n)]


def chipped_ngon(n, r, seed, angle_jitter=5.0, radius_jitter=0.035, start=None):
    """An irregular polygon: every corner is nudged a little, like a hand-cut stone or a chalk line drawn by hand."""
    rng = random.Random(seed)
    a0 = default_start(n) if start is None else start
    pts = []
    for i in range(n):
        a = a0 + 360.0 * i / n + (rng.random() - 0.5) * 2.0 * angle_jitter
        rr = r * (1.0 + (rng.random() - 0.5) * 2.0 * radius_jitter)
        pts.append((rr * math.cos(math.radians(a)), rr * math.sin(math.radians(a))))
    return pts


def star_polygon(n, step, r, start=-90.0):
    """{n/step} star polygon vertices in draw order."""
    pts = ngon(n, r, start)
    order, i = [], 0
    for _ in range(n):
        order.append(pts[i % n])
        i += step
    return order


def along(poly, s):
    """Point and unit tangent at fraction ``s`` (0..1) of the perimeter of the closed polygon ``poly``."""
    count = len(poly)
    lengths = [math.hypot(poly[(i + 1) % count][0] - poly[i][0], poly[(i + 1) % count][1] - poly[i][1]) for i in range(count)]
    total = sum(lengths)
    target = (s % 1.0) * total
    for i in range(count):
        if target <= lengths[i] or i == count - 1:
            t = target / lengths[i] if lengths[i] else 0.0
            a, b = poly[i], poly[(i + 1) % count]
            tangent = ((b[0] - a[0]) / lengths[i], (b[1] - a[1]) / lengths[i]) if lengths[i] else (1.0, 0.0)
            return (a[0] + (b[0] - a[0]) * t, a[1] + (b[1] - a[1]) * t), tangent
        target -= lengths[i]
    return poly[0], (1.0, 0.0)


def ray_hit(poly, angle_deg, origin=(0.0, 0.0)):
    """Where a ray leaving ``origin`` at ``angle_deg`` first crosses the closed polygon ``poly`` (None if it misses)."""
    dx, dy = math.cos(math.radians(angle_deg)), math.sin(math.radians(angle_deg))
    best = None
    count = len(poly)
    for i in range(count):
        a, b = poly[i], poly[(i + 1) % count]
        ex, ey = b[0] - a[0], b[1] - a[1]
        denom = dx * ey - dy * ex
        if abs(denom) < 1e-9:
            continue
        t = ((a[0] - origin[0]) * ey - (a[1] - origin[1]) * ex) / denom
        u = ((a[0] - origin[0]) * dy - (a[1] - origin[1]) * dx) / denom
        if t > 1e-9 and -1e-9 <= u <= 1.0 + 1e-9 and (best is None or t < best[0]):
            best = (t, (origin[0] + dx * t, origin[1] + dy * t))
    return best[1] if best else None


def octagon_norm(x, y):
    """Distance in the octagonal metric: 1.0 on the regular octagon whose flat sides are 1 away from the centre."""
    ax, ay = np.abs(x), np.abs(y)
    return np.maximum(np.maximum(ax, ay), (ax + ay) * math.sqrt(0.5))
