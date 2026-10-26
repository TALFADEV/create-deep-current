"""Textures of the electrical grid: LV connector, creative block belts, LV cable coil and cable.

The LV connector is the "Create bracket" design chosen by the author: andesite foot, dark rubber insulator with
two sheds and an LV ring, copper lug. Its parts share one 16x16 texture; CONNECTOR_PARTS gives each part's box
(in pixels, facing up) and where its top and side faces sit in that texture. tools/models/generate.py builds the
model from the same table.
"""

from __future__ import annotations

from PIL import Image

import palette as p
from parts import cylinder_side, side_band, top_plate
from pixels import canvas, fill, pick, save


class Part:
    def __init__(self, name, frm, to, top, side, top_at, side_at, seed=0):
        self.name = name
        self.frm = frm
        self.to = to
        self.top = top
        self.side = side
        self.top_at = top_at
        self.side_at = side_at
        self.seed = seed

    @property
    def size(self) -> tuple[int, int, int]:
        """Texture footprint: width, depth, height in whole pixels."""
        return (round(self.to[0] - self.frm[0]), round(self.to[2] - self.frm[2]),
                round(self.to[1] - self.frm[1]))

    def top_uv(self) -> list[int]:
        w, d, _ = self.size
        x, y = self.top_at
        return [x, y, x + w, y + d]

    def side_uv(self) -> list[int]:
        w, _, h = self.size
        x, y = self.side_at
        return [x, y, x + w, y + h]


# Layout (16x16): foot top 0..8 x 0..8, foot side below it; shed top and side to its right; insulator side,
# lug top/side and insulator top in the lower right.
CONNECTOR_PARTS = [
    Part("foot", [4, 0, 4], [12, 2, 12], top_plate(p.ANDESITE, bolts=True), side_band(p.ANDESITE),
         (0, 0), (0, 8)),
    Part("insulator", [6.5, 2, 6.5], [9.5, 10, 9.5], top_plate(p.RUBBER), cylinder_side(p.RUBBER, mark_row=1),
         (11, 13), (8, 8)),
    Part("shed_low", [5, 3, 5], [11, 4, 11], top_plate(p.RUBBER), side_band(p.RUBBER), (8, 0), (8, 6), seed=3),
    Part("shed_high", [5, 6, 5], [11, 7, 11], top_plate(p.RUBBER), side_band(p.RUBBER), (8, 0), (8, 6), seed=3),
    Part("lug", [6.5, 10, 6.5], [9.5, 12, 9.5], top_plate(p.COPPER, hole=True), side_band(p.COPPER),
         (11, 8), (11, 11)),
]


def lv_connector() -> None:
    img = canvas()
    painted = set()
    for part in CONNECTOR_PARTS:
        w, d, h = part.size
        for at, painter, height, kind in ((part.top_at, part.top, d, "top"), (part.side_at, part.side, h, "side")):
            if (at, kind) in painted:
                continue  # parts sharing an area (the two sheds) are painted once
            painted.add((at, kind))
            piece = canvas()
            painter(piece, w, height, part.seed + (7 if kind == "side" else 0))
            img.alpha_composite(piece.crop((0, 0, w, height)), at)
    save(img, "block/lv_connector.png")


def creative_belts() -> None:
    """The belt around the creative blocks: copper for the source, andesite for the load. 16 x 2 bands."""
    for name, ramp in (("source", p.COPPER), ("load", p.ANDESITE)):
        img = canvas()
        side_band(ramp)(img, 16, 2, seed=11)
        save(img, f"block/creative_belt_{name}.png")


def lv_cable_coil() -> None:
    """Item: wooden spool in Create's tones, dark wood flanges with andesite rims, rubber cable wound in turns."""
    img = canvas()
    for y in range(4, 12):
        fill(img, 4, y, 11, y, p.RUBBER, 3 if y % 2 == 0 else 1, seed=y, noise=0.25)
        img.putpixel((4, y), pick(p.RUBBER, 0))
        img.putpixel((11, y), pick(p.RUBBER, 1))
    for x in (6, 9):
        img.putpixel((x, 7), pick(p.MARK_LV, 2))
    for x0 in (2, 12):
        fill(img, x0, 2, x0 + 1, 13, p.DARK_WOOD, 4, seed=x0)
        for y in range(2, 14):
            img.putpixel((x0, y), pick(p.DARK_WOOD, 5))
        for y in (2, 13):
            img.putpixel((x0, y), pick(p.ANDESITE, 4))
            img.putpixel((x0 + 1, y), pick(p.ANDESITE, 2))
        img.putpixel((x0 + 1, 7), pick(p.ANDESITE, 3))
        img.putpixel((x0 + 1, 8), pick(p.ANDESITE, 1))
    for x, y in ((12, 12), (13, 13), (14, 13)):
        img.putpixel((x, y), pick(p.RUBBER, 2))
    img.putpixel((15, 13), pick(p.COPPER, 4))
    save(img, "item/lv_cable_coil.png")


def cable_lv() -> None:
    """Cable sheath, u across the width: round shading in Create's belt rubber, thin LV stripe on each side."""
    img = canvas()
    across = [0, 1, 1, 2, 2, 3, 4, 5, 5, 4, 3, 2, 2, 1, 1, 0]
    for x, index in enumerate(across):
        for y in range(16):
            img.putpixel((x, y), pick(p.RUBBER, index))
    for y in range(16):
        img.putpixel((3, y), pick(p.MARK_LV, 1))
        img.putpixel((12, y), pick(p.MARK_LV, 1))
    save(img, "entity/cable_lv.png")


# Superseded: the connector, coil and cable are drawn in network_art.py, the creative blocks in creative_art.py.
GENERATORS: list = []
