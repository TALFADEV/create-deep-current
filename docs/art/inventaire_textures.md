# Inventaire graphique — Create: Deep Current

Source : `DEEP_CURRENT_GDD.md` (§3 à §12). Ce document liste toutes les ressources graphiques prévues, par version.
Il ne crée aucune fonctionnalité : chaque ligne renvoie à un élément du GDD.

Conventions :
- **B** = texture de bloc (`textures/block/`), **I** = icône d'objet (`textures/item/`), **E** = entité ou rendu spécial
  (`textures/entity/`), **P** = modèle partiel animé (rendu Create/Flywheel), **G** = interface (`textures/gui/`).
- Résolution : 16 px par bloc. Les machines utilisent des atlas 32×32 ou 64×64 à cette même densité.
- États : *arrêt / actif / endommagé / encrassé* uniquement quand la mécanique correspondante existe dans la version.
- Statut : ✅ fait, 🔁 à refaire dans la nouvelle direction artistique, ⬜ à faire.

## 0. Socle commun (phase 1)

| Élément | Fichiers | Statut |
|---|---|---|
| Palette de référence | `tools/textures/dc/palette.py` | ✅ phase 1 |
| Laiton, cuivre, cuivre oxydé | B `material/brass`, `material/copper`, `material/oxidized_copper` | ✅ phase 1 |
| Acier riveté (palier 2) | B `material/riveted_steel` | ✅ phase 1 |
| Alliage abyssal (palier 3) | B `material/abyssal_alloy` | ✅ phase 1 |
| Coque hadale (palier 4) | B `material/hadal_hull` | ✅ phase 1 |
| Verre renforcé / hublot | B `material/reinforced_glass` (transparence) | ✅ phase 1 |
| Peinture anti-corrosion rouge, jaune | B `material/antifouling_red`, `material/safety_yellow` | ✅ phase 1 |

## 1. Alpha — « Premier courant »

| Élément (GDD) | Fichiers | États | Statut |
|---|---|---|---|
| Petite hydrolienne (§4.2) | B atlas `small_tidal_turbine`, P rotor/pales | arrêt, active ; intégrité 75/50/25 % (§3.4) | ✅ phase 2 |
| Alternateur (§4.1) | B atlas `alternator`, P rotor | arrêt, actif (lueur) | ✅ phase 2 |
| Moteur électrique (§4.1) | B atlas `electric_motor`, P rotor | arrêt, actif | ✅ phase 2 |
| Câble BT (§4.5, §4.6) | E `cable_lv` | normal ; aperçu valide / invalide | ✅ phase 2 |
| Connecteur BT (§4.6) | B atlas `lv_connector` | — | ✅ phase 2 |
| Bobine de câble BT (§4.6) | I `lv_cable_coil` | — | ✅ phase 2 |
| Petite batterie (§4.7) | B atlas `small_battery` | niveaux de charge (voyant) | ✅ phase 2 |
| Source et charge créatives (§12) | B atlas `creative_power_*` | — | ✅ phase 2 (à valider) |
| Dégradation palier 1 (§3.4) | B superposition `overlay/cracks_1..3` (fissures) | 75 / 50 / 25 % | ✅ phase 2 |

## 2. V1 — « Premiers courants »

| Élément (GDD) | Fichiers | États | Statut |
|---|---|---|---|
| Grande hydrolienne, multibloc (§4.2) | B atlas, P pales | arrêt, active | ⬜ |
| Générateur hydrothermal (§4.2) | B atlas | suit la cheminée (dormant, actif, éruption) | ⬜ |
| Cheminée hydrothermale, génération (§4.4) | B `vent` + `vent_top` animé (`.mcmeta`) | dormante, active, éruption | ⬜ |
| Sédiments des fosses (§3.2) | B si un bloc dédié est retenu (à valider) | — | ⬜ |
| Câble HT (§4.5) | E `cable_hv` (motif distinct + émissif) | normal | ⬜ |
| Connecteur HT, bobine HT | B atlas `hv_connector`, I `hv_cable_coil` | — | ⬜ |
| Transformateurs BT↔HT (§4.5) | B atlas `transformer` | arrêt, actif | ⬜ |
| Disjoncteur (§4.5) | B atlas `breaker` | armé, déclenché | ⬜ |
| Batterie moyenne (§4.7) | B atlas `medium_battery` | niveaux de charge | ⬜ |
| Acier riveté : lingot, plaque (§3.3) | I `riveted_steel_ingot`, `riveted_steel_sheet` | — | ⬜ |
| Alliage abyssal : lingot, plaque (§7) | I `abyssal_alloy_ingot`, `abyssal_alloy_sheet` | — | ⬜ |
| Nodules polymétalliques (§7) | B gisement, I `polymetallic_nodule` | gisement épuisé / régénéré | ⬜ |
| Sulfures (§7) | B gisement, I `sulfide` | épuisé / régénéré | ⬜ |
| Collecteur de nodules, foreuse à sulfures (§7) | B atlas, P parties mobiles | arrêt, actif | ⬜ |
| Sonar fixe (§6.3) | B atlas `sonar` | arrêt, balayage | ⬜ |
| Balise lumineuse (§6.3) | B `beacon` (émissif) | éteinte, allumée | ⬜ |
| Bouée, phare (§10 bis) | B atlas `buoy`, `lighthouse` (émissif) | éteint, allumé | ⬜ |
| Carnet d'ingénieur (§10 ter) | I `engineer_notebook` | — | ⬜ |
| Profondimètre (§10 ter) | G `depth_gauge` | normal, orange, rouge | ⬜ |
| Arbre de progrès (§10 bis) | réutilise les icônes d'objets | — | ⬜ |

