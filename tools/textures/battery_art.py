"""The small battery (Alpha, GDD §4.7), drawn with the base materials of tools/textures/dc in a 32x32 atlas.

- riveted steel base;
- a riveted steel cell belted by two brass straps, with cooling vents on its east and west sides;
- a glass charge gauge on its north and south faces: four cells that light up cyan (full-bright) with the charge;
- a brass lid with two terminals: + with a red cap, - in dark steel.

The battery block itself comes with milestone 4; these assets are ready for it: the blockstate expects a
"charge" property from 0 (empty) to 4 (full).
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.details import rivet, small_rivet
from dc.modelling import element, emissive, uv
from dc.palette import BRASS, GLASS, HV_GLOW, OUTLINE, PAINT_RED, STEEL

BASE_TOP = (0, 0, 14, 14)
BASE_SIDE = (0, 14, 14, 2)
FRONT = (14, 0, 12, 11)            # north/south faces of the cell, with the gauge
SIDE = (14, 11, 12, 11)            # east/west faces, with vents
LID_TOP = (0, 16, 12, 12)
LID_SIDE = (0, 28, 12, 1)
STRAP = (12, 22, 13, 1)
PLUS_TOP = (26, 0, 2, 2)
PLUS_SIDE = (26, 2, 2, 2)
MINUS_TOP = (28, 0, 2, 2)
MINUS_SIDE = (28, 2, 2, 2)
CELL_LIT = (26, 4, 4, 2)

# Gauge cells, bottom to top, in world pixels (x 6..10 on the north and south faces).
CELLS_Y = (3, 5, 7, 9)


def paint() -> Canvas:
    c = Canvas(32, 32)

    x0, y0, w, h = BASE_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((0, 0), (w - 4, 0), (0, h - 4), (w - 4, h - 4)):
        rivet(c, x0 + rx + 1, y0 + ry + 1, STEEL)
    x0, y0, w, _ = BASE_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, STEEL[2])

    # Front: riveted steel around a brass-framed glass gauge of four dark cells (v = 13 - y).
    x0, y0, w, h = FRONT
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((1, 1), (w - 2, 1), (1, h - 2), (w - 2, h - 2)):
        small_rivet(c, x0 + rx, y0 + ry - (1 if ry > 1 else 0), STEEL)
    c.rect(x0 + 3, y0 + 1, x0 + 8, y0 + 10, BRASS[3])            # frame (world x 5..11, y 3..12)
    c.bevel(x0 + 3, y0 + 1, x0 + 8, y0 + 10, BRASS[5], BRASS[1], BRASS[6])
    for y_world in CELLS_Y:
        v_top = 13 - (y_world + 2)
        c.rect(x0 + 4, y0 + v_top, x0 + 7, y0 + v_top + 1, GLASS[1])
        c.px(x0 + 4, y0 + v_top, GLASS[3])
        if v_top + 2 < 10:                                         # divider under each cell but the lowest
            c.rect(x0 + 4, y0 + v_top + 2, x0 + 7, y0 + v_top + 2, OUTLINE)

    # Sides: riveted steel with three cooling vents.
    x0, y0, w, h = SIDE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for vy in (3, 5, 7):
        c.rect(x0 + 3, y0 + vy, x0 + w - 4, y0 + vy, OUTLINE)
        c.rect(x0 + 3, y0 + vy + 1, x0 + w - 4, y0 + vy + 1, STEEL[5])

    # Brass lid.
    x0, y0, w, h = LID_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[5], BRASS[1], BRASS[6])
    for bx, by in ((1, 1), (w - 2, 1), (1, h - 3), (w - 2, h - 3)):
        small_rivet(c, x0 + bx, y0 + by, STEEL)
    x0, y0, w, _ = LID_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[3])
    c.px(x0, y0, BRASS[5])

    x0, y0, w, _ = STRAP
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[4])
    for x in (1, 6, 11):
        c.px(x0 + x, y0, BRASS[6])

    # Terminals: + red cap, - dark steel.
    for (tx, ty, sx, sy), ramp in (((*PLUS_TOP[:2], *PLUS_SIDE[:2]), PAINT_RED),
                                   ((*MINUS_TOP[:2], *MINUS_SIDE[:2]), STEEL)):
        dark = 2 if ramp is STEEL else 4
        c.px(tx, ty, ramp[dark + 2])
        c.px(tx + 1, ty, ramp[dark + 1])
        c.px(tx, ty + 1, ramp[dark + 1])
        c.px(tx + 1, ty + 1, ramp[dark])
        c.rect(sx, sy, sx + 1, sy, ramp[dark + 1])
        c.rect(sx, sy + 1, sx + 1, sy + 1, BRASS[2])

    # A lit gauge cell (drawn over the dark one when charged).
    x0, y0, w, h = CELL_LIT
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, HV_GLOW[4])
    c.rect(x0, y0, x0 + w - 1, y0, HV_GLOW[5])
    c.px(x0, y0, HV_GLOW[6])
    return c


# --- Model --------------------------------------------------------------------------------------------------------

SIDES = ("north", "south", "east", "west")


def _terminal(name: str, x: float, top, side) -> dict:
    return element(name, [x, 14, 7], [x + 2, 16, 9], {"up": uv(top), **{s: uv(side) for s in SIDES}})


BATTERY_ELEMENTS = [
    element("base", [1, 0, 1], [15, 2, 15], {"up": uv(BASE_TOP), "down": uv(BASE_TOP),
                                             **{s: uv(BASE_SIDE) for s in SIDES}}),
    element("cell", [2, 2, 2], [14, 13, 14], {"north": uv(FRONT), "south": uv(FRONT),
                                             "east": uv(SIDE), "west": uv(SIDE)}),
    element("strap_low", [1.5, 4, 1.5], [14.5, 5, 14.5], {s: uv(STRAP) for s in ("east", "west")}),
    element("strap_high", [1.5, 11, 1.5], [14.5, 12, 14.5], {s: uv(STRAP) for s in ("east", "west")}),
    element("lid", [2, 13, 2], [14, 14, 14], {"up": uv(LID_TOP), **{s: uv(LID_SIDE) for s in SIDES}}),
    _terminal("terminal_plus", 4, PLUS_TOP, PLUS_SIDE),
    _terminal("terminal_minus", 10, MINUS_TOP, MINUS_SIDE),
]


def lit_cells(level: int) -> list[dict]:
    """Full-bright cells just in front of both gauges, the lowest {@code level} of the four."""
    cells = []
    for i, y in enumerate(CELLS_Y[:level]):
        cells.append(emissive(element(f"cell_{i}_north", [6, y, 1.98], [10, y + 2, 2], {"north": uv(CELL_LIT)})))
        cells.append(emissive(element(f"cell_{i}_south", [6, y, 14], [10, y + 2, 14.02], {"south": uv(CELL_LIT)})))
    return cells


def small_battery() -> None:
    paint().save("block/small_battery.png")


GENERATORS = [small_battery]
