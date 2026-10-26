"""Base materials of Create: Deep Current, each drawn pixel by pixel on a 16x16 canvas.

They are the references every machine borrows from, so that brass, copper or steel look the same everywhere.
Light comes from the top left. Written to textures/block/material/.
"""

from __future__ import annotations

from .canvas import Canvas
from .details import blob, chip, hex_bolt, rivet, scratch, seam, small_rivet
from .palette import (ABYSSAL, BRASS, COPPER, GLASS, HADAL, OUTLINE, PAINT_RED, PAINT_YELLOW, STEEL, VERDIGRIS)


def brass() -> Canvas:
    """Polished brass plate: warm light from above, two diagonal reflections, a few worn specks."""
    c = Canvas()
    c.vertical_light(0, 0, 15, 15, BRASS, 4, 3)
    # One broad soft reflection (two tones) and a faint second one, instead of hard stripes.
    c.sheen((1, 12), 11, [BRASS[5], BRASS[5], BRASS[6]])
    c.sheen((8, 15), 7, [BRASS[5]])
    c.sheen((0, 8), 8, [BRASS[3]])
    c.bevel(0, 0, 15, 15, BRASS[5], BRASS[1], BRASS[6])
    c.rect(1, 14, 14, 14, BRASS[2])
    c.rect(14, 1, 14, 14, BRASS[2])
    c.pixels([(3, 11), (12, 4), (9, 12), (4, 3)], BRASS[2])
    return c


def copper() -> Canvas:
    """Two copper plates riveted at their corners, each with its own short reflection."""
    c = Canvas()
    for y0, y1 in ((0, 7), (8, 15)):
        c.vertical_light(0, y0, 15, y1, COPPER, 4, 3)
        c.bevel(0, y0, 15, y1, COPPER[5], COPPER[1], COPPER[6])
    c.sheen((3, 6), 5, [COPPER[5]])
    c.sheen((5, 14), 6, [COPPER[5], COPPER[6]])
    for x, y in ((1, 2), (11, 2), (1, 10), (11, 10)):
        rivet(c, x, y, COPPER)
    c.pixels([(7, 4), (9, 12)], COPPER[2])
    return c


def oxidized_copper() -> Canvas:
    """The copper plates after years at sea: verdigris patches around the rivets, in the seam and along the
    lower edges, where water lingers."""
    c = copper()
    blob(c, 0, 1, [".##.", "####", "###.", ".#.."], VERDIGRIS)       # top-left rivet
    blob(c, 10, 1, ["..##.", ".####", "####.", "..#.."], VERDIGRIS)  # top-right rivet
    blob(c, 3, 6, ["..###..", ".#####.", "####.##"], VERDIGRIS)     # seam, left
    blob(c, 10, 6, [".###", "####", ".##."], VERDIGRIS)              # seam, right
    blob(c, 0, 10, ["##.", "###", ".##", ".#."], VERDIGRIS)          # lower-left rivet
    blob(c, 5, 13, [".####..", "#######"], VERDIGRIS)               # crust along the bottom
    blob(c, 12, 12, [".##", "###", "##."], VERDIGRIS)                # lower-right corner
    return c


def riveted_steel() -> Canvas:
    """Tier-2 hull plate: cool grey steel, a rivet in each corner, two scratches."""
    c = Canvas()
    c.vertical_light(0, 0, 15, 15, STEEL, 4, 3)
    c.bevel(0, 0, 15, 15, STEEL[5], STEEL[1], STEEL[6])
    c.rect(1, 14, 14, 14, STEEL[2])
    c.rect(14, 1, 14, 13, STEEL[2])
    for x, y in ((2, 2), (10, 2), (2, 10), (10, 10)):
        rivet(c, x, y, STEEL)
    scratch(c, [(5, 6), (6, 7), (7, 8)], STEEL)
    scratch(c, [(9, 5), (10, 5)], STEEL)
    c.pixels([(8, 12), (4, 9)], STEEL[2])
    return c


def abyssal_alloy() -> Canvas:
    """Tier-3 alloy from the nodules: dark petrol-blue metal in a recessed panel, with cold crystal glints."""
    c = Canvas()
    c.vertical_light(0, 0, 15, 15, ABYSSAL, 3, 2)
    c.inset(2, 2, 13, 13, ABYSSAL[1], ABYSSAL[4])
    c.vertical_light(3, 3, 12, 12, ABYSSAL, 3, 2)
    c.sheen((3, 12), 9, [ABYSSAL[4]])
    c.sheen((7, 12), 5, [ABYSSAL[3]])
    for x, y in ((5, 5), (10, 9), (6, 10)):
        c.px(x, y, ABYSSAL[6])
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            c.px(x + dx, y + dy, ABYSSAL[5])
    c.bevel(0, 0, 15, 15, ABYSSAL[5], ABYSSAL[0], ABYSSAL[6])
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        c.px(x, y, ABYSSAL[4])
    return c


