"""Writes the block/item models, blockstates and loot tables, plus a Blockbench source for complex models.

Usage:
    python tools/models/generate.py

Complex models are described once here (elements in pixels, facing up) and written both as a Minecraft model and
as a Blockbench .bbmodel in art/, so the author can open and retouch them.
"""

from __future__ import annotations

import base64
import json
import sys
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "textures"))

from alternator_art import ALTERNATOR_ELEMENTS, GLOW_ELEMENTS, LAMP_OFF_ELEMENTS, ROTOR_ELEMENTS  # noqa: E402
from network_art import CONNECTOR_ELEMENTS  # noqa: E402
from battery_art import BATTERY_ELEMENTS, lit_cells  # noqa: E402
from motor_art import ITEM_SHAFT, MOTOR_ELEMENTS  # noqa: E402
import turbine_art  # noqa: E402
import creative_art  # noqa: E402

RESOURCES = ROOT / "src" / "main" / "resources"
ASSETS = RESOURCES / "assets" / "deepcurrent"
DATA = RESOURCES / "data" / "deepcurrent"
ART = ROOT / "art"
NS = "deepcurrent"
SIDES = ("north", "south", "east", "west")


def write_json(path: Path, data: dict) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2) + "\n", encoding="utf-8")


# --- Model description --------------------------------------------------------------------------

def element(name, frm, to, texture, side_uv, top_uv, bottom_uv=None):
    faces = {side: {"uv": side_uv, "texture": f"#{texture}"} for side in SIDES}
    faces["up"] = {"uv": top_uv, "texture": f"#{texture}"}
    faces["down"] = {"uv": bottom_uv or top_uv, "texture": f"#{texture}"}
    return {"name": name, "from": frm, "to": to, "faces": faces}


LV_CONNECTOR = {
    "name": "lv_connector",
    "textures": {
        "atlas": f"{NS}:block/lv_connector",
        "particle": f"{NS}:block/lv_connector",
    },
    # Drawn in network_art.py: wall plate, yellow LV collar, copper core in three insulator discs, brass clamp.
    "elements": CONNECTOR_ELEMENTS,
}


# --- Electric machines (alternator, motor) ------------------------------------------------------------------

def box(frm, to, texture, faces=("north", "south", "east", "west", "up", "down")):
    """An element with UVs taken from its own coordinates, like Blockbench's auto UV (clamped to the texture)."""
    c = lambda v: max(0.0, min(16.0, v))  # noqa: E731
    x0, y0, z0 = (c(v) for v in frm)
    x1, y1, z1 = (c(v) for v in to)
    uv = {
        "up": [x0, z0, x1, z1], "down": [x0, z0, x1, z1],
        "north": [x0, 16 - y1, x1, 16 - y0], "south": [x0, 16 - y1, x1, 16 - y0],
        "east": [z0, 16 - y1, z1, 16 - y0], "west": [z0, 16 - y1, z1, 16 - y0],
    }
    return {"from": frm, "to": to, "faces": {f: {"uv": uv[f], "texture": f"#{texture}"} for f in faces}}


def shaft_collar(y0, y1, texture):
    """Four blocks around the 4x4 opening the spinning half shaft comes out of."""
    return [box([3, y0, 3], [13, y1, 6], texture), box([3, y0, 10], [13, y1, 13], texture),
            box([3, y0, 6], [6, y1, 10], texture), box([10, y0, 6], [13, y1, 10], texture)]


SIDES_ONLY = ("north", "south", "east", "west")

# Both variants face up (shaft on top); blockstates turn them like the connector.
MACHINE_VARIANTS = {
    # A. Create casing: a full casing block, a band of copper windings, a collar around the shaft.
    "A": lambda: [box([0, 0, 0], [16, 13, 16], "casing"),
                  box([-0.5, 4, -0.5], [16.5, 8, 16.5], "windings", SIDES_ONLY),
                  *shaft_collar(13, 16, "collar")],
    # B. Open windings: casing base and cap, the copper stator showing in between.
    "B": lambda: [box([0, 0, 0], [16, 2, 16], "casing"),
                  box([2, 2, 2], [14, 12, 14], "windings", SIDES_ONLY),
                  box([1, 12, 1], [15, 14, 15], "casing"),
                  *shaft_collar(14, 16, "collar")],
}
MACHINE_VARIANT = "A"
MACHINES = {
    "alternator": "create:block/copper_casing",
    "electric_motor": "create:block/andesite_casing",
}


