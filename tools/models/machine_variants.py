"""Draws the alternator and electric motor variants side by side for the author to choose from.

Usage:
    python tools/models/machine_variants.py

Writes tools/textures/preview/machine_variants.png from the models described in generate.py.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parent))

from generate import MACHINE_VARIANTS, MACHINES, NS, machine_model  # noqa: E402
from render_preview import BACKGROUND, PREVIEW, Scene, texture  # noqa: E402

LABELS = {"A": "A. Boîtier Create", "B": "B. Bobinage apparent"}
NAMES = {"alternator": "Alternateur (cuivre)", "electric_motor": "Moteur électrique (andésite)"}

# Create's shaft, for showing where the spinning half shaft comes out (drawn by Create in game).
SHAFT = {"from": [6, 8, 6], "to": [10, 24, 10], "faces": {
    side: {"uv": [6, 0, 10, 16], "texture": "#axis"} for side in ("north", "south", "east", "west")} | {
    "up": {"uv": [6, 6, 10, 10], "texture": "#axis_top"}}}


def main() -> None:
    cell = 330
    sheet = Image.new("RGBA", (cell * 4, cell + 60), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype("arial.ttf", 16)
    except OSError:
        font = ImageFont.load_default()
    column = 0
    for variant in MACHINE_VARIANTS:
        for name, casing in MACHINES.items():
            model = machine_model(casing, variant)
            textures = {key: texture(ref if ":" in ref else f"{NS}:{ref}")
                        for key, ref in model["textures"].items()}
            textures["axis"] = texture("create:block/axis")
            textures["axis_top"] = texture("create:block/axis_top")
            scene = Scene((cell, cell), 170, (cell / 2, 90))
            scene.add_model([*model["elements"], SHAFT], textures, (-0.5, -0.6, -0.5))
            sheet.paste(scene.render(), (column * cell, 50))
            draw.text((column * cell + 14, 10), LABELS[variant], fill=(235, 235, 235, 255), font=font)
            draw.text((column * cell + 14, 30), NAMES[name], fill=(190, 190, 190, 255), font=font)
            column += 1
    out = PREVIEW / "machine_variants.png"
    sheet.save(out)
    print(f"wrote {out.relative_to(Path(__file__).resolve().parents[2])}")


if __name__ == "__main__":
    main()
