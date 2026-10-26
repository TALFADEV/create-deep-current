"""Textures of the industrial direction chosen by the author: dark riveted iron housings with thick frames,
copper windings and terminals, brass trims (references: Crafts & Additions, New Age, Diesel Generators,
Steam 'n' Rails, Big Cannons; colours only, nothing copied).

Two kinds of textures:
- material tiles (speckled, no edges), for small parts mapped with auto UV;
- 16x16 casing faces (frame, rivets, and a centre: plate, window on windings, vents or shaft ring).
"""

from __future__ import annotations

from PIL import Image

import palette as p
from pixels import bevel, canvas, fill, pick, save

TILES = {
    "dark_iron": (p.DARK_IRON, 3),
    "heavy_iron": (p.HEAVY_IRON, 3),
    "copper": (p.COPPER, 3),
    "brass": (p.BRASS, 3),
    "mark_lv": (p.MARK_LV, 2),
}


def material_tiles() -> None:
    for name, (ramp, base) in TILES.items():
        img = canvas()
        fill(img, 0, 0, 15, 15, ramp, base, seed=len(name) * 13, noise=0.3)
        save(img, f"block/{name}_tile.png")
    riveted = canvas()
    fill(riveted, 0, 0, 15, 15, p.DARK_IRON, 3, seed=5, noise=0.3)
    for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
        _rivet(riveted, x, y)
    save(riveted, "block/dark_iron_riveted_tile.png")


def _rivet(img: Image.Image, x: int, y: int) -> None:
    img.putpixel((x, y), pick(p.BRASS, 3))
    img.putpixel((x - 1, y - 1), pick(p.BRASS, 5))
    img.putpixel((x + 1, y + 1), pick(p.DARK_IRON, 0))


def _windings(img: Image.Image, x0: int, y0: int, x1: int, y1: int) -> None:
    for y in range(y0, y1 + 1):
        fill(img, x0, y, x1, y, p.COPPER, 4 if (y - y0) % 2 == 0 else 1, seed=y * 7, noise=0.08)
    for y in range(y0, y1 + 1, 2):
        start = x0 + (y * 5) % max(1, x1 - x0)
        for x in range(start, min(x1 + 1, start + 3)):
            img.putpixel((x, y), pick(p.COPPER, 5))


def casing_face(centre: str) -> Image.Image:
    """Thick riveted frame around a recessed centre."""
    img = canvas()
    fill(img, 0, 0, 15, 15, p.DARK_IRON, 4, seed=1, noise=0.25)
    bevel(img, 0, 0, 15, 15, p.DARK_IRON, 5, 0)
    # Recessed centre: shadow on its top/left inner edge, light on its bottom/right.
    fill(img, 3, 3, 12, 12, p.DARK_IRON, 2, seed=2, noise=0.25)
    bevel(img, 2, 2, 13, 13, p.DARK_IRON, 0, 4)
    for x, y in ((1, 1), (14, 1), (1, 14), (14, 14), (8, 1), (1, 8), (14, 8), (8, 14)):
        img.putpixel((x, y), pick(p.HEAVY_IRON, 5))
        if x + 1 < 16 and y + 1 < 16:
            img.putpixel((x, y + 1) if y < 14 else (x - 1, y), pick(p.DARK_IRON, 0))
    if centre == "plate":
        for x in range(3, 13):
            img.putpixel((x, 8), pick(p.DARK_IRON, 0))
            img.putpixel((x, 9), pick(p.DARK_IRON, 3))
    elif centre == "window":
        _windings(img, 4, 4, 11, 11)
        bevel(img, 3, 3, 12, 12, p.DARK_IRON, 0, 3)
    elif centre == "vent":
        for y in range(4, 12, 2):
            for x in range(4, 12):
                img.putpixel((x, y), pick(p.DARK_IRON, 0))
                img.putpixel((x, y + 1), pick(p.DARK_IRON, 3))
    elif centre == "shaft":
        # Brass ring around the opening the half shaft comes out of.
        fill(img, 4, 4, 11, 11, p.BRASS, 3, seed=9, noise=0.2)
        bevel(img, 4, 4, 11, 11, p.BRASS, 5, 1)
        for x, y in ((4, 4), (11, 4), (4, 11), (11, 11)):
            img.putpixel((x, y), pick(p.DARK_IRON, 2))
        fill(img, 6, 6, 9, 9, p.DARK_IRON, 0, seed=3, noise=0)
    elif centre == "creative":
        fill(img, 3, 3, 12, 12, p.CREATIVE_INNER, 2, seed=4, noise=0.4)
        bevel(img, 2, 2, 13, 13, p.CREATIVE_FRAME, 3, 1)
        for y in range(4, 12, 3):
            for x in range(4 + (y % 2), 12, 3):
                img.putpixel((x, y), pick(p.CREATIVE_FRAME, 4))
    return img


def casing_faces() -> None:
    for centre in ("plate", "window", "vent", "shaft", "creative"):
        save(casing_face(centre), f"block/iron_casing_{centre}.png")


def copper_windings() -> None:
    img = canvas()
    _windings(img, 0, 0, 15, 15)
    save(img, "block/copper_windings.png")


GENERATORS = [material_tiles, casing_faces, copper_windings]