def machine_model(casing: str, variant: str = MACHINE_VARIANT) -> dict:
    return {
        "parent": "block/block",
        "textures": {
            "casing": casing,
            "windings": f"{NS}:block/copper_windings",
            "collar": f"{NS}:block/brass_tile",
            "particle": casing,
        },
        "elements": MACHINE_VARIANTS[variant](),
    }


# --- Alternator (drawn by hand in alternator_art.py) ----------------------------------------------------------

ALTERNATOR_TEXTURES = {
    "atlas": f"{NS}:block/alternator",
    "axis": "create:block/axis",
    "axis_top": "create:block/axis_top",
    "particle": f"{NS}:block/alternator",
}


def write_alternator() -> None:
    """Lying machine along Z with a through shaft: static body, a lit variant, and the rotor that spins."""
    # Translucent: the portholes are reinforced glass, and the rotor must show through them.
    model = lambda elements: {"parent": "block/block", "render_type": "minecraft:translucent",  # noqa: E731
                              "textures": ALTERNATOR_TEXTURES, "elements": elements}
    variants = {}
    for axis, rotation in (("z", {}), ("x", {"y": 90})):
        for active, name in (("false", "alternator"), ("true", "alternator_active")):
            # Create uses the vanilla horizontal axis property, named "axis".
            variants[f"active={active},axis={axis}"] = {"model": f"{NS}:block/{name}", **rotation}
    write_block("alternator", {"variants": variants}, model(ALTERNATOR_ELEMENTS + LAMP_OFF_ELEMENTS))
    write_json(ASSETS / "models" / "block" / "alternator_active.json", model(ALTERNATOR_ELEMENTS + GLOW_ELEMENTS))
    write_json(ASSETS / "models" / "block" / "alternator" / "rotor.json", model(ROTOR_ELEMENTS))
    # In inventories nothing spins: the item shows the body and the rotor together.
    write_json(ASSETS / "models" / "item" / "alternator.json",
               model(ALTERNATOR_ELEMENTS + LAMP_OFF_ELEMENTS + ROTOR_ELEMENTS))
    # Blockbench source of the body, for retouching by hand.
    write_json(ART / "alternator.bbmodel", blockbench_model({
        "name": "alternator", "textures": ALTERNATOR_TEXTURES,
        "elements": ALTERNATOR_ELEMENTS + LAMP_OFF_ELEMENTS + ROTOR_ELEMENTS[:1]}))


def write_electric_motor() -> None:
    """Designed facing up (shaft on top); Create spins its half shaft through the collar."""
    textures = {"atlas": f"{NS}:block/electric_motor", "axis": "create:block/axis",
                "axis_top": "create:block/axis_top", "particle": f"{NS}:block/electric_motor"}
    model = lambda elements: {"parent": "block/block", "textures": textures, "elements": elements}  # noqa: E731
    write_block("electric_motor", facing_blockstate(f"{NS}:block/electric_motor"), model(MOTOR_ELEMENTS))
    write_json(ASSETS / "models" / "item" / "electric_motor.json", model(MOTOR_ELEMENTS + ITEM_SHAFT))
    write_json(ART / "electric_motor.bbmodel", blockbench_model({
        "name": "electric_motor", "textures": textures, "elements": MOTOR_ELEMENTS}))


