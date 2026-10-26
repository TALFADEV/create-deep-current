"""Helpers shared by the machine models: atlas regions to UVs, and named elements.

Machines use 32x32 atlases at 16 px per block, so UV = pixel / 2. A region is (x, y, width, height) in pixels.
"""

from __future__ import annotations

Region = tuple[int, int, int, int]


def uv(region: Region, du: float = 0, dv: float = 0, w: float | None = None, h: float | None = None,
       atlas: int = 32) -> list[float]:
    """UV of a region, or of the w x h part of it starting du, dv pixels in."""
    x, y, rw, rh = region
    w = rw if w is None else w
    h = rh if h is None else h
    k = 16 / atlas
    return [(x + du) * k, (y + dv) * k, (x + du + w) * k, (y + dv + h) * k]


def element(name: str, frm, to, faces: dict[str, list[float]], texture: str = "atlas") -> dict:
    """An element whose faces all use one texture; faces left out are not drawn."""
    return {"name": name, "from": frm, "to": to,
            "faces": {f: {"uv": face_uv, "texture": f"#{texture}"} for f, face_uv in faces.items()}}


def emissive(e: dict) -> dict:
    """Full brightness whatever the light around. Only neoforge_data is written (NeoForge rejects forge_data);
    the Forge build renames it to forge_data when it copies the models (build.forge.gradle.kts)."""
    e["neoforge_data"] = {"block_light": 15, "sky_light": 15}
    return e


def overlay(elements: list[dict], texture: str = "overlay", parts: tuple[str, ...] | None = None,
            inflate: float = 0.01) -> list[dict]:
    """Copies of the elements, a hair larger, with every face showing {@code texture} at its world position
    (the same UV Minecraft gives the block-breaking overlay), so the overlay runs on across elements.
    Only the elements named in {@code parts} when given (the machine's shell: small parts would only show
    scraps of the pattern). Rotated elements (blades) are left out: their faces do not line up with the grid."""
    out = []
    for e in elements:
        if "rotation" in e or (parts is not None and e.get("name") not in parts):
            continue
        (x0, y0, z0), (x1, y1, z1) = e["from"], e["to"]
        world = {
            "up": [x0, z0, x1, z1], "down": [x0, 16 - z1, x1, 16 - z0],
            "north": [16 - x1, 16 - y1, 16 - x0, 16 - y0], "south": [x0, 16 - y1, x1, 16 - y0],
            "west": [z0, 16 - y1, z1, 16 - y0], "east": [16 - z1, 16 - y1, 16 - z0, 16 - y0],
        }
        faces = {side: {"uv": world[side], "texture": f"#{texture}"} for side in e["faces"] if side in world}
        out.append({"name": f"{e.get('name', 'part')}_{texture}",
                    "from": [x0 - inflate, y0 - inflate, z0 - inflate],
                    "to": [x1 + inflate, y1 + inflate, z1 + inflate], "faces": faces})
    return out
