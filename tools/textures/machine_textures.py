"""Textures shared by the electric machines: copper windings and plain material tiles.

The machines' casings come from Create itself (copper and andesite casings); these textures only add the parts
Create has no texture for.
"""

from __future__ import annotations

import palette as p
from pixels import canvas, fill, pick, save


def copper_windings() -> None:
    """Rows of copper wire, 2 px per turn: lit top, shaded bottom, a few strands catching the light."""
    img = canvas()
    # Light noise only: strong speckles break the strands into bricks.
    for y in range(16):
        base = 4 if y % 2 == 0 else 1
        fill(img, 0, y, 15, y, p.COPPER, base, seed=y * 7, noise=0.08)
    for y in range(0, 16, 2):
        start = (y * 5) % 16
        for x in range(start, start + 4):
            img.putpixel((x % 16, y), pick(p.COPPER, 5))
    save(img, "block/copper_windings.png")


def material_tiles() -> None:
    """Plain speckled tiles for small parts (collars, caps), usable at any UV."""
    for name, ramp in (("brass", p.BRASS), ("andesite", p.ANDESITE)):
        img = canvas()
        fill(img, 0, 0, 15, 15, ramp, 3, seed=len(name), noise=0.3)
        save(img, f"block/{name}_tile.png")


GENERATORS = [copper_windings, material_tiles]
