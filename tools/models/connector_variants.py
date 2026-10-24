"""Draws the connector variants, the creative blocks on Create's casing, and the new coil and cable, in Create's
art direction, on one comparison sheet for the author to choose from.

Usage:
    python tools/models/connector_variants.py

Writes tools/textures/preview/connector_variants.png. Nothing is written into the mod's assets.
"""

from __future__ import annotations

import io
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "textures"))

import palette as p  # noqa: E402
from extract_create_palette import find_jar  # noqa: E402
from parts import casing_side, casing_top, cylinder_side, side_band, top_plate  # noqa: E402
from pixels import canvas, fill, pick  # noqa: E402
from render_preview import BACKGROUND, PREVIEW, Scene  # noqa: E402

SIDES = ("north", "south", "east", "west")


# --- Parts: one element, a top texture and a side texture -----------------------------------------------

def part(name, frm, to, top, side, textures, seed=0):
    """Paints {name}_top (w x d at the origin) and {name}_side (w x h) and returns the element."""
    w = min(16, round(to[0] - frm[0]))
    d = min(16, round(to[2] - frm[2]))
    h = min(16, round(to[1] - frm[1]))
    top_img, side_img = canvas(), canvas()
    top(top_img, w, d, seed)
    side(side_img, w, h, seed + 7)
    textures[f"{name}_top"] = top_img
    textures[f"{name}_side"] = side_img
    faces = {s: {"uv": [0, 0, w, h], "texture": f"#{name}_side"} for s in SIDES}
    faces["up"] = {"uv": [0, 0, w, d], "texture": f"#{name}_top"}
    faces["down"] = {"uv": [0, 0, w, d], "texture": f"#{name}_top"}
    return {"from": frm, "to": to, "faces": faces}


# --- The three connectors -------------------------------------------------------------------------------------

def variant_bracket():
    """1. Create bracket: andesite foot, dark rubber insulator with two sheds, copper lug."""
    t = {}
    elements = [
        part("foot", [4, 0, 4], [12, 2, 12], top_plate(p.ANDESITE, bolts=True), side_band(p.ANDESITE), t),
        part("body", [6.5, 2, 6.5], [9.5, 10, 9.5], top_plate(p.RUBBER), cylinder_side(p.RUBBER, mark_row=1), t),
        part("shed_a", [5, 3, 5], [11, 4, 11], top_plate(p.RUBBER), side_band(p.RUBBER), t, seed=3),
        part("shed_b", [5, 6, 5], [11, 7, 11], top_plate(p.RUBBER), side_band(p.RUBBER), t, seed=5),
        part("lug", [6.5, 10, 6.5], [9.5, 12, 9.5], top_plate(p.COPPER, hole=True), side_band(p.COPPER), t),
    ]
    return "1. Support Create", elements, t


def variant_marine_brass():
    """2. Marine brass: brass flange and collar, brown-glazed porcelain, copper cap with a touch of patina."""
    t = {}
    elements = [
        part("flange", [5, 0, 5], [11, 1, 11], top_plate(p.BRASS, bolts=True), side_band(p.BRASS), t),
        part("collar", [6, 1, 6], [10, 3, 10], top_plate(p.BRASS), side_band(p.BRASS), t, seed=2),
        part("body", [6.5, 3, 6.5], [9.5, 10, 9.5], top_plate(p.GLAZED_CERAMIC),
             cylinder_side(p.GLAZED_CERAMIC, mark_row=1, glaze=True), t),
        part("shed_a", [5, 4, 5], [11, 5, 11], top_plate(p.GLAZED_CERAMIC), side_band(p.GLAZED_CERAMIC), t, seed=3),
        part("shed_b", [5.5, 7, 5.5], [10.5, 8, 10.5], top_plate(p.GLAZED_CERAMIC),
             side_band(p.GLAZED_CERAMIC), t, seed=5),
        part("cap", [6, 10, 6], [10, 12, 10], top_plate(p.COPPER, hole=True), side_band(p.COPPER, patina=True), t),
    ]
    return "2. Laiton marin", elements, t


def variant_terminal_box():
    """3. Compact terminal: a small andesite casing, brass post with LV ring, copper nut."""
    t = {}
    elements = [
        part("box", [3, 0, 3], [13, 5, 13], casing_top, casing_side, t),
        part("post", [6, 5, 6], [10, 9, 10], top_plate(p.BRASS), side_band(p.BRASS, mark_row=1), t),
        part("nut", [6.5, 9, 6.5], [9.5, 11, 9.5], top_plate(p.COPPER, hole=True), side_band(p.COPPER), t),
    ]
    return "3. Borne compacte", elements, t


