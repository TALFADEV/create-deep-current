"""Creative power source and load (test blocks, GDD §12), drawn with the base materials of tools/textures/dc.

Like Create's creative items, they wear a magenta frame over a night-blue checker, so nobody mistakes them for
survival machines. Each is a full block (connectors go on any face) with a 32x32 atlas:
- the source shows a brass-ringed porthole on each side, full of the cyan glow of the HV core (full-bright);
- the load shows a brass-framed window on each side onto two rows of red-hot heating coils (full-bright): it
  burns the power it draws.
The top carries a bolted brass plate (source) or vent slots (load); the bottom is the plain checker.
"""

from __future__ import annotations

from dc.canvas import Canvas
from dc.details import small_rivet
from dc.modelling import element, emissive, uv
from dc.palette import BRASS, COPPER, CREATIVE, CREATIVE_NIGHT, GLASS, HV_GLOW, OUTLINE, PAINT_RED

SIDE = (0, 0, 16, 16)
TOP = (16, 0, 16, 16)
BOTTOM = (0, 16, 16, 16)
CORE_GLOW = (16, 16, 6, 6)         # source: what shows through the porthole (world 5..11)
COILS = (16, 22, 8, 4)             # load: what shows through the window (world x 4..12, y 6..10)


def _frame(c: Canvas, x0: int, y0: int) -> None:
    """16x16 face: 2-px magenta frame with brass corner rivets around a recessed night-blue checker."""
    x1, y1 = x0 + 15, y0 + 15
    c.bevel(x0, y0, x1, y1, CREATIVE[5], CREATIVE[2], CREATIVE[6])
    c.bevel(x0 + 1, y0 + 1, x1 - 1, y1 - 1, CREATIVE[4], CREATIVE[3])
    for y in range(2, 14):
        for x in range(2, 14):
            c.px(x0 + x, y0 + y, CREATIVE_NIGHT[2 if (x // 2 + y // 2) % 2 else 3])
    # Recess: the frame shades the inner top and left edges; the bottom and right inner edges catch the light.
    c.rect(x0 + 2, y0 + 2, x0 + 13, y0 + 2, CREATIVE_NIGHT[0])
    c.rect(x0 + 2, y0 + 3, x0 + 2, y0 + 13, CREATIVE_NIGHT[1])
    c.rect(x0 + 3, y0 + 13, x0 + 13, y0 + 13, CREATIVE_NIGHT[4])
    for rx, ry in ((1, 0), (14, 0), (1, 13), (14, 13)):
        small_rivet(c, x0 + rx, y0 + ry + 1, BRASS)


def _bezel(c: Canvas, x0: int, y0: int, x1: int, y1: int) -> None:
    """Brass ring around a window, standing out of the checker: lit top-left, shaded bottom-right."""
    c.bevel(x0, y0, x1, y1, BRASS[5], BRASS[1], BRASS[6])
    c.bevel(x0 + 1, y0 + 1, x1 - 1, y1 - 1, BRASS[3], BRASS[4])


def paint(source: bool) -> Canvas:
    c = Canvas(32, 32)
    for region in (SIDE, TOP, BOTTOM):
        _frame(c, region[0], region[1])

    x0, y0 = SIDE[:2]
    if source:
        _bezel(c, x0 + 4, y0 + 4, x0 + 11, y0 + 11)
        c.rect(x0 + 6, y0 + 6, x0 + 9, y0 + 9, GLASS[1])          # under the glow element
    else:
        _bezel(c, x0 + 3, y0 + 5, x0 + 12, y0 + 10)
        c.rect(x0 + 4, y0 + 6, x0 + 11, y0 + 9, OUTLINE)

    x0, y0 = TOP[:2]
    if source:
        # Bolted brass terminal plate.
        c.rect(x0 + 5, y0 + 5, x0 + 10, y0 + 10, BRASS[4])
        c.bevel(x0 + 5, y0 + 5, x0 + 10, y0 + 10, BRASS[5], BRASS[1], BRASS[6])
        for bx, by in ((6, 6), (9, 6), (6, 8), (9, 8)):
            small_rivet(c, x0 + bx, y0 + by, COPPER)
    else:
        # Vent slots letting the heat out: dark slot, lit lip below.
        for y in (5, 8, 11):
            c.rect(x0 + 4, y0 + y - 1, x0 + 11, y0 + y - 1, OUTLINE)
            c.rect(x0 + 4, y0 + y, x0 + 11, y0 + y, CREATIVE[4])

    if source:
        # HV core: rings brightening to the centre, a glint on the glass at the top left.
        x0, y0, w, h = CORE_GLOW
        for y in range(h):
            for x in range(w):
                ring = min(x, y, w - 1 - x, h - 1 - y)
                c.px(x0 + x, y0 + y, HV_GLOW[(3, 5, 6)[ring]])
        c.px(x0 + 1, y0 + 1, HV_GLOW[6])
        c.px(x0 + w - 2, y0 + h - 2, HV_GLOW[4])
    else:
        # Two coils seen from the side: slanted turns, white-hot on the wire, red in its glow, dim between turns.
        x0, y0, w, h = COILS
        for y in range(h):
            for x in range(w):
                phase = (x + y) % 3
                color = (COPPER[6] if y in (0, 2) else PAINT_RED[5], PAINT_RED[3], PAINT_RED[1])[phase]
                c.px(x0 + x, y0 + y, color)
    return c


# --- Model ---------------------------------------------------------------------------------------------------------

SIDES = ("north", "south", "east", "west")
_OUT = 0.02     # the glow sits a hair in front of the face


def _cube() -> dict:
    e = element("casing", [0, 0, 0], [16, 16, 16], {
        **{s: uv(SIDE) for s in SIDES}, "up": uv(TOP), "down": uv(BOTTOM)})
    for side, face in e["faces"].items():
        face["cullface"] = side
    return e


def _windows(name: str, region, x0: float, y0: float, x1: float, y1: float) -> list[dict]:
    """Full-bright panes in front of the window on each side (x0..x1 across the face, y0..y1 up it; the windows
    are centred, so the pane reads the same whichever way a face runs its u)."""
    face = uv(region)
    panes = [
        element(f"{name}_north", [16 - x1, y0, -_OUT], [16 - x0, y1, 0], {"north": face}),
        element(f"{name}_south", [x0, y0, 16], [x1, y1, 16 + _OUT], {"south": face}),
        element(f"{name}_west", [-_OUT, y0, x0], [0, y1, x1], {"west": face}),
        element(f"{name}_east", [16, y0, 16 - x1], [16 + _OUT, y1, 16 - x0], {"east": face}),
    ]
    for pane in panes:
        for side, f in pane["faces"].items():
            f["cullface"] = side
        emissive(pane)
    return panes


SOURCE_ELEMENTS = [_cube(), *_windows("core", CORE_GLOW, 5, 5, 11, 11)]
LOAD_ELEMENTS = [_cube(), *_windows("coils", COILS, 4, 6, 12, 10)]


def creative_blocks() -> None:
    paint(True).save("block/creative_power_source.png")
    paint(False).save("block/creative_power_consumer.png")


GENERATORS = [creative_blocks]
