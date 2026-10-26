"""The small tidal turbine (Alpha, GDD §4.2), drawn with the base materials of tools/textures/dc in a 32x32 atlas.

A one-block turbine lying along Z, designed facing north (the rotor points into the current, toward -Z):
- a riveted steel foot and a short pylon holding the nacelle;
- a nacelle painted safety yellow (anti-corrosion paint, GDD §1), octagonal in section, girdled by two steel
  bands, with a brass inspection hatch on its flanks and a lifting lug on top;
- at the back, a steel collar around a 6-px opening where Create's half shaft spins (the output);
- the rotor, a separate model that spins as a whole around Z: a small brass hub and nose and four pitched blades,
  light steel with red tips. Each blade is tilted 22.5 degrees around its own length, the top and bottom (and the
  left and right) ones the opposite way, so the four form one propeller.

The blockstate turns it toward its horizontal "facing"; the rotor and the half shaft spin as one partial model.
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.details import rivet, small_rivet
from dc.modelling import element, uv
from dc.palette import BRASS, OUTLINE, PAINT_RED, PAINT_YELLOW, STEEL

ROUND_SIDE = (0, 0, 9, 5)          # flanks and belly of the nacelle, u along Z, v down
ROUND_TOP = (9, 0, 5, 9)           # its back, u across X, v along Z
NACELLE_END = (0, 5, 6, 6)
NACELLE_EDGE = (6, 5, 3, 9)        # slivers of the square core showing at the chamfers
BAND = (16, 0, 8, 8)
COLLAR_FACE = (24, 0, 8, 8)
COLLAR_SIDE = (14, 0, 2, 8)
HUB_FRONT = (16, 8, 4, 4)
HUB_SIDE = (0, 11, 4, 3)
NOSE_FRONT = (20, 8, 2, 2)
NOSE_SIDE = (22, 8, 2, 1)
LUG = (26, 10, 2, 2)
BLADE = (0, 14, 3, 6)              # vertical blade, tip at the top (v = 0)
BLADE_H = (3, 14, 6, 3)            # horizontal blade, tip on the right (u = 5)
BLADE_EDGE = (3, 17, 6, 1)
FOOT_TOP = (11, 14, 10, 9)
FOOT_SIDE = (11, 23, 10, 2)
PYLON_SIDE = (21, 14, 5, 3)
PYLON_FRONT = (26, 14, 3, 3)


def _paint_nacelle(c: Canvas) -> None:
    # Flanks: yellow paint, lit at the top; bolt rows at both ends; a brass hatch with a steel latch; worn paint.
    x0, y0, w, h = ROUND_SIDE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW, 5, 2)
    for y in range(h):
        c.px(x0, y0 + y, PAINT_YELLOW[2])
        c.px(x0 + w - 1, y0 + y, PAINT_YELLOW[1])
    c.rect(x0 + 3, y0 + 1, x0 + 5, y0 + 3, BRASS[4])
    c.bevel(x0 + 3, y0 + 1, x0 + 5, y0 + 3, BRASS[6], BRASS[1])
    c.px(x0 + 4, y0 + 2, STEEL[2])
    c.px(x0 + 1, y0 + 2, PAINT_YELLOW[6])
    c.px(x0 + 7, y0 + 1, STEEL[4])          # paint worn to the steel
    c.px(x0 + 7, y0 + 2, STEEL[2])

    x0, y0, w, h = ROUND_TOP
    for x in range(w):
        tone = (5, 6, 5, 4, 3)[x]
        c.rect(x0 + x, y0, x0 + x, y0 + h - 1, PAINT_YELLOW[tone])
    c.rect(x0, y0, x0 + w - 1, y0, PAINT_YELLOW[3])
    c.rect(x0, y0 + h - 1, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[2])
    for y in (2, 6):
        small_rivet(c, x0 + 1, y0 + y, STEEL)
        small_rivet(c, x0 + 3, y0 + y, STEEL)

    x0, y0, w, h = NACELLE_END
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[3])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[5], PAINT_YELLOW[1])
    x0, y0, w, h = NACELLE_EDGE
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[2])
    c.rect(x0, y0, x0, y0 + h - 1, PAINT_YELLOW[4])

    # Steel bands: a dark seam on each side of a lit strap, bolt heads along it.
    x0, y0, w, h = BAND
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 5, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[6], STEEL[1])
    for y in (2, 5):
        c.px(x0 + 3, y0 + y, STEEL[6])
        c.px(x0 + 4, y0 + y, STEEL[2])

    # Output collar: steel ring with a lit outer rim and a dark opening for the shaft.
    x0, y0, w, h = COLLAR_FACE
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[4])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[6], STEEL[2])
    c.rect(x0 + 1, y0 + 1, x0 + w - 2, y0 + h - 2, OUTLINE)
    x0, y0, w, h = COLLAR_SIDE
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[3])
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.px(x0, y0 + 3, BRASS[5])

    # Lifting lug.
    x0, y0, _, _ = LUG
    c.px(x0, y0, STEEL[6])
    c.px(x0 + 1, y0, STEEL[4])
    c.px(x0, y0 + 1, STEEL[3])
    c.px(x0 + 1, y0 + 1, STEEL[1])


def _paint_rotor(c: Canvas) -> None:
    # Hub: brass disc with a ring of steel bolts; nose: a polished brass dome.
    x0, y0, w, h = HUB_FRONT
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 2)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[5], BRASS[1], BRASS[6])
    for bx, by in ((0, 0), (w - 1, 0), (0, h - 2), (w - 1, h - 2)):
        small_rivet(c, x0 + bx, y0 + by, STEEL)
    x0, y0, w, h = HUB_SIDE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, BRASS, 4, 2)
    c.rect(x0, y0 + h - 1, x0 + w - 1, y0 + h - 1, BRASS[1])
    x0, y0, w, h = NOSE_FRONT
    c.px(x0, y0, BRASS[6])
    c.px(x0 + 1, y0, BRASS[5])
    c.px(x0, y0 + 1, BRASS[5])
    c.px(x0 + 1, y0 + 1, BRASS[3])
    x0, y0, w, _ = NOSE_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[3])

    # Blades: light steel, lit leading edge, shaded trailing edge, red tips (two pixels), a root collar.
    x0, y0, w, h = BLADE
    for v in range(h):
        for u, tone in enumerate((6, 5, 3)):
            ramp, t = (PAINT_RED, tone - 1) if v < 1 else (STEEL, tone)
            c.px(x0 + u, y0 + v, ramp[t])
    c.rect(x0, y0 + h - 1, x0 + w - 1, y0 + h - 1, STEEL[2])
    x0, y0, w, h = BLADE_H
    for u in range(w):
        for v, tone in enumerate((6, 5, 3)):
            ramp, t = (PAINT_RED, tone - 1) if u >= w - 1 else (STEEL, tone)
            c.px(x0 + u, y0 + v, ramp[t])
    c.rect(x0, y0, x0, y0 + h - 1, STEEL[2])
    x0, y0, w, _ = BLADE_EDGE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[3])
    c.px(x0 + w - 1, y0, PAINT_RED[2])


def _paint_mount(c: Canvas) -> None:
    x0, y0, w, h = FOOT_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((0, 0), (w - 4, 0), (0, h - 4), (w - 4, h - 4)):
        rivet(c, x0 + rx + 1, y0 + ry + 1, STEEL)
    x0, y0, w, _ = FOOT_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, STEEL[2])
    x0, y0, w, h = PYLON_SIDE
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 3, 2)
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[1])     # in the nacelle's shadow
    c.px(x0 + 1, y0 + 1, STEEL[5])
    c.px(x0 + w - 2, y0 + 1, STEEL[5])
    x0, y0, w, h = PYLON_FRONT
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 3, 2)
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[1])


def paint() -> Canvas:
    c = Canvas(32, 32)
    _paint_nacelle(c)
    _paint_rotor(c)
    _paint_mount(c)
    return c


# --- Model (facing north) ----------------------------------------------------------------------------------------

def _rotated(e: dict, axis: str, angle: float, origin) -> dict:
    e["rotation"] = {"angle": angle, "axis": axis, "origin": origin}
    return e


def _octagon(name: str, z0: float, z1: float, half: float, chamfer: float, side, top, end) -> list[dict]:
    """A prism along Z with an octagonal section: two crossed boxes {@code half} px from the axis, the corners cut
    by {@code chamfer} px."""
    lo, hi, length = 8 - half, 8 + half, z1 - z0
    inner = half * 2 - chamfer * 2
    long_side = uv(side, 0, 0, length, min(side[3], inner))
    return [
        element(f"{name}_wide", [lo, lo + chamfer, z0], [hi, hi - chamfer, z1], {
            "east": long_side, "west": long_side,
            "north": uv(end, 0, 0, end[2], min(end[3], inner)), "south": uv(end, 0, 0, end[2], min(end[3], inner))}),
        element(f"{name}_tall", [lo + chamfer, lo, z0], [hi - chamfer, hi, z1], {
            "up": uv(top, 0, 0, min(top[2], inner), length), "down": long_side,
            "north": uv(end, 0, 0, min(end[2], inner), end[3]), "south": uv(end, 0, 0, min(end[2], inner), end[3])}),
    ]


def _band(name: str, z: float) -> list[dict]:
    half, chamfer = 3.75, 1
    lo, hi = 8 - half, 8 + half
    inner = half * 2 - chamfer * 2
    return [
        element(f"{name}_wide", [lo, lo + chamfer, z], [hi, hi - chamfer, z + 1], {
            "east": uv(BAND, 0, 0, 1, inner), "west": uv(BAND, 0, 0, 1, inner),
            "north": uv(BAND, 0, 0, 7.5, inner), "south": uv(BAND, 0, 0, 7.5, inner)}),
        element(f"{name}_tall", [lo + chamfer, lo, z], [hi - chamfer, hi, z + 1], {
            "up": uv(BAND, 0, 0, inner, 1), "down": uv(BAND, 0, 0, inner, 1),
            "north": uv(BAND, 0, 0, inner, 7.5), "south": uv(BAND, 0, 0, inner, 7.5)}),
    ]


def _collar(name: str, frm, to) -> dict:
    """Part of the output collar around the 6-px opening; its back face samples the collar ring where it sits."""
    du, dv = frm[0] - 4, 12 - to[1]
    w, h = to[0] - frm[0], to[1] - frm[1]
    side = uv(COLLAR_SIDE, 0, 0, 2, max(w, h))
    return element(name, frm, to, {"south": uv(COLLAR_FACE, du, dv, w, h), "north": uv(COLLAR_FACE, du, dv, w, h),
                                   "east": side, "west": side, "up": side, "down": side})


TURBINE_ELEMENTS = [
    element("foot", [3, 0, 5], [13, 2, 14], {"up": uv(FOOT_TOP), "down": uv(FOOT_TOP),
                                            **{s: uv(FOOT_SIDE) for s in ("north", "south", "east", "west")}}),
    element("pylon", [6.5, 2, 7], [9.5, 5, 12], {"east": uv(PYLON_SIDE), "west": uv(PYLON_SIDE),
                                                 "north": uv(PYLON_FRONT), "south": uv(PYLON_FRONT)}),
    # Square core: its edges show between the chamfers; its back is the dark opening behind the collar.
    element("core", [5, 5, 4], [11, 11, 14], {
        **{s: uv(NACELLE_EDGE, 0, 0, 1, 9) for s in ("east", "west", "up", "down")},
        "south": uv(COLLAR_FACE, 1, 1, 6, 6), "north": uv(NACELLE_END)}),
    *_octagon("nacelle", 4.5, 13.5, 3.5, 1, ROUND_SIDE, ROUND_TOP, NACELLE_END),
    *_band("band_front", 5.5),
    *_band("band_back", 11.5),
    _collar("collar_top", [4, 11, 14], [12, 12, 16]),
    _collar("collar_bottom", [4, 4, 14], [12, 5, 16]),
    _collar("collar_west", [4, 5, 14], [5, 11, 16]),
    _collar("collar_east", [11, 5, 14], [12, 11, 16]),
    element("lifting_lug", [7, 11.5, 8], [9, 12.5, 10], {
        "up": uv(LUG), **{s: uv(LUG, 0, 0, 2, 1) for s in ("north", "south", "east", "west")}}),
]

_ROOT = [8, 8, 2.5]


def _blade(name: str, frm, to, faces: dict, axis: str, angle: float) -> dict:
    return _rotated(element(name, frm, to, faces), axis, angle, _ROOT)


def _vertical_faces(tip_up: bool) -> dict:
    v = uv(BLADE, 0, 0, 3, 5.5)
    v = v if tip_up else _flip_v(v)
    edge = _edge(tip_up)
    return {"north": v, "south": v, "east": edge, "west": edge,
            ("up" if tip_up else "down"): uv(PAINT_TIP, 0, 0, 3, 1)}


# Kept near the blade helpers: the red tip seen end-on.
PAINT_TIP = (BLADE[0], BLADE[1], 3, 1)


def _flip_v(u: list[float]) -> list[float]:
    return [u[0], u[3], u[2], u[1]]


def _flip_u(u: list[float]) -> list[float]:
    return [u[2], u[1], u[0], u[3]]


def _edge(tip_first: bool) -> list[float]:
    """Edge of a vertical blade: the shaded column of BLADE, tip first."""
    column = uv(BLADE, 2, 0, 1, 5.5)
    return column if tip_first else _flip_v(column)


def _horizontal_faces(tip_right: bool) -> dict:
    h = uv(BLADE_H, 0.5, 0, 5.5, 3)
    edge = uv(BLADE_EDGE, 0.5, 0, 5.5, 1)
    h, edge = (h, edge) if tip_right else (_flip_u(h), _flip_u(edge))
    return {"north": _flip_u(h), "south": h, "up": edge, "down": edge,
            ("east" if tip_right else "west"): uv(PAINT_TIP, 0, 0, 1, 3)}


ROTOR_ELEMENTS = [
    element("hub", [6, 6, 1], [10, 10, 4], {
        "north": uv(HUB_FRONT), **{s: uv(HUB_SIDE) for s in ("east", "west", "up", "down")}}),
    element("nose", [7, 7, 0], [9, 9, 1], {
        "north": uv(NOSE_FRONT), **{s: uv(NOSE_SIDE) for s in ("east", "west", "up", "down")}}),
    _blade("blade_top", [6.5, 10, 2], [9.5, 15.5, 3], _vertical_faces(True), "y", 22.5),
    _blade("blade_bottom", [6.5, 0.5, 2], [9.5, 6, 3], _vertical_faces(False), "y", -22.5),
    _blade("blade_east", [10, 6.5, 2], [15.5, 9.5, 3], _horizontal_faces(True), "x", 22.5),
    _blade("blade_west", [0.5, 6.5, 2], [6, 9.5, 3], _horizontal_faces(False), "x", -22.5),
]

# Create's half shaft from the centre to the back face; it spins with the rotor (same partial model), drawn like
# the alternator's through shaft.
HALF_SHAFT = [{"name": "shaft", "from": [6, 6, 8], "to": [10, 10, 16], "faces": {
    **{f: {"uv": [6, 0, 10, 8], "texture": "#axis"} for f in ("up", "down", "east", "west")},
    "south": {"uv": [6, 6, 10, 10], "texture": "#axis_top"}}}]


def small_tidal_turbine() -> None:
    paint().save("block/small_tidal_turbine.png")


GENERATORS = [small_tidal_turbine]
