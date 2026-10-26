"""Mechanical details drawn on a Canvas: rivets, bolts, seams, scratches, worn paint."""

from __future__ import annotations

from .canvas import Canvas
from .palette import OUTLINE, Color


# 3x3 domed rivet: glint top-left, body, shaded lower-right; a rim on its lower-right sits it on the plate.
_RIVET = ((6, 5, 3), (5, 4, 2), (3, 2, 1))


def rivet(c: Canvas, x: int, y: int, ramp: list[Color]) -> None:
    for dy, row in enumerate(_RIVET):
        for dx, index in enumerate(row):
            c.px(x + dx, y + dy, ramp[index])
    # A soft rim (the material's own shadow, not black): a black rim reads as a hole.
    for dx, dy in ((3, 1), (1, 3)):
        c.px(x + dx, y + dy, ramp[2])
    for dx, dy in ((3, 2), (2, 3), (3, 3)):
        c.px(x + dx, y + dy, ramp[1])


def small_rivet(c: Canvas, x: int, y: int, ramp: list[Color]) -> None:
    """1-px rivet with its shadow below."""
    c.px(x, y, ramp[6])
    c.px(x, y + 1, ramp[1])


def hex_bolt(c: Canvas, x: int, y: int, ramp: list[Color]) -> None:
    """3x3 bolt head with a dark centre, for heavier parts."""
    c.rect(x, y, x + 2, y + 2, ramp[4])
    c.px(x, y, ramp[6])
    c.px(x + 1, y, ramp[5])
    c.px(x, y + 1, ramp[5])
    c.px(x + 1, y + 1, ramp[2])
    c.px(x + 2, y + 2, ramp[1])
    c.px(x + 2, y + 1, ramp[2])
    c.px(x + 1, y + 2, ramp[2])


def seam(c: Canvas, y: int, x0: int, x1: int, ramp: list[Color]) -> None:
    """Horizontal joint between two plates: dark gap with a lit lip below it."""
    c.rect(x0, y, x1, y, OUTLINE)
    c.rect(x0, y + 1, x1, y + 1, ramp[5])


def scratch(c: Canvas, points: list[tuple[int, int]], ramp: list[Color], light: int = 5, dark: int = 2) -> None:
    """A scratch: lit pixels with a shadow pixel right below each one."""
    for x, y in points:
        c.px(x, y, ramp[light])
        c.px(x, y + 1, ramp[dark])


def blob(c: Canvas, x: int, y: int, rows: list[str], ramp: list[Color]) -> None:
    """A patch (patina, crust) given as rows of '#': lit where its top edge is, shaded along its bottom edge."""
    filled = {(dx, dy) for dy, row in enumerate(rows) for dx, ch in enumerate(row) if ch == "#"}
    for dx, dy in filled:
        if (dx, dy - 1) not in filled:
            index = 5
        elif (dx, dy + 1) not in filled:
            index = 2
        elif (dx - 1, dy) not in filled:
            index = 4
        else:
            index = 3
        c.px(x + dx, y + dy, ramp[index])


def chip(c: Canvas, points: list[tuple[int, int]], paint: list[Color], metal: list[Color]) -> None:
    """Paint worn off down to the metal: bare metal pixels, darker paint along their lower-right rim."""
    bare = set(points)
    for x, y in points:
        c.px(x, y, metal[4] if (x + y) % 3 else metal[5])
    for x, y in points:
        for nx, ny in ((x + 1, y), (x, y + 1), (x + 1, y + 1)):
            if (nx, ny) not in bare:
                c.px(nx, ny, paint[1])
