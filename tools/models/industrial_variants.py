"""Industrial redo (dark riveted iron, copper, brass): connector, alternator, motor and creative blocks in two
variants each, on one sheet for the author to choose from.

Usage:
    python tools/models/industrial_variants.py

Writes the industrial textures into the assets (they are used whichever variant wins) and
tools/textures/preview/industrial_variants.png.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

# models/ must come first: both folders have a generate.py.
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "textures"))
sys.path.insert(0, str(Path(__file__).resolve().parent))

import industrial_textures  # noqa: E402
import palette as p  # noqa: E402
from generate import NS, SIDES_ONLY, box  # noqa: E402
from pixels import canvas, fill, pick  # noqa: E402
from render_preview import BACKGROUND, PREVIEW, Scene, texture  # noqa: E402

TEXTURES = {
    "riveted": "block/dark_iron_riveted_tile", "dark": "block/dark_iron_tile", "heavy": "block/heavy_iron_tile",
    "copper": "block/copper_tile", "brass": "block/brass_tile", "lv": "block/mark_lv_tile",
    "windings": "block/copper_windings", "plate": "block/iron_casing_plate", "window": "block/iron_casing_window",
    "vent": "block/iron_casing_vent", "shaft": "block/iron_casing_shaft", "creative": "block/iron_casing_creative",
}


def cube(faces: dict[str, str]) -> dict:
    """Full block with one texture per face (up = shaft side)."""
    return {"from": [0, 0, 0], "to": [16, 16, 16],
            "faces": {side: {"uv": [0, 0, 16, 16], "texture": f"#{tex}"} for side, tex in faces.items()}}


def corner_posts(texture: str) -> list[dict]:
    return [box([x, 0, z], [x + 3, 16, z + 3], texture) for x in (-0.5, 13.5) for z in (-0.5, 13.5)]


CONNECTORS = {
    "1. Pagode (C&A + cuivre)": [
        box([3, 0, 3], [13, 2, 13], "riveted"),
        box([6.5, 2, 6.5], [9.5, 9, 9.5], "heavy"),
        box([4, 3, 4], [12, 4, 12], "dark"),
        box([4.5, 5, 4.5], [11.5, 6, 11.5], "dark"),
        box([5, 7, 5], [11, 8, 11], "dark"),
        box([5.5, 9, 5.5], [10.5, 10, 10.5], "lv"),
        box([6, 10, 6], [10, 12, 10], "copper"),
    ],
    "2. Bulbe cuivre (New Age)": [
        box([4, 0, 4], [12, 2, 12], "riveted"),
        box([5, 2, 5], [11, 3, 11], "heavy"),
        box([5.5, 3, 5.5], [10.5, 8, 10.5], "copper"),
        box([5.25, 5, 5.25], [10.75, 6, 10.75], "lv"),
        box([5, 8, 5], [11, 9, 11], "dark"),
        box([7, 9, 7], [9, 12, 9], "heavy"),
        box([5.5, 10, 5.5], [10.5, 11, 10.5], "dark"),
        box([6.5, 12, 6.5], [9.5, 13, 9.5], "copper"),
    ],
}

SIDE4 = ("north", "south", "east", "west")


def drum(core: str, band: str | None) -> list[dict]:
    """Variant B: plinth, a drum between two plates, brass straps."""
    elements = [
        box([0, 0, 0], [16, 3, 16], "plate"),
        box([0, 13, 0], [16, 16, 16], "plate", SIDE4 + ("down",)),
        {**box([0, 13, 0], [16, 16, 16], "shaft", ("up",)), "faces": {
            "up": {"uv": [0, 0, 16, 16], "texture": "#shaft"}}},
        box([2, 3, 2], [14, 13, 14], core, SIDES_ONLY),
        box([1.5, 5, 1.5], [14.5, 6, 14.5], "brass", SIDES_ONLY),
        box([1.5, 10, 1.5], [14.5, 11, 14.5], "brass", SIDES_ONLY),
    ]
    if band:
        elements.append(box([1.75, 7, 1.75], [14.25, 9, 14.25], band, SIDES_ONLY))
    return elements


MACHINES = {
    ("A. Caisson riveté", "Alternateur"): [
        cube({"up": "shaft", "down": "plate", "north": "window", "south": "window", "east": "vent",
              "west": "vent"}),
        *corner_posts("brass"),
    ],
    ("A. Caisson riveté", "Moteur électrique"): [
        cube({"up": "shaft", "down": "plate", "north": "plate", "south": "plate", "east": "plate",
              "west": "plate"}),
        box([-0.5, 5, -0.5], [16.5, 11, 16.5], "windings", SIDES_ONLY),
        *corner_posts("heavy"),
    ],
    ("B. Tambour", "Alternateur"): drum("windings", None),
    ("B. Tambour", "Moteur électrique"): drum("dark", "windings"),
}

CREATIVE = {
    "Source créative": [cube({s: "creative" for s in (*SIDE4, "up", "down")}),
                        box([-0.5, 7, -0.5], [16.5, 9, 16.5], "copper", SIDES_ONLY)],
    "Charge créative": [cube({s: "creative" for s in (*SIDE4, "up", "down")}),
                        box([-0.5, 7, -0.5], [16.5, 9, 16.5], "heavy", SIDES_ONLY)],
}

SHAFT = {"from": [6, 8, 6], "to": [10, 24, 10], "faces": {
    side: {"uv": [6, 0, 10, 16], "texture": "#axis"} for side in SIDE4} | {
    "up": {"uv": [6, 6, 10, 10], "texture": "#axis_top"}}}


def coil_item() -> Image.Image:
    """Spool with dark iron flanges and brass rims, rubber cable wound in turns, LV marks."""
    img = canvas()
    for y in range(4, 12):
        fill(img, 4, y, 11, y, p.RUBBER, 3 if y % 2 == 0 else 1, seed=y, noise=0.25)
    for x in (6, 9):
        img.putpixel((x, 7), pick(p.MARK_LV, 2))
    for x0 in (2, 12):
        fill(img, x0, 2, x0 + 1, 13, p.DARK_IRON, 3, seed=x0)
        for y in range(2, 14):
            img.putpixel((x0, y), pick(p.DARK_IRON, 5))
        for y in (2, 13):
            img.putpixel((x0, y), pick(p.BRASS, 5))
            img.putpixel((x0 + 1, y), pick(p.BRASS, 2))
        img.putpixel((x0 + 1, 7), pick(p.BRASS, 3))
    for x, y in ((12, 12), (13, 13), (14, 13)):
        img.putpixel((x, y), pick(p.RUBBER, 2))
    img.putpixel((15, 13), pick(p.COPPER, 4))
    return img


def textures_for(elements: list[dict]) -> dict:
    textures = {key: texture(f"{NS}:{path}") for key, path in TEXTURES.items()}
    textures["axis"] = texture("create:block/axis")
    textures["axis_top"] = texture("create:block/axis_top")
    return textures


def render(elements: list[dict], size: int, scale: float = 0, at=(-0.5, -0.6, -0.5)) -> Image.Image:
    """Renders large, crops to the drawn pixels and fits the cell, whatever the model's size."""
    scene = Scene((600, 600), 220, (300, 220))
    scene.add_model(elements, textures_for(elements), at)
    img = scene.render()
    background = Image.new("RGB", img.size, BACKGROUND[:3])
    bbox = ImageChops.difference(img.convert("RGB"), background).getbbox()
    img = img.crop(bbox)
    ratio = min((size - 16) / img.width, (size - 16) / img.height)
    img = img.resize((int(img.width * ratio), int(img.height * ratio)), Image.NEAREST)
    cell = Image.new("RGBA", (size, size), BACKGROUND)
    cell.paste(img, ((size - img.width) // 2, (size - img.height) // 2))
    return cell


def main() -> None:
    for generator in industrial_textures.GENERATORS:
        generator()
    texture.cache_clear()

    cell = 260
    sheet = Image.new("RGBA", (cell * 4, cell * 2 + 220), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype("arial.ttf", 15)
    except OSError:
        font = ImageFont.load_default()

    def label(text, x, y):
        draw.text((x, y), text, fill=(235, 235, 235, 255), font=font)

    # Row 1: connectors (close-up) and creative blocks.
    for i, (name, elements) in enumerate(CONNECTORS.items()):
        sheet.paste(render(elements, cell, 300, (-0.5, -0.75, -0.5)), (i * cell, 30))
        label("Connecteur " + name, i * cell + 10, 8)
    for i, (name, elements) in enumerate(CREATIVE.items()):
        sheet.paste(render(elements, cell, 120), ((i + 2) * cell, 30))
        label(name, (i + 2) * cell + 10, 8)

    # Rows 2-3: machines, with Create's shaft where the half shaft spins.
    for i, ((variant, machine), elements) in enumerate(MACHINES.items()):
        x = (i % 2) * cell + (i // 2) * cell * 2
        y = cell + 70
        sheet.paste(render([*elements, SHAFT], cell, 130), (x, y))
        label(f"{variant} : {machine}", x + 10, y - 22)

    label("Bobine BT", 14, cell * 2 + 150)
    sheet.alpha_composite(coil_item().resize((96, 96), Image.NEAREST), (120, cell * 2 + 110))
    out = PREVIEW / "industrial_variants.png"
    sheet.save(out)
    print(f"wrote {out.relative_to(Path(__file__).resolve().parents[2])}")


if __name__ == "__main__":
    main()
