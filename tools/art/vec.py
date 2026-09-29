"""Anti-aliased vector drawing on a supersampled canvas, in normalised [-1, 1] coordinates.

    v = Vec(256)                    # 256x256 output, drawn at 4x
    v.polyline([(0, -0.5), (0, 0.5)], width=0.03)
    mask = v.mask()                 # float32 [0,1], shape (256, 256)
"""
from __future__ import annotations

import math

import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter


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

    # primitives --------------------------------------------------------------------
    def polyline(self, pts, width=0.02, close=False, fill=255, round_caps=True):
        pp = [self.px(p) for p in pts]
        if close:
            pp = pp + [pp[0]]
        w = self.wpx(width)
        self.d.line(pp, fill=fill, width=w, joint="curve")
        if round_caps and w > 2:
            r = w / 2
            for x, y in pp:
                self.d.ellipse([x - r, y - r, x + r, y + r], fill=fill)

    def line(self, a, b, width=0.02, fill=255):
        self.polyline([a, b], width, fill=fill)

    def circle(self, cx, cy, r, width=0.02, fill=255):
        w = self.wpx(width)
        (x, y), rr = self.px((cx, cy)), r * 0.5 * self.n
        self.d.ellipse([x - rr, y - rr, x + rr, y + rr], outline=fill, width=w)

    def disc(self, cx, cy, r, fill=255):
        (x, y), rr = self.px((cx, cy)), r * 0.5 * self.n
        self.d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=fill)

    def arc(self, cx, cy, r, a0, a1, width=0.02, fill=255, steps=None):
        """Arc from angle a0 to a1 (degrees, 0 = +x, 90 = +y i.e. down on screen)."""
        steps = steps or max(8, int(abs(a1 - a0) / 3))
        pts = [(cx + r * math.cos(math.radians(a0 + (a1 - a0) * i / steps)),
                cy + r * math.sin(math.radians(a0 + (a1 - a0) * i / steps))) for i in range(steps + 1)]
        self.polyline(pts, width, fill=fill)

    def polygon(self, pts, fill=255):
        self.d.polygon([self.px(p) for p in pts], fill=fill)

    def polygon_outline(self, pts, width=0.02, fill=255):
        self.polyline(pts, width, close=True, fill=fill)

    def erase_disc(self, cx, cy, r):
        self.disc(cx, cy, r, fill=0)

    def bezier(self, p0, p1, p2, width=0.02, steps=32, fill=255):
        pts = []
        for i in range(steps + 1):
            t = i / steps
            pts.append(((1 - t) ** 2 * p0[0] + 2 * (1 - t) * t * p1[0] + t * t * p2[0],
                        (1 - t) ** 2 * p0[1] + 2 * (1 - t) * t * p1[1] + t * t * p2[1]))
        self.polyline(pts, width, fill=fill)

    def cubic(self, p0, p1, p2, p3, width=0.02, steps=48, fill=255):
        pts = []
        for i in range(steps + 1):
            t = i / steps
            u = 1 - t
            pts.append((u ** 3 * p0[0] + 3 * u * u * t * p1[0] + 3 * u * t * t * p2[0] + t ** 3 * p3[0],
                        u ** 3 * p0[1] + 3 * u * u * t * p1[1] + 3 * u * t * t * p2[1] + t ** 3 * p3[1]))
        self.polyline(pts, width, fill=fill)

    def spiral(self, turns=2.5, r0=0.03, r1=0.4, width=0.02, start=0.0, cx=0.0, cy=0.0, steps=200):
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


def star_polygon(n, step, r, start=-90.0):
    """{n/step} star polygon vertices in draw order."""
    pts = ngon(n, r, start)
    order, i = [], 0
    for _ in range(n):
        order.append(pts[i % n])
        i += step
    return order
