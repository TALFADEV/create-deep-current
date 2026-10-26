"""Isometric preview of block models with their textures, for reviewing art outside the game.

Usage:
    python tools/models/render_preview.py

Writes tools/textures/preview/<sheet>.png. Faces are shaded like Minecraft's (top 100 %, south 80 %, east 60 %).
"""

from __future__ import annotations

import io
import json
import math
import sys
import zipfile
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "textures"))

from extract_create_palette import find_jar  # noqa: E402

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "src" / "main" / "resources" / "assets" / "deepcurrent"
PREVIEW = ROOT / "tools" / "textures" / "preview"
BACKGROUND = (38, 42, 50, 255)
SHADE = {"up": 1.0, "south": 0.8, "east": 0.6}
COS30 = math.cos(math.radians(30))


def load_model(name: str) -> dict:
    """Resolves a block model with its elements; cube_bottom_top parents are expanded."""
    model = json.loads((ASSETS / "models" / "block" / f"{name}.json").read_text(encoding="utf-8"))
    if model.get("parent") == "block/cube_bottom_top":
        t = model["textures"]
        faces = {s: {"uv": [0, 0, 16, 16], "texture": "#side"} for s in ("north", "south", "east", "west")}
        faces["up"] = {"uv": [0, 0, 16, 16], "texture": "#top"}
        faces["down"] = {"uv": [0, 0, 16, 16], "texture": "#bottom"}
        model = {"textures": t, "elements": [{"from": [0, 0, 0], "to": [16, 16, 16], "faces": faces}]}
    return model


@lru_cache(maxsize=None)
def texture(ref: str) -> Image.Image:
    """Our textures from the assets; Create's from its jar (models may point at them)."""
    namespace, path = ref.split(":", 1)
    if namespace == "create":
        with zipfile.ZipFile(find_jar()) as archive:
            data = archive.read(f"assets/create/textures/{path}.png")
        return Image.open(io.BytesIO(data)).convert("RGBA").crop((0, 0, 16, 16))
    return Image.open(ASSETS / "textures" / f"{path}.png").convert("RGBA")


