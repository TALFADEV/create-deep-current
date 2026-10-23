"""Regenerates every Deep Current texture.

Usage:
    python tools/textures/generate.py

Textures are written into the mod's assets; review sheets go to
tools/textures/preview/ (not shipped in the jar). Generators are added with the
milestones that need them and registered in GENERATORS.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw

import palette

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "deepcurrent" / "textures"
PREVIEW = Path(__file__).resolve().parent / "preview"


def palette_sheet() -> None:
    """Swatch sheet of every ramp, for reviewing the palette."""
    swatch, label_w, pad = 32, 140, 8
    longest = max(len(r) for r in palette.RAMPS.values())
    width = label_w + longest * swatch + pad * 2
    height = len(palette.RAMPS) * (swatch + 4) + pad * 2
    sheet = Image.new("RGBA", (width, height), (24, 24, 28, 255))
    draw = ImageDraw.Draw(sheet)
    for row, (name, colours) in enumerate(palette.RAMPS.items()):
        y = pad + row * (swatch + 4)
        draw.text((pad, y + swatch // 2 - 6), name, fill=(230, 230, 230, 255))
        for col, colour in enumerate(colours):
            x = label_w + col * swatch
            draw.rectangle((x, y, x + swatch - 2, y + swatch - 1), fill=colour)
    PREVIEW.mkdir(parents=True, exist_ok=True)
    sheet.save(PREVIEW / "palette.png")


GENERATORS = [palette_sheet]


def main() -> None:
    for generator in GENERATORS:
        generator()
        print(f"ok  {generator.__name__}")


if __name__ == "__main__":
    main()
