"""Preview of the machines drawn so far: their atlas enlarged, and the machine at rest and running, seen from
two opposite corners.

Usage:
    python tools/models/machine_preview.py

Writes tools/textures/preview/<machine>.png.
"""

from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "textures"))
sys.path.insert(0, str(HERE))

import alternator_art  # noqa: E402
import battery_art  # noqa: E402
import creative_art  # noqa: E402
import motor_art  # noqa: E402
import network_art  # noqa: E402
import turbine_art  # noqa: E402
import wear_art  # noqa: E402
from dc.modelling import overlay  # noqa: E402
from render_preview import Scene, texture  # noqa: E402

BACKGROUND = (16, 30, 44, 255)
PANEL = (22, 40, 58, 255)
GOLD = (233, 188, 85, 255)
TEXT = (222, 232, 240, 255)
OPPOSITE = {"north": "south", "south": "north", "east": "west", "west": "east", "up": "up", "down": "down"}


def turned(elements: list[dict]) -> list[dict]:
    """The model turned 180 degrees around Y, to see its other side with a renderer that shows up/south/east."""
    out = []
    for e in elements:
        (x0, y0, z0), (x1, y1, z1) = e["from"], e["to"]
        turned_e = {"name": e.get("name"), "from": [16 - x1, y0, 16 - z1], "to": [16 - x0, y1, 16 - z0],
                    "faces": {OPPOSITE[side]: face for side, face in e["faces"].items()}}
        if "rotation" in e:
            r = e["rotation"]
            ox, oy, oz = r["origin"]
            # A half turn around Y reverses the X and Z axes, so rotations about them change sign.
            turned_e["rotation"] = {"axis": r["axis"], "origin": [16 - ox, oy, 16 - oz],
                                    "angle": r["angle"] if r["axis"] == "y" else -r["angle"]}
        out.append(turned_e)
    return out


def shifted(elements: list[dict], dz: float) -> list[dict]:
    """The model moved along Z (a second machine on the same shaft line)."""
    return [{**e, "from": [e["from"][0], e["from"][1], e["from"][2] + dz],
             "to": [e["to"][0], e["to"][1], e["to"][2] + dz]} for e in elements]


