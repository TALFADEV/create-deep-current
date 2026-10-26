"""The electric motor (Alpha), drawn with the base materials of tools/textures/dc in a 32x32 atlas.

Designed facing up (the shaft comes out of the top face); the blockstate turns it toward its facing.
- riveted steel mounting base, bolted at the four corners;
- a body with cooling fins in sea-green paint, so the motor reads apart from the steel-and-brass alternator;
- a copper winding band standing proud around the body;
- a brass cap crowned with bolts, and the output collar: a steel ring around a 6-px opening where Create's half
  shaft spins (its 4-px square needs 5.7 px when it turns past 45 degrees);
- a brass terminal box on the north side, with two copper studs, for the connector.
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.details import rivet, small_rivet
from dc.modelling import element, uv
from dc.palette import BRASS, COPPER, OUTLINE, STEEL, VERDIGRIS

BASE_TOP = (0, 0, 14, 14)
BASE_SIDE = (0, 14, 14, 2)
CAP_TOP = (14, 0, 12, 12)
CAP_SIDE = (14, 12, 12, 2)
HOUSING_SIDE = (0, 16, 10, 11)     # finned body, u across the face, v = y
BAND_SIDE = (10, 16, 11, 2)
COLLAR_TOP = (24, 14, 8, 8)
COLLAR_SIDE = (10, 18, 8, 1)
TERMINAL_FACE = (10, 19, 6, 5)
TERMINAL_SIDE = (16, 19, 2, 5)
TERMINAL_TOP = (18, 19, 6, 2)


def paint() -> Canvas:
    c = Canvas(32, 32)

    # Mounting base: riveted plate (the body covers its middle), steel band on its sides.
    x0, y0, w, h = BASE_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((0, 0), (w - 4, 0), (0, h - 4), (w - 4, h - 4)):
        rivet(c, x0 + rx + 1, y0 + ry + 1, STEEL)
    x0, y0, w, _ = BASE_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, STEEL[2])
    for x in (1, w - 2):
        c.px(x0 + x, y0, BRASS[5])

    # Finned body: lit fin, fin flank, shadowed gap, repeated; brighter at the top, darker at the bottom.
    x0, y0, w, h = HOUSING_SIDE
    for x in range(w):
        for y in range(h):
            base = (5, 3, 1)[x % 3]
            tone = base - (1 if y > h - 4 else 0) + (1 if y == 0 else 0)
            c.px(x0 + x, y0 + y, VERDIGRIS[max(0, min(6, tone))])
    for x in range(w):
        c.px(x0 + x, y0 + h - 1, VERDIGRIS[0])

    # Copper winding band: two turns, lit wire on top, strands catching the light.
    x0, y0, w, _ = BAND_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, COPPER[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, COPPER[3])
    for x in (1, 5, 9):
        c.px(x0 + x, y0, COPPER[6])
    for x in (3, 7):
        c.px(x0 + x, y0 + 1, COPPER[2])

    # Brass cap with a crown of steel bolts (the collar covers its middle).
    x0, y0, w, h = CAP_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[5], BRASS[1], BRASS[6])
    for bx, by in ((1, 1), (5, 1), (10, 1), (1, 5), (10, 5), (1, 9), (5, 10), (10, 9)):
        small_rivet(c, x0 + bx, y0 + by, STEEL)
    # Under the collar's opening (world 5..11): a dark recess, not brass, around the spinning shaft.
    c.rect(x0 + 3, y0 + 3, x0 + 8, y0 + 8, OUTLINE)
    x0, y0, w, _ = CAP_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, BRASS[2])
    c.px(x0, y0, BRASS[6])

    # Output collar: steel ring with a lit outer rim and a dark lip around the opening.
    x0, y0, w, h = COLLAR_TOP
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[4])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[6], STEEL[2])
    c.rect(x0 + 1, y0 + 1, x0 + w - 2, y0 + h - 2, OUTLINE)
    x0, y0, w, _ = COLLAR_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])

    # Terminal box: brass, two copper studs.
    x0, y0, w, h = TERMINAL_FACE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[5], BRASS[1], BRASS[6])
    for sx in (1, 3):
        c.px(x0 + sx, y0 + 2, COPPER[6])
        c.px(x0 + sx + 1, y0 + 2, COPPER[4])
        c.px(x0 + sx + 1, y0 + 3, COPPER[2])
    x0, y0, w, h = TERMINAL_SIDE
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[2])
    c.rect(x0, y0, x0, y0 + h - 1, BRASS[4])
    x0, y0, w, h = TERMINAL_TOP
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, BRASS[3])
    return c


# --- Model (facing up) -----------------------------------------------------------------------------------------

SIDES = ("north", "south", "east", "west")


def _collar_piece(name: str, frm, to) -> dict:
    """Part of the output collar around the 6-px opening; samples the collar ring where it sits."""
    dx, dz = frm[0] - 4, frm[2] - 4
    w, d = to[0] - frm[0], to[2] - frm[2]
    side = uv(COLLAR_SIDE, 0, 0, max(w, d), 1)
    return element(name, frm, to, {"up": uv(COLLAR_TOP, dx, dz, w, d), **{s: side for s in SIDES}})


MOTOR_ELEMENTS = [
    element("base", [1, 0, 1], [15, 2, 15], {"up": uv(BASE_TOP), "down": uv(BASE_TOP),
                                             **{s: uv(BASE_SIDE) for s in SIDES}}),
    element("body", [3, 2, 3], [13, 13, 13], {s: uv(HOUSING_SIDE) for s in SIDES}),
    element("winding_band", [2.5, 5, 2.5], [13.5, 7, 13.5], {s: uv(BAND_SIDE) for s in SIDES}),
    element("cap", [2, 13, 2], [14, 15, 14], {"up": uv(CAP_TOP), "down": uv(CAP_TOP),
                                             **{s: uv(CAP_SIDE) for s in SIDES}}),
    _collar_piece("collar_north", [4, 15, 4], [12, 16, 5]),
    _collar_piece("collar_south", [4, 15, 11], [12, 16, 12]),
    _collar_piece("collar_west", [4, 15, 5], [5, 16, 11]),
    _collar_piece("collar_east", [11, 15, 5], [12, 16, 11]),
    element("terminal_box", [5, 4, 1.5], [11, 9, 3], {
        "north": uv(TERMINAL_FACE),
        "east": uv(TERMINAL_SIDE, 0, 0, 1.5, 5), "west": uv(TERMINAL_SIDE, 0, 0, 1.5, 5),
        "up": uv(TERMINAL_TOP, 0, 0, 6, 1.5), "down": uv(TERMINAL_TOP, 0, 0, 6, 1.5)}),
]

# For the item only (nothing spins in inventories): Create's shaft coming out of the collar.
ITEM_SHAFT = [{"name": "shaft", "from": [6, 8, 6], "to": [10, 16, 10], "faces": {
    **{f: {"uv": [6, 0, 10, 8], "texture": "#axis"} for f in SIDES},
    "up": {"uv": [6, 6, 10, 10], "texture": "#axis_top"}}}]


def electric_motor() -> None:
    paint().save("block/electric_motor.png")


GENERATORS = [electric_motor]