## 3. V2 — « Grands fonds » (inventaire seulement, production sur demande)

| Élément (GDD) | Fichiers prévus |
|---|---|
| Flotteurs à houle, barrage marémoteur (§4.2) | B atlas flotteur, multibloc barrage, P parties mobiles |
| ETM (§4.2) | B atlas colonne (segments haut / milieu / bas) |
| Encrassement (§5) | B superposition `overlay/fouling_1..3`, I grattoir, B nettoyeur, I peinture anti-corrosion |
| Scaphandres paliers 1 à 3 (§3.5) | I casque, plastron, jambières, bottes ×3 ; E textures d'armure ×3 |
| Bathyscaphe (§6.1) | E texture d'entité, I objet |
| Habitats pressurisés (§6.4) | B coque, sas (ouvert / fermé), pompe à air, hublot |
| Cartographie (§6.3) | B table, I carte bathymétrique |
| Hydrates de méthane (§7) | B gisement, I hydrate, B extracteur, fluide méthane |
| Câble THT, transformateur THT multibloc (§4.5) | E `cable_ehv`, B atlas |
| Salle de contrôle (§10 bis) | B écrans de supervision |
| Électrolyseur, réservoir, hydrogène (§10 bis) | B atlas, fluide |
| Concrétions récupérables (§10 bis) | I coquillages, corail, calcaire |
| Épave du bathyscaphe, balise de détresse (§10 ter) | B épave, B balise (émissive) |

## 4. V3 — « Abysses » (inventaire seulement)

| Élément (GDD) | Fichiers prévus |
|---|---|
| Palier 4, coque hadale (§3.3) | I lingot / plaque hadale |
| Champs de cheminées, mégacentrale (§4.2) | B multibloc, P parties mobiles |
| ROV et console (§6.3) | E ROV, B console, E câble ombilical |
| Navire câblier (§4.6) | B dérouleur de câble |
| Sonar portatif (§6.3) | I |
| Faune : poissons, méduses, crustacés, rongeur de câbles, prédateur (§8.1) | E textures d'entités |
| Glissements de terrain, stations abandonnées (§8.2, §8.3) | réutilisent les blocs existants |
| Organismes bioluminescents, aspirateur de fond (§7) | B organismes (émissif), B machine, I peinture lumineuse |
| Convertisseur FE (§4.8) | B atlas |
| Désalinisation, pipelines (§10 bis) | B usine, B tuyau sous-marin |

## Audit de l'existant (2026-10-26)

Utilisées par les modèles : `block/alternator`, `block/lv_connector`, `block/copper_windings`, `block/brass_tile`,
`block/creative_belt_source`, `block/creative_belt_load`, `item/lv_cable_coil`, `entity/cable_lv`.
Les blocs créatifs utilisent aussi `create:block/creative_casing` de Create.

Orphelines (restes des variantes explorées, plus référencées) : `block/andesite_tile`, `block/copper_tile`,
`block/dark_iron_tile`, `block/dark_iron_riveted_tile`, `block/heavy_iron_tile`, `block/mark_lv_tile`,
`block/iron_casing_{creative,plate,shaft,vent,window}`. À supprimer après validation.

Anciens générateurs remplacés par `tools/textures/dc/` au fil des phases : `grid_textures.py`, `machine_textures.py`,
`industrial_textures.py`, `parts.py`, `pixels.py`, `palette.py` (palette échantillonnée de Create).