def write_small_battery() -> None:
    """One model per charge level (0 to 4 lit gauge cells), selected by the block's "charge" property.
    The block itself comes with milestone 4."""
    textures = {"atlas": f"{NS}:block/small_battery", "particle": f"{NS}:block/small_battery"}
    model = lambda elements: {"parent": "block/block", "textures": textures, "elements": elements}  # noqa: E731
    variants = {}
    for level in range(5):
        write_json(ASSETS / "models" / "block" / "small_battery" / f"charge_{level}.json",
                   model(BATTERY_ELEMENTS + lit_cells(level)))
        variants[f"charge={level}"] = {"model": f"{NS}:block/small_battery/charge_{level}"}
    write_json(ASSETS / "blockstates" / "small_battery.json", {"variants": variants})
    write_json(ASSETS / "models" / "item" / "small_battery.json", {"parent": f"{NS}:block/small_battery/charge_2"})
    # The dropped item keeps its charge (StoredKj, see BatteryCharge): copy_nbt into the stack tag on 1.20,
    # copy_custom_data into the custom data component on 1.21.
    ops = [{"source": "StoredKj", "target": "StoredKj", "op": "replace"}]
    for folder, function in (("loot_tables", "minecraft:copy_nbt"), ("loot_table", "minecraft:copy_custom_data")):
        table = loot_table("small_battery")
        table["pools"][0]["entries"][0]["functions"] = [{"function": function, "source": "block_entity", "ops": ops}]
        write_json(DATA / folder / "blocks" / "small_battery.json", table)
    write_json(ART / "small_battery.bbmodel", blockbench_model({
        "name": "small_battery", "textures": textures, "elements": BATTERY_ELEMENTS + lit_cells(4)}))


def write_small_tidal_turbine() -> None:
    """Static nacelle facing north (the rotor points into the current) and the rotor that spins around Z.
    The block itself comes with milestone 5."""
    textures = {"atlas": f"{NS}:block/small_tidal_turbine", "axis": "create:block/axis",
                "axis_top": "create:block/axis_top", "particle": f"{NS}:block/small_tidal_turbine"}
    model = lambda elements: {"parent": "block/block", "textures": textures, "elements": elements}  # noqa: E731
    turbine = turbine_art.TURBINE_ELEMENTS
    write_json(ASSETS / "models" / "block" / "small_tidal_turbine.json", model(turbine))
    write_json(ASSETS / "models" / "block" / "small_tidal_turbine" / "rotor.json", model(turbine_art.ROTOR_ELEMENTS))
    variants = {f"facing={f}": {"model": f"{NS}:block/small_tidal_turbine", **({"y": y} if y else {})}
                for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}
    write_json(ASSETS / "blockstates" / "small_tidal_turbine.json", {"variants": variants})
    # In inventories nothing spins: the item shows the nacelle, the rotor and the output shaft together.
    write_json(ASSETS / "models" / "item" / "small_tidal_turbine.json",
               model(turbine + turbine_art.ROTOR_ELEMENTS + turbine_art.ITEM_SHAFT))
    # No loot table yet: it would name an item that does not exist until the block is registered (milestone 5).
    write_json(ART / "small_tidal_turbine.bbmodel", blockbench_model({
        "name": "small_tidal_turbine", "textures": textures, "elements": turbine + turbine_art.ROTOR_ELEMENTS}))


def write_creative_blocks() -> None:
    """Full blocks with full-bright windows (see tools/textures/creative_art.py)."""
    for name, elements in (("creative_power_source", creative_art.SOURCE_ELEMENTS),
                           ("creative_power_consumer", creative_art.LOAD_ELEMENTS)):
        textures = {"atlas": f"{NS}:block/{name}", "particle": f"{NS}:block/{name}"}
        write_block(name, {"variants": {"": {"model": f"{NS}:block/{name}"}}},
                    {"parent": "block/block", "textures": textures, "elements": elements})


def minecraft_model(model: dict) -> dict:
    return {
        "parent": "block/block",
        "textures": model["textures"],
        "elements": [{k: v for k, v in e.items() if k != "name"} for e in model["elements"]],
    }


# --- Blockbench ---------------------------------------------------------------------------------------

def stable_uuid(*parts: str) -> str:
    return str(uuid.uuid5(uuid.NAMESPACE_URL, "deepcurrent/" + "/".join(parts)))


