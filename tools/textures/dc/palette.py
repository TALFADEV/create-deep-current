"""Reference palette of Create: Deep Current, from the author's art-direction boards.

Every ramp has 7 tones, index 0 (deepest shadow) to 6 (brightest highlight). Shadows shift hue (warm metals go
toward red-brown, cold metals toward blue), highlights toward pale yellow or cyan: shading is never brightness
alone. Textures only use these colours, which keeps every machine of the addon in the same family.
"""

from __future__ import annotations

Color = tuple[int, int, int, int]


def _ramp(*hex_codes: str) -> list[Color]:
    out = []
    for code in hex_codes:
        h = code.lstrip("#")
        out.append((int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255))
    return out


# Metals
BRASS = _ramp("#3D2410", "#6B4419", "#9C6B26", "#C9973A", "#E6BC55", "#F6DC86", "#FFF4C4")
COPPER = _ramp("#3A140E", "#6A2617", "#9C3E22", "#C75B33", "#E27F4E", "#F2A777", "#FBD3B0")
VERDIGRIS = _ramp("#12302C", "#1D4D44", "#2A6D5E", "#3E907A", "#5DB195", "#8ED2B4", "#C2EDD6")
STEEL = _ramp("#1C1D22", "#2C2E35", "#40434B", "#585C65", "#737881", "#949AA2", "#BEC3C9")
ABYSSAL = _ramp("#0A1018", "#111D2B", "#1A2E42", "#25445C", "#2F6078", "#3E8296", "#5FB0BE")
HADAL = _ramp("#07080B", "#0E1015", "#161920", "#20242D", "#2D3340", "#3F4757", "#5C6678")

# Glass and paints
GLASS = _ramp("#0E2A36", "#1C4A5C", "#2E7189", "#4E9BB2", "#82C4D6", "#BFE6EF", "#F0FBFF")
PAINT_RED = _ramp("#2A0A08", "#4F120E", "#7A1C16", "#A3281E", "#C83A2A", "#E2604A", "#F49A82")
PAINT_YELLOW = _ramp("#3A2706", "#6B4A0C", "#A07214", "#CF9A1E", "#EDBE2E", "#F8DA62", "#FFF0A8")

# Light
HV_GLOW = _ramp("#04232E", "#0A4458", "#0F6E8A", "#16A0C2", "#3CCFEA", "#8CEBFA", "#E0FDFF")
BIOLUMINESCENCE = _ramp("#120A3A", "#1E1C70", "#2438A8", "#2A62D6", "#2E9CE8", "#3FD6F0", "#A6F7FF")

# Creative blocks only (test tools, like Create's creative items): a magenta frame over a night-blue checker,
# ramps sampled from the tones of Create's creative casing.
CREATIVE = _ramp("#241430", "#3A1F46", "#573062", "#7A4580", "#9F5D9F", "#C07DB8", "#E2B4DA")
CREATIVE_NIGHT = _ramp("#05041A", "#0E0C22", "#161529", "#22203A", "#33304E", "#4A4668", "#6A6690")

# Darkest line, for gaps and deep recesses.
OUTLINE: Color = (11, 11, 14, 255)

RAMPS: dict[str, list[Color]] = {
    "Laiton": BRASS,
    "Cuivre": COPPER,
    "Cuivre oxydé": VERDIGRIS,
    "Acier riveté": STEEL,
    "Alliage abyssal": ABYSSAL,
    "Coque hadale": HADAL,
    "Verre renforcé": GLASS,
    "Peinture rouge": PAINT_RED,
    "Peinture jaune": PAINT_YELLOW,
    "Lueur HT": HV_GLOW,
    "Bioluminescence": BIOLUMINESCENCE,
    "Créatif": CREATIVE,
    "Créatif nuit": CREATIVE_NIGHT,
}


def all_colors() -> set[tuple[int, int, int]]:
    """Every colour of the palette (RGB), for checking that textures stay inside it."""
    colors = {c[:3] for ramp in RAMPS.values() for c in ramp}
    colors.add(OUTLINE[:3])
    return colors
