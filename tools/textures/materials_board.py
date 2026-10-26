"""Checks the base materials and draws their presentation board.

Usage:
    python tools/textures/materials_board.py

Checks every material PNG (16x16, RGBA, colours from the reference palette, transparency only where intended),
then writes tools/textures/preview/materiaux_base.png: each texture enlarged with nearest-neighbour, the same
texture on a block, and the palette.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "models"))

from dc.canvas import TEXTURES  # noqa: E402
from dc.materials import MATERIALS  # noqa: E402
from dc.palette import RAMPS, all_colors  # noqa: E402
from render_preview import Scene  # noqa: E402

BACKGROUND = (16, 30, 44, 255)
PANEL = (22, 40, 58, 255)
GOLD = (233, 188, 85, 255)
TEXT = (222, 232, 240, 255)
TRANSLUCENT = {"reinforced_glass"}


def check() -> list[str]:
    """Problems found in the material files; an empty list when all is well."""
    problems = []
    palette = all_colors()
    for name in MATERIALS:
        path = TEXTURES / "block" / "material" / f"{name}.png"
        img = Image.open(path)
        if img.size != (16, 16):
            problems.append(f"{name}: {img.size[0]}x{img.size[1]} instead of 16x16")
        if img.mode != "RGBA":
            problems.append(f"{name}: mode {img.mode} instead of RGBA")
        img = img.convert("RGBA")
        pixels = [img.getpixel((x, y)) for y in range(16) for x in range(16)]
        partial = [p for p in pixels if p[3] < 255]
        if partial and name not in TRANSLUCENT:
            problems.append(f"{name}: {len(partial)} transparent pixels in an opaque material")
        outside = {p[:3] for p in pixels if p[3] > 0 and p[:3] not in palette}
        if outside:
            problems.append(f"{name}: {len(outside)} colours outside the palette")
    return problems


def cube(texture: Image.Image, size: int) -> Image.Image:
    faces = {side: {"uv": [0, 0, 16, 16], "texture": "#t"} for side in ("up", "south", "east")}
    scene = Scene((size * 2, size * 2), size * 0.62, (size, size * 0.42))
    scene.image.paste(PANEL, (0, 0, size * 2, size * 2))
    scene.add_model([{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}], {"t": texture}, (-0.5, -0.5, -0.5))
    return scene.render().resize((size, size), Image.NEAREST)


def board() -> Path:
    cell, pad = 150, 14
    columns = len(MATERIALS)
    width = columns * cell + pad * 2
    height = 640
    sheet = Image.new("RGBA", (width, height), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    try:
        title = ImageFont.truetype("arialbd.ttf", 30)
        font = ImageFont.truetype("arial.ttf", 15)
        small = ImageFont.truetype("arial.ttf", 12)
    except OSError:
        title = font = small = ImageFont.load_default()

    draw.text((pad + 4, 12), "Create: Deep Current — Matériaux de base", fill=GOLD, font=title)
    draw.text((pad + 4, 52), "Textures 16×16 agrandies (×8, sans lissage) et appliquées sur un bloc", fill=TEXT, font=font)

    for i, (name, (label, _)) in enumerate(MATERIALS.items()):
        x = pad + i * cell
        texture = Image.open(TEXTURES / "block" / "material" / f"{name}.png").convert("RGBA")
        draw.rectangle((x + 4, 84, x + cell - 6, 410), fill=PANEL, outline=(52, 78, 98, 255))
        backdrop = Image.new("RGBA", (128, 128), (40, 44, 52, 255))
        backdrop.alpha_composite(texture.resize((128, 128), Image.NEAREST))
        sheet.paste(backdrop, (x + 11, 94))
        draw.text((x + 12, 228), label, fill=TEXT, font=font)
        sheet.alpha_composite(cube(texture, 128), (x + 11, 258))
        draw.text((x + 12, 392), f"material/{name}", fill=(140, 170, 190, 255), font=small)

    draw.text((pad + 4, 428), "Palette de référence (7 tons par matière, ombre → reflet)", fill=GOLD, font=font)
    y = 456
    per_row = 6
    for i, (label, ramp) in enumerate(RAMPS.items()):
        x = pad + 4 + (i % per_row) * 225
        row_y = y + (i // per_row) * 76
        draw.text((x, row_y), label, fill=TEXT, font=small)
        for j, color in enumerate(ramp):
            draw.rectangle((x + j * 28, row_y + 18, x + j * 28 + 25, row_y + 50), fill=color)

    out = HERE / "preview" / "materiaux_base.png"
    out.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(out)
    return out


def main() -> None:
    problems = check()
    for name in MATERIALS:
        img = Image.open(TEXTURES / "block" / "material" / f"{name}.png").convert("RGBA")
        colors = len({img.getpixel((x, y)) for y in range(16) for x in range(16)})
        print(f"  material/{name}.png  16x16  {colors} colours")
    print("checks:", "OK" if not problems else "")
    for problem in problems:
        print("  -", problem)
    print("board:", board().relative_to(HERE.parents[1]))


if __name__ == "__main__":
    main()
