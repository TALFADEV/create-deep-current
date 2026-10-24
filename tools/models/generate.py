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

from grid_textures import CONNECTOR_PARTS  # noqa: E402

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
        "connector": f"{NS}:block/lv_connector",
        "particle": f"{NS}:block/lv_connector",
    },
    # One element per part; face UVs point at the part's areas in the shared texture (see grid_textures.py).
    "elements": [element(part.name, part.frm, part.to, "connector", part.side_uv(), part.top_uv())
                 for part in CONNECTOR_PARTS],
}


def creative_block_model(belt: str) -> dict:
    """Create's creative casing on every face, with a metal belt around the middle (copper: source, andesite:
    load). Only the belt's sides show; its top and bottom are inside the cube."""
    cube_faces = {side: {"uv": [0, 0, 16, 16], "texture": "#casing", "cullface": side}
                  for side in (*SIDES, "up", "down")}
    belt_faces = {side: {"uv": [0, 0, 16, 2], "texture": "#belt"} for side in SIDES}
    return {
        "parent": "block/block",
        "textures": {
            "casing": "create:block/creative_casing",
            "belt": f"{NS}:block/creative_belt_{belt}",
            "particle": "create:block/creative_casing",
        },
        "elements": [
            {"from": [0, 0, 0], "to": [16, 16, 16], "faces": cube_faces},
            {"from": [-0.5, 7, -0.5], "to": [16.5, 9, 16.5], "faces": belt_faces},
        ],
    }


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
    texture_keys = [k for k in model["textures"] if k != "particle"]
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
            "origin": [8, 8, 8],
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

    for name, belt in (("creative_power_source", "source"), ("creative_power_consumer", "load")):
        write_block(name, {"variants": {"": {"model": f"{NS}:block/{name}"}}}, creative_block_model(belt))

    write_json(ASSETS / "models" / "item" / "lv_cable_coil.json",
               {"parent": "item/generated", "textures": {"layer0": f"{NS}:item/lv_cable_coil"}})
    print("models, blockstates, loot tables and art/lv_connector.bbmodel written")


if __name__ == "__main__":
    main()
