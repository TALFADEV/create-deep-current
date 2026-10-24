"""Pixel-art helpers in Create's style: noisy material fills, 1-px lit edges, recessed frames.

Everything is deterministic (seeded hash), so regenerating gives the same pixels.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

Color = tuple[int, int, int, int]

ROOT = Path(__file__).resolve().parents[2]
TEXTURES = ROOT / "src" / "main" / "resources" / "assets" / "deepcurrent" / "textures"


def canvas(width: int = 16, height: int = 16) -> Image.Image:
    return Image.new("RGBA", (width, height), (0, 0, 0, 0))


def _hash(x: int, y: int, seed: int) -> float:
    h = (x * 374761393 + y * 668265263 + seed * 2147483647) & 0xFFFFFFFF
    h = ((h ^ (h >> 13)) * 1274126177) & 0xFFFFFFFF
    return (h ^ (h >> 16)) / 0xFFFFFFFF


def pick(ramp: list[Color], index: int) -> Color:
    return ramp[max(0, min(len(ramp) - 1, index))]


def fill(img: Image.Image, x0: int, y0: int, x1: int, y1: int, ramp: list[Color], base: int,
         seed: int = 0, noise: float = 0.3) -> None:
    """Material fill of the inclusive box: mostly ramp[base], speckled one step lighter or darker."""
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            r = _hash(x, y, seed)
            delta = 1 if r < noise / 2 else -1 if r > 1 - noise / 2 else 0
            img.putpixel((x, y), pick(ramp, base + delta))


def bevel(img: Image.Image, x0: int, y0: int, x1: int, y1: int, ramp: list[Color], light: int,
          dark: int) -> None:
    """Lit top/left edge and shaded bottom/right edge."""
    for x in range(x0, x1 + 1):
        img.putpixel((x, y0), pick(ramp, light))
        img.putpixel((x, y1), pick(ramp, dark))
    for y in range(y0 + 1, y1):
        img.putpixel((x0, y), pick(ramp, light))
        img.putpixel((x1, y), pick(ramp, dark))


def plate(img: Image.Image, x0: int, y0: int, x1: int, y1: int, ramp: list[Color], seed: int = 0) -> None:
    """A top face: material fill with a bevelled rim."""
    fill(img, x0, y0, x1, y1, ramp, 3, seed)
    bevel(img, x0, y0, x1, y1, ramp, 5, 1)


def band(img: Image.Image, x0: int, y0: int, x1: int, y1: int, ramp: list[Color], seed: int = 0) -> None:
    """A side face: darker than the top, lit top row, shaded bottom row."""
    fill(img, x0, y0, x1, y1, ramp, 2, seed)
    for x in range(x0, x1 + 1):
        img.putpixel((x, y0), pick(ramp, 4))
        if y1 > y0:
            img.putpixel((x, y1), pick(ramp, 0))


def save(img: Image.Image, relative: str) -> Path:
    path = TEXTURES / relative
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path)
    return path
