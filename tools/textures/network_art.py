"""Low-voltage network (Alpha): connector, cable and cable coil, drawn with the base materials of tools/textures/dc.

Voltage code: LV is marked in safety yellow (connector collar) and orange (cable tracer); HV and EHV will get their
own colours and patterns.

Connector, designed facing up (the blockstate turns it toward the face it sits on):
- riveted steel wall plate;
- a sealed collar painted safety yellow (the LV mark);
- a copper core inside three dark insulator discs stepping in like a pagoda;
- a brass clamp and a copper lug on top, where the cable hangs (anchor at 11 px, see ConnectorBlock).
"""

from __future__ import annotations

from PIL import Image

from dc.canvas import Canvas
from dc.details import small_rivet
from dc.modelling import element, uv
from dc.palette import BRASS, COPPER, HADAL, OUTLINE, PAINT_YELLOW, STEEL

# --- Connector atlas (32x32) -----------------------------------------------------------------------------------

PLATE_TOP = (0, 0, 8, 8)
PLATE_SIDE = (0, 8, 8, 1)
COLLAR_TOP = (8, 0, 6, 6)
COLLAR_SIDE = (8, 6, 6, 2)
DISC_TOP = (14, 0, 7, 7)
DISC_SIDE = (14, 7, 7, 1)
CORE_SIDE = (21, 0, 3, 7)
CLAMP_TOP = (24, 0, 4, 4)
CLAMP_SIDE = (24, 4, 4, 1)
LUG_TOP = (28, 0, 2, 2)
LUG_SIDE = (28, 2, 2, 1)


def connector_atlas() -> Canvas:
    c = Canvas(32, 32)

    x0, y0, w, h = PLATE_TOP
    c.vertical_light(x0, y0, x0 + w - 1, y0 + h - 1, STEEL, 4, 3)
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, STEEL[5], STEEL[1], STEEL[6])
    for rx, ry in ((1, 0), (w - 2, 0), (1, h - 3), (w - 2, h - 3)):
        small_rivet(c, x0 + rx, y0 + ry + 1, BRASS)
    x0, y0, w, _ = PLATE_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, STEEL[2])
    c.px(x0, y0, STEEL[4])

    # Sealed collar, painted safety yellow: the LV mark.
    x0, y0, w, h = COLLAR_TOP
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[3])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, PAINT_YELLOW[5], PAINT_YELLOW[1], PAINT_YELLOW[6])
    x0, y0, w, _ = COLLAR_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, PAINT_YELLOW[5])
    c.rect(x0, y0 + 1, x0 + w - 1, y0 + 1, PAINT_YELLOW[2])
    c.px(x0 + 1, y0 + 1, STEEL[5])           # a worn spot showing the steel
    c.px(x0 + w - 2, y0, PAINT_YELLOW[6])

    # Insulator disc: dark ceramic, lit rim, a groove ring, glint on the top-left.
    x0, y0, w, h = DISC_TOP
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, HADAL[4])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, HADAL[6], HADAL[2])
    c.bevel(x0 + 1, y0 + 1, x0 + w - 2, y0 + h - 2, HADAL[3], HADAL[5])
    c.px(x0 + 1, y0 + 1, STEEL[6])
    x0, y0, w, _ = DISC_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, HADAL[4])
    c.px(x0, y0, HADAL[6])
    c.px(x0 + w - 1, y0, HADAL[1])

    # Copper core between the discs: round shading.
    x0, y0, w, h = CORE_SIDE
    for y in range(h):
        c.px(x0, y0 + y, COPPER[5])
        c.px(x0 + 1, y0 + y, COPPER[4])
        c.px(x0 + 2, y0 + y, COPPER[2])

    # Brass clamp and copper lug.
    x0, y0, w, h = CLAMP_TOP
    c.rect(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[4])
    c.bevel(x0, y0, x0 + w - 1, y0 + h - 1, BRASS[6], BRASS[2])
    x0, y0, w, _ = CLAMP_SIDE
    c.rect(x0, y0, x0 + w - 1, y0, BRASS[3])
    c.px(x0, y0, BRASS[5])
    x0, y0, _, _ = LUG_TOP
    c.px(x0, y0, COPPER[6])
    c.px(x0 + 1, y0, COPPER[5])
    c.px(x0, y0 + 1, COPPER[4])
    c.px(x0 + 1, y0 + 1, COPPER[3])
    x0, y0, _, _ = LUG_SIDE
    c.px(x0, y0, COPPER[4])
    c.px(x0 + 1, y0, COPPER[2])
    return c


SIDES = ("north", "south", "east", "west")


