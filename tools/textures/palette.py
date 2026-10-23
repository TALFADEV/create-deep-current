"""Shared colour palette for every Deep Current texture.

Each ramp is ordered from darkest to lightest, so generators can shade with
``ramp[i]`` (0 = deepest shadow, -1 = highlight). Colours stay close to Create's
brass/copper/andesite look, with the marine touch of the GDD: verdigris patina,
portholes, anti-fouling paint, deep water and concretions.

Never hard-code a colour in a generator: add it here instead.
"""

from __future__ import annotations


def rgb(hex_code: str) -> tuple[int, int, int, int]:
    """'#RRGGBB' -> (r, g, b, 255)."""
    h = hex_code.lstrip("#")
    return int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), 255


def ramp(*hex_codes: str) -> list[tuple[int, int, int, int]]:
    return [rgb(h) for h in hex_codes]


TRANSPARENT = (0, 0, 0, 0)

# --- Metals (Create) ---------------------------------------------------------
BRASS = ramp("#5E3517", "#8E5A25", "#BD8636", "#E2B451", "#F6D978", "#FFF1B0")
COPPER = ramp("#5A2A1A", "#8A3E24", "#B85A33", "#D9774A", "#EE9A6C", "#F9C3A0")
ANDESITE_ALLOY = ramp("#3E3E39", "#55554F", "#6F6F68", "#8C8B83", "#A9A89F", "#C6C5BC")
# Riveted steel, tier 2 hull material.
STEEL = ramp("#2E3338", "#454C53", "#5E666E", "#7C858D", "#9DA6AD", "#C3CAD0")
# Abyssal alloy (polymetallic nodules), tier 3: dark, slightly purple.
ABYSSAL_ALLOY = ramp("#1C1824", "#2C2638", "#40384F", "#575069", "#726B85", "#948DA6")

# --- Marine weathering ---------------------------------------------------------
# Verdigris patina on copper and brass.
VERDIGRIS = ramp("#1F4A40", "#2E6A5A", "#3F8A74", "#55A88D", "#76C4A7", "#A3DDC4")
# Concretions overlay (shells, limestone, algae), drawn over fouled machines.
CONCRETION = ramp("#4A4E3A", "#6C6A50", "#8E866A", "#B2A98A", "#D3CBAE", "#ECE6CF")
ALGAE = ramp("#1E3A22", "#2D5531", "#3F7243", "#56905A")

# --- Paint -------------------------------------------------------------------------
ANTIFOULING_RED = ramp("#4C140D", "#7A2216", "#A63322", "#C8472F", "#E0674B")
SAFETY_YELLOW = ramp("#6B4A0C", "#9C7014", "#C9971E", "#EBBE2E", "#FAD95A")

# --- Glass and water ---------------------------------------------------------------
PORTHOLE_GLASS = ramp("#123245", "#1F4D63", "#3F8AA6", "#7CC4D6", "#BFE8F0", "#EFFBFF")
DEEP_WATER = ramp("#03080F", "#060F1A", "#0E2236", "#1B3B55", "#2B5A7A", "#3F7A9C")

# --- Hydrothermal vents --------------------------------------------------------------
VENT_ROCK = ramp("#151110", "#241D1A", "#3A3330", "#544A44", "#71665D")
VENT_GLOW = ramp("#7A1E05", "#C2410C", "#FF8A2B", "#FFB65C", "#FFE3A3")

# --- Structural accents --------------------------------------------------------------
RIVET = ramp("#2A170A", "#5E3517", "#FFF1B0")  # shadow, body, glint
OUTLINE = rgb("#1A120C")  # darkest edge line used around machine parts

# --- Cable voltage colours (emissive layers) -------------------------------------
# Low / high / extra-high voltage. The colour-blind mode adds patterns on top.
CABLE_LV = ramp("#7A4A00", "#C27A00", "#FFB13B", "#FFD98A")
CABLE_HV = ramp("#004A66", "#007FA8", "#3BD8FF", "#A8F0FF")
CABLE_EHV = ramp("#4A0066", "#8A1FB0", "#E040FB", "#F5B3FF")

RAMPS: dict[str, list[tuple[int, int, int, int]]] = {
    "brass": BRASS,
    "copper": COPPER,
    "andesite_alloy": ANDESITE_ALLOY,
    "steel": STEEL,
    "abyssal_alloy": ABYSSAL_ALLOY,
    "verdigris": VERDIGRIS,
    "concretion": CONCRETION,
    "algae": ALGAE,
    "antifouling_red": ANTIFOULING_RED,
    "safety_yellow": SAFETY_YELLOW,
    "porthole_glass": PORTHOLE_GLASS,
    "deep_water": DEEP_WATER,
    "vent_rock": VENT_ROCK,
    "vent_glow": VENT_GLOW,
    "rivet": RIVET,
    "cable_lv": CABLE_LV,
    "cable_hv": CABLE_HV,
    "cable_ehv": CABLE_EHV,
}
