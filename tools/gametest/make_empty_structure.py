"""Writes the empty structures used as templates of the mod's game tests.

- empty: 1x1x1, for tests that only need a level.
- platform: 9x4x4 of air, for tests that place machines and connectors.

Usage:
    python tools/gametest/make_empty_structure.py

The same file is written under both folder names: data/<ns>/structures (1.20.1) and data/<ns>/structure (1.21+).
"""

from __future__ import annotations

import gzip
import struct
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DATA = ROOT / "src" / "main" / "resources" / "data" / "deepcurrent"
# 1.20.1; newer versions upgrade the structure on load.
DATA_VERSION = 3465

TAG_END, TAG_INT, TAG_STRING, TAG_LIST, TAG_COMPOUND = 0, 3, 8, 9, 10


def name(text: str) -> bytes:
    raw = text.encode("utf-8")
    return struct.pack(">H", len(raw)) + raw


def int_tag(key: str, value: int) -> bytes:
    return bytes([TAG_INT]) + name(key) + struct.pack(">i", value)


def string_tag(key: str, value: str) -> bytes:
    return bytes([TAG_STRING]) + name(key) + name(value)


def int_list(key: str, values: list[int]) -> bytes:
    body = bytes([TAG_INT]) + struct.pack(">i", len(values))
    body += b"".join(struct.pack(">i", v) for v in values)
    return bytes([TAG_LIST]) + name(key) + body


def compound_list(key: str, compounds: list[bytes]) -> bytes:
    element_type = TAG_COMPOUND if compounds else TAG_END
    body = bytes([element_type]) + struct.pack(">i", len(compounds))
    body += b"".join(c + bytes([TAG_END]) for c in compounds)
    return bytes([TAG_LIST]) + name(key) + body


def empty_structure(size: list[int]) -> bytes:
    root = (
        int_tag("DataVersion", DATA_VERSION)
        + int_list("size", size)
        + compound_list("palette", [string_tag("Name", "minecraft:air")])
        + compound_list("blocks", [int_list("pos", [0, 0, 0]) + int_tag("state", 0)])
        + compound_list("entities", [])
    )
    return bytes([TAG_COMPOUND]) + name("") + root + bytes([TAG_END])


STRUCTURES = {"empty": [1, 1, 1], "platform": [9, 4, 4]}


def main() -> None:
    for name, size in STRUCTURES.items():
        data = gzip.compress(empty_structure(size), mtime=0)
        for folder in ("structures", "structure"):
            path = DATA / folder / f"{name}.nbt"
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(data)
            print(f"wrote {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
