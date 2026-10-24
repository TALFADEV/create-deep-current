"""Painters for model parts in Create's style. Each painter fills a w x d (top) or w x h (side) area at the
origin of the image it is given; callers place the result wherever their texture layout wants it.
"""

from __future__ import annotations

import palette as p
from pixels import band, bevel, fill, pick, plate


def top_plate(ramp, hole=False, bolts=False):
    def paint(img, w, d, seed):
        plate(img, 0, 0, w - 1, d - 1, ramp, seed)
        if hole and w >= 3:
            cx, cy = w // 2, d // 2
            img.putpixel((cx, cy), pick(ramp, 0))
            if w >= 4:
                img.putpixel((cx - 1, cy - 1), pick(ramp, 0))
                img.putpixel((cx - 1, cy), pick(ramp, 1))
                img.putpixel((cx, cy - 1), pick(ramp, 1))
        if bolts:
            for x, y in ((1, 1), (w - 2, 1), (1, d - 2), (w - 2, d - 2)):
                img.putpixel((x, y), pick(p.IRON, 4))
    return paint


def side_band(ramp, mark_row=None, patina=False):
    def paint(img, w, h, seed):
        band(img, 0, 0, w - 1, h - 1, ramp, seed)
        if mark_row is not None:
            for x in range(w):
                img.putpixel((x, mark_row), pick(p.MARK_LV, 3 if x == 0 else 2 if x < w - 1 else 1))
        if patina:
            # A light touch: two specks of verdigris along the lower edge.
            img.putpixel((1, h - 1), pick(p.VERDIGRIS, 2))
            img.putpixel((w - 2, h - 1), pick(p.VERDIGRIS, 3))
    return paint


def cylinder_side(ramp, mark_row=None, glaze=False):
    """Narrow round body: light column left of centre, dark right edge."""
    def paint(img, w, h, seed):
        fill(img, 0, 0, w - 1, h - 1, ramp, 2, seed, noise=0.2)
        for y in range(h):
            img.putpixel((0, y), pick(ramp, 3))
            img.putpixel((w - 1, y), pick(ramp, 1))
            if w >= 3:
                img.putpixel((1, y), pick(ramp, 5 if glaze and y % 3 == 0 else 4))
        if mark_row is not None:
            for x in range(w):
                img.putpixel((x, mark_row), pick(p.MARK_LV, 3 if x == 1 else 1 if x == w - 1 else 2))
    return paint


def casing_top(img, w, d, seed):
    """Create's casing look: andesite frame around a dark wood recess with plank seams."""
    fill(img, 0, 0, w - 1, d - 1, p.ANDESITE, 3, seed)
    bevel(img, 0, 0, w - 1, d - 1, p.ANDESITE, 5, 1)
    fill(img, 2, 2, w - 3, d - 3, p.DARK_WOOD, 3, seed + 1)
    bevel(img, 2, 2, w - 3, d - 3, p.DARK_WOOD, 0, 5)
    for x in range(3, w - 3, 3):
        for y in range(3, d - 3):
            img.putpixel((x, y), pick(p.DARK_WOOD, 1))


def casing_side(img, w, h, seed):
    fill(img, 0, 0, w - 1, h - 1, p.ANDESITE, 2, seed)
    for x in range(w):
        img.putpixel((x, 0), pick(p.ANDESITE, 5))
        img.putpixel((x, h - 1), pick(p.ANDESITE, 0))
    if h >= 4:
        fill(img, 2, 1, w - 3, h - 2, p.DARK_WOOD, 3, seed + 3)
        for x in range(2, w - 2):
            img.putpixel((x, 1), pick(p.DARK_WOOD, 0))