def hadal_hull() -> Canvas:
    """Tier-4 hull: near-black armour plates laid like bricks, each lit along its top edge."""
    c = Canvas()
    c.rect(0, 0, 15, 15, OUTLINE)
    for row, y0 in enumerate((0, 4, 8, 12)):
        offset = 0 if row % 2 == 0 else -4
        for x0 in range(offset, 16, 8):
            x1 = x0 + 7
            y1 = y0 + 3
            c.rect(max(0, x0), y0, min(15, x1 - 1), y1 - 1, HADAL[3])
            for x in range(max(0, x0), min(16, x1)):
                c.px(x, y0, HADAL[5])
                c.px(x, y1 - 1, HADAL[1])
            if x0 >= 0:
                c.px(x0, y0, HADAL[6])
                for y in range(y0 + 1, y1 - 1):
                    c.px(x0, y, HADAL[4])
    c.pixels([(3, 2), (11, 6), (6, 10), (13, 14)], HADAL[2])
    c.pixels([(9, 1), (2, 9), (12, 9)], HADAL[6])
    return c


def reinforced_glass() -> Canvas:
    """Porthole glass in a riveted steel frame: tinted, see-through glass with two reflections."""
    c = Canvas()
    # Steel frame, 2 px.
    c.rect(0, 0, 15, 15, STEEL[3])
    c.bevel(0, 0, 15, 15, STEEL[5], STEEL[1], STEEL[6])
    c.bevel(1, 1, 14, 14, STEEL[4], STEEL[2])
    # Glass: translucent, lighter at the top, shadow under the frame.
    for y in range(2, 14):
        tone = GLASS[4] if y < 5 else GLASS[3] if y < 10 else GLASS[2]
        c.rect(2, y, 13, y, tone, alpha=110)
    c.rect(2, 2, 13, 2, GLASS[1], alpha=170)
    c.rect(2, 3, 2, 13, GLASS[1], alpha=150)
    # Reflections.
    for step in range(6):
        c.px(4 + step, 9 - step, GLASS[6], alpha=210)
        c.px(5 + step, 9 - step, GLASS[5], alpha=170)
    for step in range(3):
        c.px(8 + step, 12 - step, GLASS[6], alpha=190)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14)):
        small_rivet(c, x, y - 1 if y > 1 else y, STEEL)
    return c


def antifouling_red() -> Canvas:
    """Anti-fouling paint on steel, chipped at the edges where tools and currents rub."""
    c = Canvas()
    c.vertical_light(0, 0, 15, 15, PAINT_RED, 4, 3)
    c.bevel(0, 0, 15, 15, PAINT_RED[5], PAINT_RED[1], PAINT_RED[6])
    c.rect(1, 14, 14, 14, PAINT_RED[2])
    c.rect(14, 1, 14, 13, PAINT_RED[2])
    for x, y in ((2, 2), (10, 2), (2, 10), (10, 10)):
        rivet(c, x, y, PAINT_RED)
    chip(c, [(1, 5), (1, 6), (2, 6)], PAINT_RED, STEEL)
    chip(c, [(6, 14), (7, 14), (7, 13)], PAINT_RED, STEEL)
    chip(c, [(14, 8), (14, 9)], PAINT_RED, STEEL)
    chip(c, [(8, 6), (9, 6)], PAINT_RED, STEEL)
    return c


def safety_yellow() -> Canvas:
    """Safety-yellow paint on two steel panels, worn along the seam and the lower edge."""
    c = Canvas()
    for y0, y1 in ((0, 7), (8, 15)):
        c.vertical_light(0, y0, 15, y1, PAINT_YELLOW, 4, 3)
        c.bevel(0, y0, 15, y1, PAINT_YELLOW[5], PAINT_YELLOW[1], PAINT_YELLOW[6])
    seam(c, 7, 1, 14, PAINT_YELLOW)
    for x in (2, 12):
        hex_bolt(c, x, 3, STEEL)
        hex_bolt(c, x, 11, STEEL)
    chip(c, [(6, 6), (7, 6), (8, 5)], PAINT_YELLOW, STEEL)
    chip(c, [(4, 14), (5, 14), (10, 13), (11, 14)], PAINT_YELLOW, STEEL)
    chip(c, [(1, 9), (1, 10)], PAINT_YELLOW, STEEL)
    return c


MATERIALS = {
    "brass": ("Laiton", brass),
    "copper": ("Cuivre", copper),
    "oxidized_copper": ("Cuivre oxydé", oxidized_copper),
    "riveted_steel": ("Acier riveté", riveted_steel),
    "abyssal_alloy": ("Alliage abyssal", abyssal_alloy),
    "hadal_hull": ("Coque hadale", hadal_hull),
    "reinforced_glass": ("Verre renforcé", reinforced_glass),
    "antifouling_red": ("Peinture rouge", antifouling_red),
    "safety_yellow": ("Peinture jaune", safety_yellow),
}


def write_materials() -> None:
    for name, (_, draw) in MATERIALS.items():
        draw().save(f"block/material/{name}.png")


GENERATORS = [write_materials]