def blockbench_model(model: dict) -> dict:
    # Only our own textures go in the project; faces using another mod's texture (Create's shaft) are left out.
    texture_keys = [k for k in model["textures"] if k != "particle" and model["textures"][k].startswith(f"{NS}:")]
    textures = []
    for index, key in enumerate(texture_keys):
        ref = model["textures"][key].split(":", 1)[1]
        png = ASSETS / "textures" / f"{ref}.png"
        textures.append({
            "path": "",
            "name": png.name,
            "folder": ref.rsplit("/", 1)[0],
            "namespace": NS,
            "id": str(index),
            "particle": model["textures"]["particle"].endswith(ref),
            "render_mode": "default",
            "visible": True,
            "mode": "bitmap",
            "saved": True,
            "uuid": stable_uuid(model["name"], "texture", key),
            "relative_path": Path("..", png.relative_to(ROOT)).as_posix(),
            "source": "data:image/png;base64," + base64.b64encode(png.read_bytes()).decode(),
        })
    elements = []
    for e in model["elements"]:
        faces = {}
        for side, face in e["faces"].items():
            faces[side] = {"uv": face["uv"], "texture": texture_keys.index(face["texture"][1:])}
        elements.append({
            "name": e["name"],
            "box_uv": False,
            "rescale": False,
            "locked": False,
            "from": e["from"],
            "to": e["to"],
            "autouv": 0,
            "color": 0,
            "origin": e["rotation"]["origin"] if "rotation" in e else [8, 8, 8],
            **({"rotation": [e["rotation"]["angle"] if e["rotation"]["axis"] == axis else 0 for axis in "xyz"]}
               if "rotation" in e else {}),
            "faces": faces,
            "type": "cube",
            "uuid": stable_uuid(model["name"], "element", e["name"]),
        })
    return {
        "meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False},
        "name": model["name"],
        "model_identifier": "",
        "visible_box": [1, 1, 0],
        "resolution": {"width": 16, "height": 16},
        "elements": elements,
        "outliner": [e["uuid"] for e in elements],
        "textures": textures,
    }


# --- Blockstates, items, loot -------------------------------------------------------------------------

# Rotations turning an up-facing model to each facing, as vanilla does for end rods.
FACING_ROTATIONS = {
    "up": {}, "down": {"x": 180}, "north": {"x": 90}, "south": {"x": 90, "y": 180},
    "west": {"x": 90, "y": 270}, "east": {"x": 90, "y": 90},
}


def facing_blockstate(model: str) -> dict:
    return {"variants": {f"facing={f}": {"model": model, **rot} for f, rot in FACING_ROTATIONS.items()}}


def loot_table(block: str) -> dict:
    return {
        "type": "minecraft:block",
        "pools": [{
            "rolls": 1,
            "entries": [{"type": "minecraft:item", "name": f"{NS}:{block}"}],
            "conditions": [{"condition": "minecraft:survives_explosion"}],
        }],
    }


def write_block(name: str, blockstate: dict, model: dict | None) -> None:
    write_json(ASSETS / "blockstates" / f"{name}.json", blockstate)
    if model is not None:
        write_json(ASSETS / "models" / "block" / f"{name}.json", model)
    write_json(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{NS}:block/{name}"})
    # 1.20.1 reads loot_tables/, 1.21 reads loot_table/.
    for folder in ("loot_tables", "loot_table"):
        write_json(DATA / folder / "blocks" / f"{name}.json", loot_table(name))


def main() -> None:
    write_block("lv_connector", facing_blockstate(f"{NS}:block/lv_connector"), minecraft_model(LV_CONNECTOR))
    write_json(ART / "lv_connector.bbmodel", blockbench_model(LV_CONNECTOR))

    write_creative_blocks()

    write_alternator()
    write_electric_motor()
    write_small_battery()
    write_small_tidal_turbine()

    write_json(ASSETS / "models" / "item" / "lv_cable_coil.json",
               {"parent": "item/generated", "textures": {"layer0": f"{NS}:item/lv_cable_coil"}})
    print("models, blockstates, loot tables and art/lv_connector.bbmodel written")


if __name__ == "__main__":
    main()