def _round(name: str, frm, to, top, side, sub_top=None) -> dict:
    """Square part of the stack: its top samples the centre of the top region when it is smaller."""
    w = to[0] - frm[0]
    top_uv = uv(top) if sub_top is None else uv(top, sub_top, sub_top, w, w)
    side_uv = uv(side, 0, 0, w, side[3])
    return element(name, frm, to, {"up": top_uv, "down": top_uv, **{s: side_uv for s in SIDES}})


CONNECTOR_ELEMENTS = [
    _round("plate", [4, 0, 4], [12, 1, 12], PLATE_TOP, PLATE_SIDE),
    _round("collar", [5, 1, 5], [11, 3, 11], COLLAR_TOP, COLLAR_SIDE),
    element("core", [6.5, 3, 6.5], [9.5, 10, 9.5], {s: uv(CORE_SIDE) for s in SIDES}),
    _round("disc_low", [4.5, 4, 4.5], [11.5, 5, 11.5], DISC_TOP, DISC_SIDE),
    _round("disc_mid", [5, 6, 5], [11, 7, 11], DISC_TOP, DISC_SIDE, sub_top=0.5),
    _round("disc_high", [5.5, 8, 5.5], [10.5, 9, 10.5], DISC_TOP, DISC_SIDE, sub_top=1),
    _round("clamp", [6, 10, 6], [10, 11, 10], CLAMP_TOP, CLAMP_SIDE),
    _round("lug", [7, 11, 7], [9, 12, 9], LUG_TOP, LUG_SIDE),
]


# --- Cable (16x16, u across the sheath, v along it) -------------------------------------------------------------

def cable_lv() -> Canvas:
    """Dark rubber sheath, rounded shading across, an orange tracer wound around it (helix along v)."""
    c = Canvas()
    across = [1, 2, 2, 3, 3, 4, 4, 5, 5, 4, 4, 3, 3, 2, 2, 1]
    for u, tone in enumerate(across):
        for v in range(16):
            c.px(u, v, HADAL[tone])
    for v in range(16):
        u = (v * 3 // 2) % 16
        for du, ramp_index in ((0, 4), (1, 3)):
            x = (u + du) % 16
            # The tracer darkens toward the edges of the sheath, like the rubber under it.
            tone = max(1, ramp_index - (1 if across[x] <= 2 else 0))
            c.px(x, v, COPPER[tone])
    c.rect(0, 0, 0, 15, OUTLINE)
    c.rect(15, 0, 15, 15, OUTLINE)
    return c


# --- Cable coil item (16x16) -------------------------------------------------------------------------------------

# Pixel map of the coil icon: each character is one pixel (see _COIL_KEY); '.' stays transparent.
_COIL = [
    "................",
    ".obbo......obbo.",
    ".oBbo......oBbo.",
    ".oSdoCCrCCCoSdo.",
    ".oSdocckcckoSdo.",
    ".oSdoCCCCrCoSdo.",
    ".ohhocckcckohho.",
    ".ohhoCrCCCCohho.",
    ".oSdocckcckoSdo.",
    ".oSdoCCCrCCoSdo.",
    ".oSdocckcckoSdo.",
    ".oSdoCCCCCroSdo.",
    ".oSdooooCooOSdo.",
    ".oBbo...oCo.Bbo.",
    ".obbo....oCo.bo.",
    "..........op....",
]
_COIL_KEY = {
    "o": OUTLINE, "O": OUTLINE,
    "S": STEEL[5], "d": STEEL[3],
    "B": BRASS[5], "b": BRASS[3], "h": BRASS[4],
    "C": HADAL[5], "c": HADAL[3], "k": HADAL[1],
    "r": COPPER[4], "p": COPPER[6],
}


def lv_cable_coil() -> Canvas:
    """Spool seen from the side: thick steel flanges with brass rims and hub, cable wound in turns with the
    orange LV tracer, the loose end hanging with its copper core. Dark outline for inventory readability."""
    c = Canvas()
    for y, row in enumerate(_COIL):
        assert len(row) == 16, f"coil row {y} has {len(row)} pixels"
        for x, key in enumerate(row):
            if key != ".":
                c.px(x, y, _COIL_KEY[key])
    return c


def write() -> None:
    connector_atlas().save("block/lv_connector.png")
    cable_lv().save("entity/cable_lv.png")
    lv_cable_coil().save("item/lv_cable_coil.png")


GENERATORS = [write]


def preview_images() -> dict[str, Image.Image]:
    return {"connector": connector_atlas().img, "cable": cable_lv().img, "coil": lv_cable_coil().img}