class Scene:
    def __init__(self, size: tuple[int, int], scale: float, origin: tuple[float, float]):
        self.image = Image.new("RGBA", size, BACKGROUND)
        self.draw = ImageDraw.Draw(self.image)
        self.scale = scale
        self.origin = origin
        self.polygons: list[tuple[float, list[tuple[float, float]], tuple[int, int, int, int]]] = []

    def project(self, x: float, y: float, z: float) -> tuple[float, float]:
        sx = (x - z) * COS30 * self.scale + self.origin[0]
        sy = ((x + z) * 0.5 - y) * self.scale + self.origin[1]
        return sx, sy

    def add_block(self, name: str, at: tuple[float, float, float]) -> None:
        model = load_model(name)
        textures = {key: texture(ref) for key, ref in model["textures"].items() if ":" in ref}
        self.add_model(model["elements"], textures, at)

    def add_model(self, elements: list[dict], textures: dict[str, Image.Image],
                  at: tuple[float, float, float]) -> None:
        """Elements in pixels; face textures are '#key' references into {@code textures}."""
        for e in elements:
            x0, y0, z0 = (c / 16 for c in e["from"])
            x1, y1, z1 = (c / 16 for c in e["to"])
            bx, by, bz = at
            turn = self._rotation(e.get("rotation"), at)
            for side in ("up", "south", "east"):
                face = e["faces"].get(side)
                if face is None:
                    continue
                img = textures[face["texture"][1:]]
                self._face(side, img, face["uv"], (bx + x0, by + y0, bz + z0), (bx + x1, by + y1, bz + z1), turn)

    @staticmethod
    def _rotation(rotation, at):
        """Element rotation as Minecraft applies it (right-handed, about the positive axis), or None."""
        if not rotation or not rotation.get("angle"):
            return None
        a = math.radians(rotation["angle"])
        ca, sa = math.cos(a), math.sin(a)
        ox, oy, oz = (o / 16 + b for o, b in zip(rotation["origin"], at))
        axis = rotation["axis"]

        def turn(p):
            x, y, z = p[0] - ox, p[1] - oy, p[2] - oz
            if axis == "x":
                y, z = y * ca - z * sa, y * sa + z * ca
            elif axis == "y":
                x, z = x * ca + z * sa, -x * sa + z * ca
            else:
                x, y = x * ca - y * sa, x * sa + y * ca
            return x + ox, y + oy, z + oz
        return turn

    def _face(self, side, img, uv, low, high, turn=None):
        # UVs run 0..16 whatever the texture size: an atlas of 32 px has 2 pixels per UV unit.
        k = img.width / 16
        u0, v0, u1, v1 = (c * k for c in uv)
        x0, y0, z0 = low
        x1, y1, z1 = high
        columns = max(1, round(abs(u1 - u0)))
        rows = max(1, round(abs(v1 - v0)))
        for i in range(columns):
            for j in range(rows):
                u = int(min(u0, u1) + i)
                v = int(min(v0, v1) + j)
                color = img.getpixel((min(u, img.width - 1), min(v, img.height - 1)))
                if color[3] == 0:
                    continue
                s0, s1 = i / columns, (i + 1) / columns
                t0, t1 = j / rows, (j + 1) / rows
                if side == "up":
                    corners = [(x0 + (x1 - x0) * s, y1, z0 + (z1 - z0) * t)
                               for s, t in ((s0, t0), (s1, t0), (s1, t1), (s0, t1))]
                elif side == "south":
                    corners = [(x0 + (x1 - x0) * s, y1 - (y1 - y0) * t, z1)
                               for s, t in ((s0, t0), (s1, t0), (s1, t1), (s0, t1))]
                else:  # east: u runs from south (z1) to north (z0)
                    corners = [(x1, y1 - (y1 - y0) * t, z1 - (z1 - z0) * s)
                               for s, t in ((s0, t0), (s1, t0), (s1, t1), (s0, t1))]
                if turn is not None:
                    corners = [turn(c) for c in corners]
                depth = sum(c[0] + c[1] + c[2] for c in corners) / 4
                shaded = tuple(int(c * SHADE[side]) for c in color[:3]) + (color[3],)
                self.polygons.append((depth, [self.project(*c) for c in corners], shaded,
                                      [c[0] + c[1] + c[2] for c in corners]))

    def add_cable(self, start, end, sag_per_block=0.06, segments=32):
        length = math.dist(start, end)
        sag = min(length * sag_per_block, 2.5)
        points = []
        for k in range(segments + 1):
            t = k / segments
            x = start[0] + (end[0] - start[0]) * t
            y = start[1] + (end[1] - start[1]) * t - sag * 4 * t * (1 - t)
            z = start[2] + (end[2] - start[2]) * t
            points.append((x, y, z))
        for a, b in zip(points, points[1:]):
            depth = sum(a) + 0.05
            self.polygons.append((depth, ("line", self.project(*a), self.project(*b)), (26, 26, 30, 255), None))

    @staticmethod
    def _raster(shape, corner_depths, depth, width, height):
        """Mask of the pixels a quad covers within its bounding box, and its depth there."""
        xs = [p[0] for p in shape]
        ys = [p[1] for p in shape]
        bx0, by0 = max(0, int(math.floor(min(xs)))), max(0, int(math.floor(min(ys))))
        bx1, by1 = min(width, int(math.ceil(max(xs))) + 1), min(height, int(math.ceil(max(ys))) + 1)
        if bx1 <= bx0 or by1 <= by0:
            return None, (bx0, by0, bx1, by1), None
        mask = Image.new("1", (bx1 - bx0, by1 - by0), 0)
        ImageDraw.Draw(mask).polygon([(x - bx0, y - by0) for x, y in shape], fill=1)
        (x_a, y_a), (x_b, y_b), (x_c, y_c) = shape[0], shape[1], shape[3]
        try:
            a, b, c = np.linalg.solve(np.array([[x_a, y_a, 1], [x_b, y_b, 1], [x_c, y_c, 1]]),
                                      [corner_depths[0], corner_depths[1], corner_depths[3]])
        except np.linalg.LinAlgError:
            a, b, c = 0, 0, depth
        gx, gy = np.meshgrid(np.arange(bx0, bx1) + 0.5, np.arange(by0, by1) + 0.5)
        return np.array(mask, dtype=bool), (bx0, by0, bx1, by1), a * gx + b * gy + c

    def render(self) -> Image.Image:
        """Rasterises every texel quad with a depth buffer (nearest to the viewer = largest x + y + z), so
        interleaved parts hide each other correctly; cables are drawn on top."""
        width, height = self.image.size
        zbuf = np.full((height, width), -np.inf)
        pixels = np.array(self.image)
        lines = []
        translucent = []
        for depth, shape, color, corner_depths in self.polygons:
            if shape[0] == "line":
                lines.append((shape, color))
                continue
            if color[3] < 255:
                translucent.append((depth, shape, color, corner_depths))
                continue
            xs = [p[0] for p in shape]
            ys = [p[1] for p in shape]
            bx0, by0 = max(0, int(math.floor(min(xs)))), max(0, int(math.floor(min(ys))))
            bx1, by1 = min(width, int(math.ceil(max(xs))) + 1), min(height, int(math.ceil(max(ys))) + 1)
            if bx1 <= bx0 or by1 <= by0:
                continue
            mask = Image.new("1", (bx1 - bx0, by1 - by0), 0)
            ImageDraw.Draw(mask).polygon([(x - bx0, y - by0) for x, y in shape], fill=1)
            inside = np.array(mask, dtype=bool)
            # Depth is affine on a plane under this orthographic projection: fit it on three corners.
            (x_a, y_a), (x_b, y_b), (x_c, y_c) = shape[0], shape[1], shape[3]
            matrix = np.array([[x_a, y_a, 1], [x_b, y_b, 1], [x_c, y_c, 1]])
            try:
                a, b, c = np.linalg.solve(matrix, [corner_depths[0], corner_depths[1], corner_depths[3]])
            except np.linalg.LinAlgError:
                a, b, c = 0, 0, depth
            gx, gy = np.meshgrid(np.arange(bx0, bx1) + 0.5, np.arange(by0, by1) + 0.5)
            d = a * gx + b * gy + c
            region = zbuf[by0:by1, bx0:bx1]
            closer = inside & (d > region - 1e-4)
            region[closer] = d[closer]
            pixels[by0:by1, bx0:bx1][closer] = color
        # Glass and other translucent texels: blended far to near over what is behind them, without hiding it.
        for depth, shape, color, corner_depths in sorted(translucent, key=lambda p: p[0]):
            mask, (bx0, by0, bx1, by1), d = self._raster(shape, corner_depths, depth, width, height)
            if mask is None:
                continue
            front = mask & (d > zbuf[by0:by1, bx0:bx1] - 1e-4)
            alpha = color[3] / 255
            target = pixels[by0:by1, bx0:bx1]
            for channel in range(3):
                layer = target[..., channel].astype(float)
                layer[front] = layer[front] * (1 - alpha) + color[channel] * alpha
                target[..., channel] = layer.astype(np.uint8)
        self.image = Image.fromarray(pixels)
        self.draw = ImageDraw.Draw(self.image)
        line_width = max(2, int(self.scale * 0.09))
        for shape, color in lines:
            self.draw.line([shape[1], shape[2]], fill=color, width=line_width)
            self.draw.line([shape[1], shape[2]], fill=(194, 122, 0, 255), width=1)
        return self.image


