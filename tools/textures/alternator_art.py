"""The alternator (Alpha), drawn with the base materials of tools/textures/dc in a 32x32 atlas (16 px per block).

Lying machine with a through shaft along Z:
- two round end caps in riveted steel (brass crown, ring of bolts, brass hub around the shaft opening);
- a riveted steel shell whose sides open on reinforced-glass portholes framed in brass;
- inside, the rotor: a steel drum with copper bands, spinning with the shaft (separate partial model);
- on top, a brass terminal box with two copper studs (where the connector goes) and an indicator lamp,
  dark at rest, cyan and full-bright while the alternator produces.

UV rules for elements along Z: up/down u=x v=z, north/south u=x v=y (top row = highest y), east/west u=z v=y.
UV = pixel / 2 in a 32 px atlas.
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.details import rivet, small_rivet
from dc.modelling import element, emissive
from dc.modelling import uv as _uv
from dc.palette import BRASS, COPPER, GLASS, HV_GLOW, OUTLINE, STEEL

# Regions of the atlas, in pixels: (x, y, width, height).
CAP_FACE = (0, 0, 14, 14)
CAP_EDGE_V = (14, 0, 2, 14)        # east/west faces of the caps, u = z (2), v = y
CAP_EDGE_H = (0, 14, 14, 2)        # up/down faces of the caps, u = x, v = z (2)
SHELL_TOP = (16, 0, 14, 12)
RIB_SIDE = (30, 0, 2, 8)           # brass porthole posts, u = z (2), v = y (8)
SHELL_SIDE = (16, 12, 12, 3)       # east/west faces of the top and bottom plates, u = z, v = y
INNER = (0, 16, 14, 8)             # faces seen through the porthole: inner walls, floor, ceiling
ROTOR_SIDE = (14, 16, 8, 8)        # drum, u along the shaft
ROTOR_TOP = (22, 16, 8, 8)         # drum, v along the shaft
PORTHOLE = (0, 24, 8, 8)           # glass, translucent
TERMINAL_TOP = (8, 24, 8, 6)
TERMINAL_SIDE = (8, 30, 8, 1)
FOOT_TOP = (16, 24, 16, 3)
FOOT_SIDE = (16, 27, 16, 1)
LAMP_OFF = (16, 28, 2, 2)
LAMP_ON = (18, 28, 2, 2)


def _cap_face(c: Canvas) -> None:
    """Round end cap: steel plate, brass crown, eight brass bolts, brass hub around the shaft opening."""
    x0, y0, w, h = CAP_FACE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    centre = (w - 1) / 2
    for y in range(h):
        for x in range(w):
            dx, dy = x - centre, y - centre
            d = (dx * dx + dy * dy) ** 0.5
            if 4.4 <= d < 5.6:                          # brass crown, lit on its top-left
                tone = 5 if dx + dy < -3 else 2 if dx + dy > 3 else 4 if dx + dy < 0 else 3
                c.px(x0 + x, y0 + y, BRASS[tone])
            elif d < 4.4:                               # recessed disc inside the crown
                c.px(x0 + x, y0 + y, STEEL[2] if dx + dy < 0 else STEEL[3])
    for bx, by in ((6, 1), (11, 3), (12, 7), (10, 11), (6, 12), (2, 10), (1, 6), (3, 2)):
        small_rivet(c, x0 + bx, y0 + by, BRASS)
    # Hub around a 6-px opening: the shaft's 4-px square needs 5.7 px when it turns past 45 degrees.
    c.rect(x0 + 3, y0 + 3, x0 + 10, y0 + 10, BRASS[3])
    c.bevel(x0 + 3, y0 + 3, x0 + 10, y0 + 10, BRASS[5], BRASS[1], BRASS[6])
    c.rect(x0 + 4, y0 + 4, x0 + 9, y0 + 9, OUTLINE)


def _bands(c: Canvas, region, along_u: bool) -> None:
    """Rotor drum: steel with two copper bands across the shaft; rounded shading across the drum."""
    x0, y0, w, h = region
    pattern = [STEEL, COPPER, COPPER, STEEL, STEEL, COPPER, COPPER, STEEL]   # along the shaft
    shade = [5, 4, 4, 3, 3, 2, 2, 1]                                          # across the drum, top to bottom
    for i in range(w):
        for j in range(h):
            along, across = (i, j) if along_u else (j, i)
            ramp = pattern[along]
            tone = shade[across] if ramp is COPPER else max(1, shade[across] - 1)
            c.px(x0 + i, y0 + j, ramp[tone])
    # A copper strand catching the light on each band.
    for along in (1, 5):
        i, j = (along, 1) if along_u else (1, along)
        c.px(x0 + i, y0 + j, COPPER[6])


def paint() -> Canvas:
    c = Canvas(32, 32)
    _cap_face(c)

    x0, y0, w, h = CAP_EDGE_V
    for y in range(h):
        c.px(x0, y0 + y, STEEL[4])
        c.px(x0 + 1, y0 + y, STEEL[2])
    x0, y0, w, h = CAP_EDGE_H
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, STEEL[2])

    # Shell top: riveted plate (the terminal box covers its middle).
    x0, y0, w, h = SHELL_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((1, 1), (w - 5, 1), (1, h - 5), (w - 5, h - 5)):
        rivet(c, x0 + rx, y0 + ry, STEEL)

    # Shell side band (top and bottom plates seen from the side).
    x0, y0, w, _ = SHELL_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, STEEL[3])
    c.rect(x0, y0 + 2, x0 + w - 1, y0 + 2, STEEL[1])
    for x in (2, 6, 10):
        c.px(x0 + x, y0 + 1, STEEL[6])

    # Brass porthole posts.
    x0, y0, w, h = RIB_SIDE
    for y in range(h):
        c.px(x0, y0 + y, BRASS[5])
        c.px(x0 + 1, y0 + y, BRASS[2])
    c.px(x0, y0, BRASS[6])
    c.px(x0 + 1, y0 + h - 1, BRASS[0])
    c.px(x0, y0 + 2, BRASS[6])
    c.px(x0, y0 + 5, BRASS[6])

    # Inside the machine, seen through the glass: dark, a little lighter toward the top.
    x0, y0, w, h = INNER
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 2, 0)

    _bands(c, ROTOR_SIDE, along_u=True)
    _bands(c, ROTOR_TOP, along_u=False)

    # Porthole glass: light tint, the rotor stays visible; two reflections.
    x0, y0, w, h = PORTHOLE
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, GLASS[4], alpha=60)
    c.rect(x0, y0, x0 + w - 1, y0, GLASS[2], alpha=150)
    for step in range(4):
        c.px(x0 + 1 + step, y0 + 5 - step, GLASS[6], alpha=200)
        c.px(x0 + 2 + step, y0 + 5 - step, GLASS[5], alpha=140)
    c.px(x0 + 5, y0 + 6, GLASS[6], alpha=180)
    c.px(x0 + 6, y0 + 5, GLASS[6], alpha=180)

    # Brass terminal box with two copper studs.
    x0, y0, w, h = TERMINAL_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[5], BRASS[1], BRASS[6])
    for sx in (1, 5):
        c.rect(x0 + sx, y0 + 2, x0 + sx + 1, y0 + 3, COPPER[4])
        c.px(x0 + sx, y0 + 2, COPPER[6])
        c.px(x0 + sx + 1, y0 + 3, COPPER[2])
        c.px(x0 + sx + 2, y0 + 3, BRASS[1])
        c.px(x0 + sx, y0 + 4, BRASS[1])
    x0, y0, w, _ = TERMINAL_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[3])
    c.px(x0, y0, BRASS[5])
    c.px(x0 + w - 1, y0, BRASS[1])

    # Feet.
    x0, y0, w, h = FOOT_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    small_rivet(c, x0 + 2, y0 + 1, BRASS)
    small_rivet(c, x0 + w - 3, y0 + 1, BRASS)
    x0, y0, w, _ = FOOT_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[2])

    # Indicator lamp: dark glass at rest, cyan glow while producing.
    x0, y0, _, _ = LAMP_OFF
    c.px(x0, y0, GLASS[3])
    c.px(x0 + 1, y0, GLASS[2])
    c.px(x0, y0 + 1, GLASS[1])
    c.px(x0 + 1, y0 + 1, GLASS[0])
    x0, y0, _, _ = LAMP_ON
    c.px(x0, y0, HV_GLOW[6])
    c.px(x0 + 1, y0, HV_GLOW[5])
    c.px(x0, y0 + 1, HV_GLOW[5])
    c.px(x0 + 1, y0 + 1, HV_GLOW[4])
    return c


# --- Model --------------------------------------------------------------------------------------------------------

def _cap_piece(name: str, frm, to, outer: str) -> dict:
    """Part of an end cap around the 6-px shaft opening; samples the cap face where it sits."""
    x0, y0, _ = frm
    x1, y1, _ = to
    return element(name, frm, to, {
        outer: _uv(CAP_FACE, x0 - 1, 15 - y1, x1 - x0, y1 - y0),
        "up": _uv(CAP_EDGE_H, x0 - 1, 0, x1 - x0, 2), "down": _uv(CAP_EDGE_H, x0 - 1, 0, x1 - x0, 2),
        "east": _uv(CAP_EDGE_V, 0, 15 - y1, 2, y1 - y0), "west": _uv(CAP_EDGE_V, 0, 15 - y1, 2, y1 - y0),
    })


def _cap(name: str, z0: float, z1: float, outer: str) -> list[dict]:
    return [
        _cap_piece(f"{name}_bottom", [1, 1, z0], [15, 5, z1], outer),
        _cap_piece(f"{name}_top", [1, 11, z0], [15, 15, z1], outer),
        _cap_piece(f"{name}_left", [1, 5, z0], [5, 11, z1], outer),
        _cap_piece(f"{name}_right", [11, 5, z0], [15, 11, z1], outer),
    ]


def _foot(name: str, z0: float) -> dict:
    return element(name, [0, 0, z0], [16, 1, z0 + 3], {
        "up": _uv(FOOT_TOP), "north": _uv(FOOT_SIDE), "south": _uv(FOOT_SIDE),
        "east": _uv(FOOT_SIDE, 0, 0, 3, 1), "west": _uv(FOOT_SIDE, 0, 0, 3, 1)})


def _plate(name: str, y0: float, y1: float, outer: str) -> dict:
    """Top or bottom plate of the shell: riveted outside, dark on the side facing the inside."""
    inner = "down" if outer == "up" else "up"
    return element(name, [1, y0, 2], [15, y1, 14], {
        outer: _uv(SHELL_TOP), inner: _uv(INNER, 0, 0, 14, 8),
        "east": _uv(SHELL_SIDE), "west": _uv(SHELL_SIDE)})


def _post(name: str, z0: float, inner: str) -> dict:
    """Wall between a cap and the portholes: brass posts on the sides, dark face toward the inside."""
    return element(name, [1, 4, z0], [15, 12, z0 + 2], {
        "east": _uv(RIB_SIDE), "west": _uv(RIB_SIDE), inner: _uv(INNER)})


def _porthole(name: str, x0: float, face: str) -> dict:
    return element(name, [x0, 4, 4], [x0 + 0.5, 12, 12], {face: _uv(PORTHOLE)})


def _lamp(region) -> dict:
    lamp = element("lamp", [12, 15, 11], [14, 16, 13], {
        "up": _uv(region), "north": _uv(region, 0, 0, 2, 1), "south": _uv(region, 0, 0, 2, 1),
        "east": _uv(region, 0, 0, 2, 1), "west": _uv(region, 0, 0, 2, 1)})
    return lamp


ALTERNATOR_ELEMENTS = [
    _foot("foot_front", 1),
    _foot("foot_back", 12),
    *_cap("cap_front", 0, 2, "north"),
    *_cap("cap_back", 14, 16, "south"),
    _plate("shell_bottom", 1, 4, "down"),
    _plate("shell_top", 12, 15, "up"),
    _post("post_front", 2, "south"),
    _post("post_back", 12, "north"),
    _porthole("porthole_east", 14.5, "east"),
    _porthole("porthole_west", 1, "west"),
    element("terminal_box", [4, 15, 5], [12, 16, 11], {
        "up": _uv(TERMINAL_TOP), "north": _uv(TERMINAL_SIDE), "south": _uv(TERMINAL_SIDE),
        "east": _uv(TERMINAL_SIDE, 0, 0, 6, 1), "west": _uv(TERMINAL_SIDE, 0, 0, 6, 1)}),
]

LAMP_OFF_ELEMENTS = [_lamp(LAMP_OFF)]

GLOW_ELEMENTS = [emissive(_lamp(LAMP_ON))]

# Spins with the shaft: steel drum with copper bands, plus Create's shaft running through the whole machine.
ROTOR_ELEMENTS = [
    element("rotor", [4, 4, 4], [12, 12, 12], {"up": _uv(ROTOR_TOP), "down": _uv(ROTOR_TOP),
                                              "east": _uv(ROTOR_SIDE), "west": _uv(ROTOR_SIDE)}),
    {"name": "shaft", "from": [6, 6, 0], "to": [10, 10, 16], "faces": {
        **{f: {"uv": [6, 0, 10, 16], "texture": "#axis"} for f in ("up", "down", "east", "west")},
        **{f: {"uv": [6, 6, 10, 10], "texture": "#axis_top"} for f in ("north", "south")}}},
]


def alternator() -> None:
    paint().save("block/alternator.png")


GENERATORS = [alternator]
