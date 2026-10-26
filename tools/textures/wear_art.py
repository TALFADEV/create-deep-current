"""Tier-1 pressure damage (GDD §3.4): three transparent 16x16 overlays laid over every face of a damaged machine.

    overlay/cracks_1  integrity under 75 %: hairline cracks.
    overlay/cracks_2  under 50 %: the cracks run longer and branch, the plates weep rust (the "leaks").
    overlay/cracks_3  under 25 %: open fractures, a punched-in plate, more rust.

Each stage keeps every crack of the stage before, so the damage visibly grows on the same lines. The overlays
only darken and stain: they work over steel, brass and paint alike. A crack is a groove: dark core, its upper-left
wall in shadow, its lower-right lip catching the light (light from the top left).

The overlays use the world position of each face as UV (like the block-breaking overlay), so they continue across
the elements of a model; see dc.modelling.overlay.
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.palette import COPPER, HADAL, OUTLINE, STEEL

Point = tuple[int, int]

# Crack paths per stage, as polylines in pixels. A stage draws its own paths and every earlier one.
CRACKS: dict[int, list[list[Point]]] = {
    1: [
        [(3, 2), (5, 4), (5, 6), (7, 8)],
        [(12, 11), (13, 13)],
    ],
    2: [
        [(7, 8), (8, 10), (10, 11), (12, 11)],
        [(5, 6), (2, 8)],
        [(10, 2), (11, 4), (13, 5)],
    ],
    3: [
        [(13, 5), (14, 8), (12, 9), (12, 11)],
        [(8, 10), (6, 13), (6, 15)],
    ],
}

# Rust weeping down from a crack: start point and length, per stage.
STREAKS: dict[int, list[tuple[Point, int]]] = {
    2: [((5, 7), 3), ((11, 12), 3)],
    3: [((13, 9), 3), ((7, 11), 4)],
}

# The machine's shell, which carries the overlay (dc.modelling.overlay): small parts would only show scraps of it.
SHELLS: dict[str, tuple[str, ...]] = {
    "small_tidal_turbine": ("foot", "nacelle_wide", "nacelle_tall"),
    "alternator": ("foot_front", "foot_back", "shell_bottom", "shell_top",
                   *(f"cap_{end}_{side}" for end in ("front", "back") for side in ("bottom", "top", "left", "right"))),
    "small_battery": ("base", "cell", "lid"),
}

# Plate punched in by the pressure (stage 3): top-left corner of a 2x2 hole.
HOLE: Point = (8, 8)


def _line(a: Point, b: Point) -> list[Point]:
    """Bresenham: a jagged, pixel-exact line, which suits a crack."""
    (x0, y0), (x1, y1) = a, b
    dx, dy = abs(x1 - x0), -abs(y1 - y0)
    sx, sy = (1 if x1 > x0 else -1), (1 if y1 > y0 else -1)
    err = dx + dy
    points = []
    while True:
        points.append((x0, y0))
        if (x0, y0) == (x1, y1):
            return points
        e2 = 2 * err
        if e2 >= dy:
            err += dy
            x0 += sx
        if e2 <= dx:
            err += dx
            y0 += sy


def crack_pixels(stage: int) -> set[Point]:
    pixels = set()
    for s in range(1, stage + 1):
        for path in CRACKS[s]:
            for a, b in zip(path, path[1:]):
                pixels.update(_line(a, b))
    return {(x, y) for x, y in pixels if 0 <= x < 16 and 0 <= y < 16}


def paint(stage: int) -> Canvas:
    c = Canvas()
    cracks = crack_pixels(stage)

    # Rust first, so the cracks are drawn over it: dark at the source, fading as it runs down.
    for s in range(2, stage + 1):
        for (x, y), length in STREAKS[s]:
            for i in range(length):
                c.px(x, y + i, COPPER[1] if i == 0 else COPPER[2], 200 - i * 120 // length)

    # Groove walls: lower-right lip lit; the open fractures of stage 3 also get their upper-left wall in shadow.
    for x, y in cracks:
        if (x + 1, y + 1) not in cracks:
            c.px(x + 1, y + 1, STEEL[6], 110)
        if stage == 3 and (x - 1, y) not in cracks:
            c.px(x - 1, y, HADAL[2], 100)
    for x, y in cracks:
        c.px(x, y, OUTLINE, 230 if stage == 1 else 255)

    if stage == 3:
        hx, hy = HOLE
        # Buckled rim: the plate bends in, its upper-left edge falls into shadow, the lower-right one is lit.
        for x, y in ((hx - 1, hy - 1), (hx, hy - 1), (hx + 1, hy - 1), (hx - 1, hy), (hx - 1, hy + 1)):
            c.px(x, y, HADAL[3])
        for x, y in ((hx + 2, hy), (hx + 2, hy + 1), (hx, hy + 2), (hx + 1, hy + 2), (hx + 2, hy + 2)):
            c.px(x, y, STEEL[6])
        c.rect(hx, hy, hx + 1, hy + 1, OUTLINE)
        c.px(hx + 1, hy + 1, HADAL[1])
    return c


def cracks() -> None:
    for stage in (1, 2, 3):
        paint(stage).save(f"block/overlay/cracks_{stage}.png")


GENERATORS = [cracks]