def anchor(block, facing_up_height=11 / 16):
    x, y, z = block
    return (x + 0.5, y + facing_up_height, z + 0.5)


def main() -> None:
    PREVIEW.mkdir(parents=True, exist_ok=True)

    # A small network: source and load 4 blocks apart, a connector on each, a cable between them.
    scene = Scene((760, 460), 64, (380, 200))
    scene.add_block("creative_power_source", (0, 0, 0))
    scene.add_block("lv_connector", (0, 1, 0))
    scene.add_block("creative_power_consumer", (4, 0, 1))
    scene.add_block("lv_connector", (4, 1, 1))
    scene.add_cable(anchor((0, 1, 0)), anchor((4, 1, 1)))
    network = scene.render()

    close_up = Scene((320, 360), 300, (160, 20))
    close_up.add_block("lv_connector", (-0.5, -0.9, -0.5))
    connector = close_up.render()

    sheet = Image.new("RGBA", (1100, 460), BACKGROUND)
    sheet.paste(network, (0, 0))
    sheet.paste(connector, (760, 0))
    coil = Image.open(ASSETS / "textures" / "item" / "lv_cable_coil.png").convert("RGBA").resize(
        (96, 96), Image.NEAREST)
    sheet.alpha_composite(coil, (790, 350))
    cable = Image.open(ASSETS / "textures" / "entity" / "cable_lv.png").convert("RGBA").resize(
        (96, 96), Image.NEAREST)
    sheet.alpha_composite(cable, (920, 350))
    out = PREVIEW / "m2_grid.png"
    sheet.save(out)
    print(f"wrote {out.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
