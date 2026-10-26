"""Pixel canvas with the shading tools every Deep Current texture uses.

Light always comes from the top left: top and left edges are lit, bottom and right edges shaded. No random
noise: every pixel comes from a fill, a gradient, a bevel or an explicitly placed detail.
"""

from __future__ import annotations

from pathlib import Path

from PIL import Image

from .palette import Color

TEXTURES = Path(__file__).resolve().parents[3] / "src" / "main" / "resources" / "assets" / "deepcurrent" / "textures"


class Canvas:
    def __init__(self, width: int = 16, height: int = 16):
        self.img = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        self.width = width
        self.height = height

    # --- Pixels -----------------------------------------------------------------------------------------------

    def px(self, x: int, y: int, color: Color, alpha: int | None = None) -> None:
        if 0 <= x < self.width and 0 <= y < self.height:
            self.img.putpixel((x, y), color if alpha is None else color[:3] + (alpha,))

    def pixels(self, points: list[tuple[int, int]], color: Color) -> None:
        for x, y in points:
            self.px(x, y, color)

    def rect(self, x0: int, y0: int, x1: int, y1: int, color: Color, alpha: int | None = None) -> None:
        """Inclusive rectangle."""
        for y in range(y0, y1 + 1):
            for x in range(x0, x1 + 1):
                self.px(x, y, color, alpha)

    # --- Shading ----------------------------------------------------------------------------------------------

    def vertical_light(self, x0: int, y0: int, x1: int, y1: int, ramp: list[Color], top: int, bottom: int) -> None:
        """Fill whose tone steps down evenly from {@code top} (upper rows) to {@code bottom} (lower rows)."""
        rows = y1 - y0 + 1
        steps = top - bottom + 1
        for y in range(y0, y1 + 1):
            index = top - min(steps - 1, (y - y0) * steps // rows)
            self.rect(x0, y, x1, y, ramp[index])

    def bevel(self, x0: int, y0: int, x1: int, y1: int, light: Color, dark: Color,
              corner_light: Color | None = None) -> None:
        """Lit top/left edge, shaded bottom/right edge; the top-left corner can catch an extra glint."""
        for x in range(x0, x1 + 1):
            self.px(x, y0, light)
            self.px(x, y1, dark)
        for y in range(y0 + 1, y1):
            self.px(x0, y, light)
            self.px(x1, y, dark)
        if corner_light is not None:
            self.px(x0, y0, corner_light)

    def inset(self, x0: int, y0: int, x1: int, y1: int, shadow: Color, light: Color) -> None:
        """Recessed panel edge: the opposite of a bevel (shadow on top/left, light on bottom/right)."""
        self.bevel(x0, y0, x1, y1, shadow, light)

    def sheen(self, start: tuple[int, int], length: int, colors: list[Color]) -> None:
        """Diagonal reflection from bottom-left to top-right; one colour per parallel line, side by side."""
        x, y = start
        for offset, color in enumerate(colors):
            for step in range(length):
                self.px(x + offset + step, y - step, color)

    # --- Output -----------------------------------------------------------------------------------------------

    def save(self, relative: str) -> Path:
        path = TEXTURES / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        self.img.save(path)
        return path