# --- Creative blocks on Create's casing -------------------------------------------------------------------

def creative_block(casing: Image.Image, belt_ramp):
    """Create's creative casing on every face, with a metal belt around the middle telling source from load."""
    t = {"casing": casing}
    faces = {s: {"uv": [0, 0, 16, 16], "texture": "#casing"} for s in (*SIDES, "up", "down")}
    cube = {"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}
    belt = part("belt", [-0.5, 7, -0.5], [16.5, 9, 16.5], top_plate(belt_ramp), side_band(belt_ramp), t)
    # Only the belt's sides show; its top and bottom are inside the cube.
    del belt["faces"]["up"], belt["faces"]["down"]
    return [cube, belt], t


def create_texture(path: str) -> Image.Image:
    with zipfile.ZipFile(find_jar()) as archive:
        data = archive.read(f"assets/create/textures/{path}.png")
    return Image.open(io.BytesIO(data)).convert("RGBA").crop((0, 0, 16, 16))


# --- Coil and cable -----------------------------------------------------------------------------------------------

def coil_item() -> Image.Image:
    """Wooden spool in Create's tones: dark wood flanges with andesite rims, rubber cable wound in turns."""
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
    return img


def cable_texture() -> Image.Image:
    """Cable sheath, u across: round rubber shading in Create's belt tones, thin LV stripe on each side."""
    img = canvas()
    across = [0, 1, 1, 2, 2, 3, 4, 5, 5, 4, 3, 2, 2, 1, 1, 0]
    for x, index in enumerate(across):
        for y in range(16):
            img.putpixel((x, y), pick(p.RUBBER, index))
    for y in range(16):
        img.putpixel((3, y), pick(p.MARK_LV, 1))
        img.putpixel((12, y), pick(p.MARK_LV, 1))
    return img


# --- Sheet -------------------------------------------------------------------------------------------------------------

def main() -> None:
    PREVIEW.mkdir(parents=True, exist_ok=True)
    casing = create_texture("block/creative_casing")
    source, source_tex = creative_block(casing, p.COPPER)
    load, load_tex = creative_block(casing, p.ANDESITE)

    column, height = 380, 760
    sheet = Image.new("RGBA", (column * 3, height), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    try:
        font = ImageFont.truetype("arial.ttf", 16)
    except OSError:
        font = ImageFont.load_default()
    for i, (label, elements, textures) in enumerate((variant_bracket(), variant_marine_brass(),
                                                       variant_terminal_box())):
        close_up = Scene((column, 340), 260, (column / 2, 40))
        close_up.add_model(elements, textures, (-0.5, -0.75, -0.5))
        sheet.paste(close_up.render(), (i * column, 30))

        in_situ = Scene((column, 330), 46, (column / 2 - 40, 110))
        in_situ.add_model(source, source_tex, (0, 0, 0))
        in_situ.add_model(elements, textures, (0, 1, 0))
        in_situ.add_model(load, load_tex, (4, 0, 1))
        in_situ.add_model(elements, textures, (4, 1, 1))
        anchor_height = max(e["to"][1] for e in elements) / 16 - 1 / 16
        in_situ.add_cable((0.5, 1 + anchor_height, 0.5), (4.5, 1 + anchor_height, 1.5))
        sheet.paste(in_situ.render(), (i * column, 380))
        draw.text((i * column + 16, 10), label, fill=(235, 235, 235, 255), font=font)

    draw.text((16, height - 50), "Bobine BT et texture du câble :", fill=(235, 235, 235, 255), font=font)
    sheet.alpha_composite(coil_item().resize((64, 64), Image.NEAREST), (250, height - 70))
    sheet.alpha_composite(cable_texture().resize((64, 64), Image.NEAREST), (330, height - 70))
    draw.text((420, height - 50), "Blocs créatifs : boîtier créatif de Create, ceinture cuivre = source, "
              "andésite = charge", fill=(235, 235, 235, 255), font=font)

    out = PREVIEW / "connector_variants.png"
    sheet.save(out)
    print(f"wrote {out.relative_to(Path(__file__).resolve().parents[2])}")


if __name__ == "__main__":
    main()