def render(elements: list[dict], textures: dict, size: int) -> Image.Image:
    scene = Scene((640, 640), 240, (320, 240))
    scene.image.paste(PANEL, (0, 0, 640, 640))
    scene.add_model(elements, textures, (-0.5, -0.5, -0.5))
    img = scene.render()
    bbox = ImageChops.difference(img.convert("RGB"), Image.new("RGB", img.size, PANEL[:3])).getbbox()
    img = img.crop(bbox)
    ratio = min((size - 20) / img.width, (size - 20) / img.height)
    img = img.resize((int(img.width * ratio), int(img.height * ratio)), Image.NEAREST)
    cell = Image.new("RGBA", (size, size), PANEL)
    cell.paste(img, ((size - img.width) // 2, (size - img.height) // 2))
    return cell


def sheet(title: str, atlas: Image.Image, panels: list[tuple[str, list[dict]]], textures: dict,
          name: str) -> None:
    """One machine: its atlas enlarged, then the model under each listed view."""
    cell = 300
    out_img = Image.new("RGBA", (cell * (len(panels) + 1) + 10 * (len(panels) + 2), cell + 110), BACKGROUND)
    draw = ImageDraw.Draw(out_img)
    draw.text((16, 12), title, fill=GOLD, font=ImageFont.truetype("arialbd.ttf", 26))
    font = ImageFont.truetype("arial.ttf", 15)
    backdrop = Image.new("RGBA", (256, 256), (40, 44, 52, 255))
    backdrop.alpha_composite(atlas.resize((256, 256), Image.NEAREST))
    images = [("Atlas 32×32 (×8)", backdrop.resize((cell - 20, cell - 20), Image.NEAREST))]
    images += [(label, render(elements, textures, cell)) for label, elements in panels]
    for i, (label, img) in enumerate(images):
        x = 10 + i * (cell + 10)
        out_img.paste(PANEL, (x, 56, x + cell, 56 + cell))
        out_img.alpha_composite(img, (x + (cell - img.width) // 2, 56 + (cell - img.height) // 2))
        draw.text((x + 8, cell + 64), label, fill=TEXT, font=font)
    out = HERE.parent / "textures" / "preview" / f"{name}.png"
    out_img.save(out)
    print("wrote", out.relative_to(HERE.parents[1]))


def main() -> None:
    shaft_textures = {"axis": texture("create:block/axis"), "axis_top": texture("create:block/axis_top")}

    atlas = alternator_art.paint().img
    body = alternator_art.ALTERNATOR_ELEMENTS + alternator_art.ROTOR_ELEMENTS
    idle = body + alternator_art.LAMP_OFF_ELEMENTS
    sheet("Alternateur — Alpha", atlas, [
        ("Au repos, côté hublot", idle),
        ("En production (voyant allumé)", body + alternator_art.GLOW_ELEMENTS),
        ("Deux à la suite sur le même arbre", idle + shifted(idle, 16)),
    ], {"atlas": atlas, **shaft_textures}, "alternator")

    atlas = motor_art.paint().img
    motor = motor_art.MOTOR_ELEMENTS + motor_art.ITEM_SHAFT
    sheet("Moteur électrique — Alpha", atlas, [
        ("Sortie d'axe en haut", motor),
        ("Côté boîte à bornes", turned(motor)),
        ("Sans l'arbre (collier de 6 px)", motor_art.MOTOR_ELEMENTS),
    ], {"atlas": atlas, **shaft_textures}, "electric_motor")

    network_sheet()

    atlas = battery_art.paint().img
    sheet("Petite batterie — Alpha", atlas,
          [(f"Charge {level}/4", battery_art.BATTERY_ELEMENTS + battery_art.lit_cells(level)) for level in (0, 2, 4)]
          + [("Côté ouïes, chargée", turned(battery_art.BATTERY_ELEMENTS + battery_art.lit_cells(4)))],
          {"atlas": atlas}, "small_battery")

    atlas = turbine_art.paint().img
    turbine = turbine_art.TURBINE_ELEMENTS + turbine_art.ROTOR_ELEMENTS
    sheet("Petite hydrolienne — Alpha", atlas, [
        ("Face au courant (rotor)", turned(turbine)),
        ("Arrière : sortie d'axe", turbine + turbine_art.ITEM_SHAFT),
        ("Nacelle sans le rotor", turned(turbine_art.TURBINE_ELEMENTS)),
    ], {"atlas": atlas, **shaft_textures}, "small_tidal_turbine")

    wear_sheet()

    for name, source, elements in (("creative_power_source", True, creative_art.SOURCE_ELEMENTS),
                                   ("creative_power_consumer", False, creative_art.LOAD_ELEMENTS)):
        atlas = creative_art.paint(source).img
        sheet(f"{'Source' if source else 'Charge'} électrique créative — Alpha", atlas, [
            ("Vue de trois quarts", elements),
            ("Autre côté", turned(elements)),
            ("Sous un connecteur BT", elements + retextured(lifted(network_art.CONNECTOR_ELEMENTS, 0, 16, 0), "connector")),
        ], {"atlas": atlas, "connector": network_art.connector_atlas().img}, name)


def wear_sheet() -> None:
    """The three damage stages: each overlay enlarged over steel and over yellow paint, then laid over the
    hydrolienne, the alternator and the battery."""
    machines = [
        ("Hydrolienne", "small_tidal_turbine", turbine_art.paint().img, turned(turbine_art.TURBINE_ELEMENTS + turbine_art.ROTOR_ELEMENTS)),
        ("Alternateur", "alternator", alternator_art.paint().img,
         alternator_art.ALTERNATOR_ELEMENTS + alternator_art.ROTOR_ELEMENTS + alternator_art.LAMP_OFF_ELEMENTS),
        ("Batterie", "small_battery", battery_art.paint().img, battery_art.BATTERY_ELEMENTS + battery_art.lit_cells(2)),
    ]
    stages = [wear_art.paint(stage).img for stage in (1, 2, 3)]
    shaft = {"axis": texture("create:block/axis"), "axis_top": texture("create:block/axis_top")}
    cell, gap = 260, 10
    font = ImageFont.truetype("arial.ttf", 15)
    out_img = Image.new("RGBA", (gap + 4 * (cell + gap), 56 + (len(machines) + 1) * (cell + 34)), BACKGROUND)
    draw = ImageDraw.Draw(out_img)
    draw.text((16, 12), "Usure palier 1 — intégrité 75 / 50 / 25 %", fill=GOLD,
              font=ImageFont.truetype("arialbd.ttf", 26))

    # Header row: each overlay (x8) over bare steel on the left half and yellow paint on the right half.
    draw.text((gap + 8, 56 + cell // 2), "Superpositions 16×16 (×8)", fill=TEXT, font=font)
    for col, img in enumerate(stages, start=1):
        x = gap + col * (cell + gap)
        tile = Image.new("RGBA", (16, 16), (88, 92, 101, 255))
        tile.paste((237, 190, 46, 255), (8, 0, 16, 16))
        tile.alpha_composite(img)
        out_img.paste(tile.resize((cell - 4, cell - 4), Image.NEAREST), (x + 2, 58))
        draw.text((x + 8, 56 + cell + 8), f"overlay/cracks_{col}", fill=TEXT, font=font)

    for row, (label, machine, atlas, elements) in enumerate(machines, start=1):
        y = 56 + row * (cell + 34)
        for col in range(4):
            x = gap + col * (cell + gap)
            textures = {"atlas": atlas, **shaft}
            parts = elements
            if col:
                textures["overlay"] = stages[col - 1]
                parts = elements + overlay(elements, parts=wear_art.SHELLS[machine])
            out_img.alpha_composite(render(parts, textures, cell), (x, y))
            caption = "intacte" if col == 0 else f"sous {(75, 50, 25)[col - 1]} %"
            draw.text((x + 8, y + cell + 8), f"{label} — {caption}", fill=TEXT, font=font)
    out = HERE.parent / "textures" / "preview" / "wear_tier1.png"
    out_img.save(out)
    print("wrote", out.relative_to(HERE.parents[1]))


def retextured(elements: list[dict], key: str) -> list[dict]:
    """The same elements reading another texture (two models with their own "#atlas" in one view)."""
    return [{**e, "faces": {side: {**f, "texture": f"#{key}"} for side, f in e["faces"].items()}} for e in elements]


def lifted(elements: list[dict], dx: float, dy: float, dz: float) -> list[dict]:
    """The model moved by whole pixels (to set a connector on top of a machine)."""
    return [{**e, "from": [e["from"][0] + dx, e["from"][1] + dy, e["from"][2] + dz],
             "to": [e["to"][0] + dx, e["to"][1] + dy, e["to"][2] + dz]} for e in elements]


def network_sheet() -> None:
    images = network_art.preview_images()
    connector = network_art.CONNECTOR_ELEMENTS
    textures = {"atlas": images["connector"]}

    # Two alternators 3 blocks apart, a connector on each terminal box, a cable between them.
    alt_atlas = alternator_art.paint().img
    alternator = alternator_art.ALTERNATOR_ELEMENTS + alternator_art.ROTOR_ELEMENTS + alternator_art.LAMP_OFF_ELEMENTS
    scene = Scene((900, 700), 90, (450, 260))
    scene.image.paste(PANEL, (0, 0, 900, 700))
    shaft = {"axis": texture("create:block/axis"), "axis_top": texture("create:block/axis_top")}
    for bx in (0, 3):
        scene.add_model(alternator, {"atlas": alt_atlas, **shaft}, (bx, 0, 0))
        scene.add_model(connector, textures, (bx, 1, 0))
    scene.add_cable((0.5, 1 + 11 / 16, 0.5), (3.5, 1 + 11 / 16, 0.5))
    network = scene.render()
    bbox = ImageChops.difference(network.convert("RGB"), Image.new("RGB", network.size, PANEL[:3])).getbbox()
    network = network.crop(bbox)

    cell = 300
    out_img = Image.new("RGBA", (cell * 4 + 50, cell + 110), BACKGROUND)
    draw = ImageDraw.Draw(out_img)
    draw.text((16, 12), "Réseau BT — Alpha", fill=GOLD, font=ImageFont.truetype("arialbd.ttf", 26))
    font = ImageFont.truetype("arial.ttf", 15)
    cable = Image.new("RGBA", (128, 128), (40, 44, 52, 255))
    cable.alpha_composite(images["cable"].resize((128, 128), Image.NEAREST))
    coil = Image.new("RGBA", (128, 128), (40, 44, 52, 255))
    coil.alpha_composite(images["coil"].resize((128, 128), Image.NEAREST))
    ratio = min((cell - 20) / network.width, (cell - 20) / network.height)
    panels = [
        ("Connecteur BT", render(connector, textures, cell)),
        ("Sur deux alternateurs, câblés", network.resize((int(network.width * ratio),
                                                           int(network.height * ratio)), Image.NEAREST)),
        ("Câble BT (texture ×8)", cable),
        ("Bobine BT (icône ×8)", coil),
    ]
    for i, (label, img) in enumerate(panels):
        x = 10 + i * (cell + 10)
        out_img.paste(PANEL, (x, 56, x + cell, 56 + cell))
        out_img.alpha_composite(img, (x + (cell - img.width) // 2, 56 + (cell - img.height) // 2))
        draw.text((x + 8, cell + 64), label, fill=TEXT, font=font)
    out = HERE.parent / "textures" / "preview" / "lv_network.png"
    out_img.save(out)
    print("wrote", out.relative_to(HERE.parents[1]))


if __name__ == "__main__":
    main()
