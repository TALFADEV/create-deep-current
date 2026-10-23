# Create: Deep Current — Document de conception (GDD)

> Addon pour le mod **Create** : l'énergie la plus puissante du jeu se trouve au fond des océans.
> Le joueur conçoit des centrales marines, transporte l'électricité par câbles sous-marins et affronte la pression, l'obscurité et les dangers des grands fonds.

Version du document : 2.2. Auteur : TALFA. Statut : entièrement validé par l'auteur.

---

## 1. Vision

- **Pitch** : « L'électricité classique reste en surface. La vraie puissance dort au fond des océans. »
- **Fantasy du joueur** : ingénieur offshore. Il part d'une hydrolienne dans une baie et finit par une mégacentrale hydrothermale reliée à sa base par des kilomètres de câbles sous-marins.
- **Piliers de design** :
  1. **Esprit Create** : tout est visible, mécanique et animé. Rotation aux deux bouts de la chaîne.
  2. **La profondeur est la progression** : plus on descend, plus l'énergie est puissante et plus l'ingénierie est exigeante.
  3. **Semi-réaliste** : des mécaniques crédibles (pression, pertes en ligne, marées), mais des chiffres simplifiés et un jeu fluide.
  4. **Le défi est technique** : pas de boss. L'objectif final est la mégacentrale.
- **Différenciation** : aucun addon Create n'exploite l'industrie sous-marine. Les mods d'abysses existants sont orientés aventure et biomes, et souvent incompatibles avec les mondes existants. Deep Current s'installe dans **n'importe quel monde ou modpack**.

## 2. Décisions validées

| Sujet | Décision |
|---|---|
| Versions | **1.20.1 Forge** et **1.21.1 NeoForge** dès la V1 |
| Monde | Océans **vanilla** (génération dans les océans existants, compatible avec les mondes existants) |
| Réalisme | Semi-réaliste façon Create |
| Énergie | **Hybride** : rotation → électricité (transport) → rotation |
| FE (Forge Energy) | Plus tard, convertisseur optionnel |
| Sources | Hydroliennes, marémotrice/houle, cheminées hydrothermales, ETM (thermique des mers) |
| Cheminées | Cycles d'activité (calme / active / éruption) |
| Marées | Liées à l'heure et à la phase lunaire |
| Stockage | Batteries |
| Surpression | Dégradation progressive |
| Paliers de pression | 4 |
| Pression sur le joueur | Oui, configurable |
| Entretien des machines | Oui (corrosion, concrétions), configurable |
| Pose des câbles | À la main (courte distance) + navire câblier (longue distance) |
| Réseau | Pertes selon la distance + transformateurs (niveaux de tension) |
| Surcharge | Disjoncteurs ; sans protection, le câble brûle |
| Dommages aux câbles | Créatures, éruptions, ancres et bateaux |
| Déplacement | Bathyscaphe propre au mod + compatibilité avec les mods de véhicules Create |
| Exploration | Sonar, drone téléguidé (ROV), cartographie des fonds, balises lumineuses |
| Bases | Habitats pressurisés (sas, air pompé) |
| Obscurité | Noir croissant avec la profondeur |
| Ressources | Nodules polymétalliques, sulfures, hydrates de méthane, organismes bioluminescents |
| Extraction | Machines Create dédiées |
| Renouvellement | Régénération lente des gisements |
| Usages des ressources | Matériaux des paliers, composants électriques, équipement du joueur, carburants |
| Créatures | Faune légère + quelques menaces |
| Dangers | Éruptions, fuites de méthane, courants violents, glissements de terrain |
| Fin de jeu | Pas de boss : la mégacentrale est le défi |
| Structures | Épaves industrielles, stations abandonnées, champs de cheminées |
| Déblocage | Tout est craftable (pas de plans à trouver) |
| Recettes | Mixtes : objets simples en table de craft, composants avancés via les chaînes Create |
| Durée de progression | 20 à 30 heures |
| Guide en jeu | Scènes Ponder + carnet d'ingénieur |
| Style | « Create marin » : laiton, cuivre, rivets, patine verte, hublots, peinture anti-corrosion |
| Ambiance | Bulles et particules, lumières des machines, sons sous-marins, sons de pression |
| Taille des machines | Progressive : petits blocs au début, multiblocs de plus en plus grands |
| Graphismes | Modèles 3D et textures réalisés par **Claude Code** (fichiers JSON de modèles, `.bbmodel` Blockbench, textures PNG générées par script) ; l'auteur relit et retouche dans Blockbench si besoin |
| Nom | **Create: Deep Current** |
| Compatibilités | JEI/EMI, JourneyMap (V1), Xaero's et FTB Chunks (V2), mods de véhicules Create, CC: Tweaked, mods électriques Create |
| Licence | Tous droits réservés (usage libre dans les modpacks) |
| Langues | Anglais + français, autres langues par la communauté (Crowdin) |
| Équilibrage | Progressif : hydrolienne ≈ petite machine à vapeur Create ; mégacentrale ≈ 10 grosses machines à vapeur |
| Unités affichées | Unités réelles simplifiées (kW, kV, A), chiffres arrondis et cohérents |
| Acier riveté | Accepte l'acier des autres mods via les tags communs (`c:ingots/steel`, `forge:ingots/steel`) ; le mod fournit sa propre recette d'acier si aucun mod d'acier n'est présent |
| Multijoueur | Respect des mods de protection (claims) : aucun dégât ni interaction sur un réseau situé dans une zone protégée |
| Bathyscaphe | Entité véhicule (pas une contraption) |
| Difficulté | Les menaces suivent la difficulté : absentes en Paisible, plus agressives en Difficile |
| Outils admin | Commandes de debug + blocs créatifs |
| Sorties | Alpha publique tôt, puis versions incrémentales jusqu'à la V1 |

---

## 3. Profondeur et paliers de pression

### 3.1 Mesure de la profondeur
- `profondeur = niveauDeLaMer - y` pour un bloc immergé (niveau de la mer = 63 en vanilla, lu dynamiquement depuis le monde).
- Un bloc n'est « sous pression » que s'il est entouré d'eau (ou dans une zone immergée). Un bloc dans un habitat pressurisé ou une poche d'air **n'est pas** soumis à la pression extérieure.

### 3.2 Fosses abyssales (validé)
Les océans profonds vanilla ne font qu'environ 30 à 40 blocs de profondeur. Pour donner une vraie sensation d'abysse sans exiger de nouveau monde, le mod ajoute des **fosses abyssales** : de rares failles générées dans les biomes d'océan profond, qui descendent jusque dans la couche de deepslate (vers Y −40, configurable). Elles ne concernent que les nouveaux chunks, donc le mod reste compatible avec les mondes existants.
- Les fosses sont **en V1**, car elles sont nécessaires pour exploiter réellement les paliers 2 et 3. Le palier 4 et les champs de cheminées en leur fond restent en V3.
- Les cheminées hydrothermales apparaissent au fond des océans profonds et, plus fréquemment, dans les fosses.
- Config : fréquence, profondeur max, largeur.

### 3.3 Les 4 paliers (valeurs par défaut, configurables)

| Palier | Matériau de coque | Profondeur max | Zone typique |
|---|---|---|---|
| 1 | Laiton (Create) | 16 blocs | Baies, littoral |
| 2 | Acier riveté | 32 blocs | Plateau, océan |
| 3 | Alliage abyssal (nodules) | 64 blocs | Fond des océans profonds, haut des fosses |
| 4 | Coque hadale (sulfures + alliage) | Illimitée | Fond des fosses abyssales |

### 3.4 Dégradation progressive (surpression)
Chaque machine immergée a une **intégrité** de 0 à 100 %.
- Au-delà de sa profondeur max : l'intégrité baisse avec le temps, plus vite si le dépassement est grand.
- **Effets par seuil** : sous 75 %, grincements et bulles ; sous 50 %, rendement −25 % et fuites visuelles ; sous 25 %, rendement −60 % et craquements ; à 0 %, la machine casse (drop partiel des matériaux).
- La réparation se fait avec un outil ou une machine de maintenance, et coûte des matériaux du palier de la machine.
- Config : vitesse de dégradation, activation ou non de la casse.

### 3.5 Pression sur le joueur (configurable, activée par défaut)
- Sans protection, le joueur subit des dégâts croissants sous une profondeur seuil.
- **Scaphandres** par palier : le palier 1 protège jusqu'à 16 blocs, et ainsi de suite.
- Prévoir une option de désactivation pour éviter les conflits avec d'autres mods de pression.

### 3.6 Obscurité
- La luminosité perçue diminue avec la profondeur (effet client : brouillard et réduction de la visibilité), jusqu'au noir quasi total au fond des fosses.
- Projecteurs, lampes de casque, balises et organismes bioluminescents deviennent indispensables.
- Config : intensité (vanilla / croissant / noir total).

---

## 4. Énergie

### 4.1 Chaîne énergétique (modèle hybride)
```
[Générateur marin] --rotation--> [Alternateur] --électricité--> [Câbles + transformateurs] --> [Moteur électrique] --rotation--> [Usine Create]
                                                         \--> [Batterie]
```
- **Au fond comme dans la base, tout est rotation Create** (SU/RPM). L'électricité ne sert **qu'au transport et au stockage**.
- Les machines du mod qui consomment de l'énergie (sonar, pompes à air, etc.) consomment de la rotation ou de l'électricité selon ce qui est le plus logique ; à trancher machine par machine dans la spec technique.

### 4.2 Sources d'énergie

**Hydrolienne (palier 1, début de jeu)**
- Une turbine immergée tourne selon le courant local. La production dépend du coefficient de marée (§4.3) et de la vitesse du courant de la zone.
- Petit modèle à un bloc, puis grande hydrolienne en multibloc (palier 2).

**Marémotrice / houlomotrice (palier 1-2)**
- Flotteurs de surface entraînés par la houle : une production faible mais régulière, plus forte par mauvais temps.
- Barrage marémoteur en multibloc dans une baie : forte production aux changements de marée.

**ETM, énergie thermique des mers (palier 3)**
- Une colonne qui relie l'eau chaude de la surface à l'eau froide du fond. Plus la colonne est haute, plus la production est forte.
- Production stable, idéale pour la base du réseau.

**Générateur hydrothermal (palier 3-4, fin de jeu)**
- Se place sur une cheminée hydrothermale. C'est la production la plus forte du jeu, qui suit le cycle de la cheminée (§4.4).
- Évolue jusqu'à la **mégacentrale hydrothermale** : un multibloc géant sur un champ de cheminées, objectif final du mod.

### 4.3 Marées et courants
- **Coefficient de marée** calculé à partir de l'heure du monde et de la phase lunaire (8 phases vanilla) : pleine lune et nouvelle lune donnent de grandes marées, les quartiers de petites marées.
- Deux marées hautes et deux basses par jour Minecraft.
- **Courants par zone** : chaque région d'océan a une vitesse de courant de base (bruit déterministe calculé à partir de la seed), révélée par le sonar et la cartographie.
- Production d'une hydrolienne = courant de la zone × coefficient de marée × rendement.
- Pendant les grandes marées, des **courants violents** (§8.2) apparaissent dans certaines zones.

### 4.4 Cycle des cheminées hydrothermales
| État | Durée par défaut | Production | Effets |
|---|---|---|---|
| Dormante | 2–4 jours MC | 30 % | Fumée légère |
| Active | 3–6 jours MC | 100 % | Fumée noire, chaleur |
| Éruption | 2–5 min réelles | 250 % | Eau brûlante, projections, dégâts aux câbles et machines proches |

- Les transitions sont annoncées (grondements, tremblements de particules) pour laisser le joueur réagir.
- Toutes les durées et tous les multiplicateurs sont configurables.

### 4.5 Réseau électrique
- **Niveaux de tension** : BT (basse), HT (haute), THT (très haute). Chaque câble a une tension max et une intensité max.
- **Pertes en ligne** proportionnelles à la distance et inversement liées à la tension : transporter loin impose de monter en tension avec un transformateur.
- **Transformateurs** élévateurs et abaisseurs (multibloc au niveau THT).
- **Disjoncteurs** : ils coupent en cas de surcharge et doivent être réarmés (à la main ou par signal redstone). Un câble non protégé en surcharge **brûle** (le bloc est détruit, avec effets visuels).
- **Calcul** : le réseau est un **graphe** recalculé uniquement lorsqu'il change (ajout ou retrait d'un bloc). Pas de calcul bloc par bloc à chaque tick. Le bilan production/consommation est résolu par réseau.

### 4.6 Câbles sous-marins
- **Pose manuelle** : on relie deux connecteurs avec une bobine de câble, sur une portée limitée (configurable). Le câble est rendu suspendu ou posé au fond.
- **Navire câblier** : une contraption avec un dérouleur de câble qui pose automatiquement le câble sur de longues distances en avançant.
- **Dommages** : créatures qui rongent les câbles (§8.1), éruptions à proximité, ancres et bateaux pour un câble posé à moins de N blocs de la surface. Un câble endommagé perd du rendement, puis coupe. Il se répare avec un outil.

### 4.7 Stockage
- **Batteries** en 3 tailles (petite, moyenne, multibloc). Capacité croissante, avec des composants issus des ressources abyssales pour les plus grandes.
- Elles servent à lisser les variations de marée et les cycles des cheminées.

### 4.8 Compatibilité FE (post-V1)
- Un convertisseur électricité ↔ FE optionnel, désactivable en config.

---

## 5. Entretien et corrosion (configurable)
- Chaque machine immergée accumule de l'**encrassement** (concrétions, coquillages) de 0 à 100 % au fil du temps.
- L'encrassement réduit le rendement jusqu'à −50 %, avec un rendu visuel (texture de concrétions qui se superpose).
- **Nettoyage** : à la main avec un grattoir, ou automatisé avec un nettoyeur (déployeur + brosse ou une machine dédiée).
- La **peinture anti-corrosion** ralentit l'encrassement.

---

## 6. Exploration et mobilité

### 6.1 Bathyscaphe (propre au mod)
- Petit submersible pour 1 ou 2 joueurs. Il tient la pression selon son palier de coque (améliorable), avec projecteurs, réserve d'air et énergie (batterie).
- Contrôles : montée, descente, déplacement et projecteurs. Le niveau de pression s'affiche sur l'interface.

### 6.2 Compatibilité avec les mods de véhicules Create
- Si un mod de contraptions physiques est présent, les blocs du mod (coques, ballasts, hélices sous-marines) permettent d'y construire des sous-marins et des navires câbliers.
- Les mods exacts à supporter seront à identifier selon la version (voir la liste en §13).

### 6.3 Outils d'exploration
- **Sonar** : bloc fixe (balayage de zone) puis version portative. Il révèle le relief, les cheminées, les gisements et les courants. Il consomme de l'énergie.
- **Drone téléguidé (ROV)** : robot relié par un câble ombilical, piloté depuis une console en surface (vue caméra). Il peut explorer, prélever et réparer des câbles.
- **Cartographie des fonds** : une table qui imprime des **cartes bathymétriques** (profondeur en couleurs, courants, cheminées repérées) à partir des données du sonar.
- **Balises lumineuses** : elles éclairent, servent de repères et s'affichent sur les cartes.

### 6.4 Habitats pressurisés
- Des blocs de coque étanche forment une structure. Un **sas** permet d'entrer et de sortir, et une **pompe à air** (alimentée depuis la surface ou par électricité) assèche l'intérieur.
- Une fuite se produit si la structure dépasse son palier de pression ou si une brèche apparaît.
- L'intérieur est une zone non soumise à la pression, où le joueur respire normalement.

---

## 7. Ressources des grands fonds

| Ressource | Où | Extraction | Usages principaux |
|---|---|---|---|
| Nodules polymétalliques | Plaines du fond des océans profonds | Collecteur de nodules | Alliage abyssal (palier 3), composants électriques |
| Sulfures | Autour des cheminées | Foreuse à sulfures | Coque hadale (palier 4), supraconducteurs, batteries |
| Hydrates de méthane | Poches dans les fosses et sédiments | Extracteur d'hydrates (refroidi) | Carburant puissant pour les chaudières et moteurs Create |
| Organismes bioluminescents | Près des cheminées et dans les fosses | Aspirateur de fond | Éclairage, composés chimiques, peinture lumineuse |

- **Régénération lente** : un gisement exploité se reforme progressivement (durée configurable). Il faut donc alterner les zones.
- **Hydrates instables** : chauffés ou mal stockés, ils libèrent du méthane, ce qui crée un risque d'explosion (§8.2).

---

## 8. Dangers et vie marine

### 8.1 Faune (légère)
- **Ambiance** : bancs de poissons abyssaux, méduses lumineuses, crustacés des cheminées.
- **Menaces** (quelques espèces) :
  - Une créature qui **ronge les câbles** et les machines isolées ; on s'en protège avec des gaines ou des répulsifs.
  - Un prédateur des fosses, attiré par la lumière et le bruit des machines.
- Pas de boss.

### 8.2 Dangers environnementaux
- **Éruptions hydrothermales** : eau brûlante et projections ; elles endommagent les joueurs, les machines et les câbles dans un rayon donné.
- **Fuites de méthane** : bulles de gaz qui s'échappent des sédiments ; elles explosent au contact d'une machine chaude ou d'une étincelle électrique.
- **Courants violents** : pendant les grandes marées, ils poussent le joueur, le bathyscaphe et les entités.
- **Glissements de terrain** : sur les pentes des fosses, de rares avalanches de sédiments ensevelissent les installations. Un signe annonciateur (tremblements) prévient le joueur, et on s'en protège avec des fondations ou des ancrages.
- Chaque danger est désactivable individuellement en config.

### 8.3 Structures
- **Épaves industrielles** : anciennes plateformes ou sous-marins coulés, avec du loot (matériaux, composants, ressources rares). **Pas de plans**, puisque tout est craftable.
- **Stations abandonnées** : habitats en ruine à réparer et réutiliser comme base avancée.
- **Champs de cheminées** : zones rares regroupant plusieurs cheminées, emplacement idéal de la mégacentrale.

---

## 9. Progression (20 à 30 heures)

| Étape | Objectif | Débloque |
|---|---|---|
| 1. Littoral | Première hydrolienne dans une baie, flotteurs à houle | Alternateur, câble BT, petite batterie |
| 2. Réseau | Ramener l'énergie à la base, premiers transformateurs | Câble HT, disjoncteurs, sonar fixe |
| 3. Plateau | Acier riveté, grande hydrolienne, barrage marémoteur | Scaphandre 2, bathyscaphe, cartographie |
| 4. Grands fonds | Collecte de nodules, alliage abyssal, ETM | Habitats pressurisés, ROV, batteries moyennes |
| 5. Cheminées | Premier générateur hydrothermal, gestion des cycles | Foreuse à sulfures, câble THT, navire câblier |
| 6. Fosses | Coque hadale, exploration des fosses, hydrates | Carburants, grandes batteries |
| 7. Mégacentrale | Multibloc géant sur un champ de cheminées | Fin de progression |

- **Tout est craftable** : la progression est limitée par les ressources, la profondeur atteignable et la complexité des usines.
- **Recettes mixtes** : blocs simples (câbles, balises, structures) en table de craft ; composants avancés (bobinages, alliages, coques) via les chaînes Create (assemblage séquencé, presse, mixeur, déployeur, remplissage).

---

## 10. Interface, guide et ambiance

- **Ponder** : une scène animée pour chaque machine et mécanique (pression, réseau, cycles, marées).
- **Carnet d'ingénieur** : un livre qui guide la progression par chapitres (étapes du §9), avec des astuces.
- **Goggles d'ingénieur** (outil Create) : elles affichent la profondeur, l'intégrité, l'encrassement, la production et l'état du réseau.
- **Ambiance** : bulles des turbines, fumées des cheminées, sédiments soulevés ; hublots éclairés, projecteurs, lueur des câbles haute tension ; sons étouffés sous l'eau, grondements, chants lointains ; grincements et craquements de pression.
- **Style** : « Create marin ». Laiton, cuivre et rivets, avec patine verte, hublots, peinture anti-corrosion (rouge ou jaune). La taille des machines grandit avec la progression.

---

## 10 ter. Expérience joueur

**Prise en main (V1)**
- À la première entrée dans une eau profonde, le joueur reçoit le **carnet d'ingénieur**, avec un premier objectif clair : construire une hydrolienne. Ce don se fait une seule fois par joueur et peut être désactivé en config.

**Indicateur de profondeur (V1)**
- Un **profondimètre discret** n'apparaît à l'écran que sous l'eau. Il affiche la profondeur et le palier, et passe à l'orange puis au rouge à l'approche de la limite du scaphandre ou du véhicule. Sa position est réglable dans la config client, et il peut être masqué.

**Pose des câbles (V1)**
- **Aperçu fantôme** du tracé avant validation, avec longueur et coût en matériau.
- **Code couleur par tension** (BT, HT, THT), avec un mode compatible daltoniens (motifs en plus des couleurs).
- **Rembobinage** : retirer un câble rend 100 % du matériau.
- **Alerte de portée** : un message clair si la distance dépasse ce que la tension peut transporter efficacement, avec la suggestion d'ajouter un transformateur.

**Perte du bathyscaphe (V2)**
- S'il est détruit, il laisse une **épave récupérable** contenant son inventaire, signalée par une **balise de détresse** visible sur le profondimètre et les cartes.

**Alertes à distance (V2)**
- Une **notification discrète** (par exemple « Centrale Nord : disjoncteur déclenché ») est envoyée aux joueurs qui portent ou possèdent un équipement de supervision. Elle est désactivable par joueur.

**Nommage (V1)**
- Les machines et les réseaux peuvent être nommés. Les noms apparaissent dans les goggles, les alertes, les cartes et la salle de contrôle.

**Multijoueur (V1)**
- Chaque réseau a un **propriétaire**, qui peut autoriser d'autres joueurs ou une équipe à le modifier. Ce système se combine avec le respect des mods de protection (claims).

**Performance (V1)**
- Réglage des particules : complet, réduit ou désactivé.
- Distance de rendu des câbles réglable.
- Modèles simplifiés à distance pour les gros multiblocs.
- Compatibilité testée avec les shaders populaires (éclairage, rendu de l'eau, texture émissive des câbles).

**Compatibilité JourneyMap (V1, dépendance optionnelle)**
- Intégration via l'API officielle de JourneyMap ; le mod fonctionne normalement sans JourneyMap.
- **Marqueurs automatiques** : cheminées hydrothermales et gisements repérés par le sonar, balises lumineuses, bouées et phares, machines nommées.
- **Tracés** : les câbles sous-marins s'affichent sur la carte, colorés selon leur tension.
- **Calques activables** : courants marins (repérés par le sonar), zones de profondeur par palier.
- **V2** : balise de détresse de l'épave du bathyscaphe, machines en panne signalées sur la carte.
- Chaque type de marqueur peut être activé ou désactivé dans la config client.
- **Découverte** : une cheminée ou un gisement n'apparaît sur la carte qu'**après un scan sonar**. Le sonar garde ainsi tout son intérêt d'exploration.
- **Visibilité en multijoueur** : les découvertes et les réseaux sont visibles par leur propriétaire et les membres autorisés du réseau. Une option serveur permet de tout rendre public.
- **Bonus** :
  - **Waypoint de chantier** : un clic sur une cheminée de la carte crée un waypoint vers elle.
  - **Calque marées en direct** : les courants sont coloriés selon la marée en cours.
  - **Infobulles détaillées** : survoler une machine affiche sa production, son intégrité et son état.
  - **Export des cartes bathymétriques** : les cartes imprimées en jeu (V2) ajoutent leurs données à JourneyMap.

**Autres cartes (dépendances optionnelles)**
- **Xaero's Minimap / World Map (V2)** : son API est plus limitée, donc l'intégration porte sur des waypoints automatiques (cheminées, balises, bouées, détresse) ; pas de tracé de câbles si l'API ne le permet pas.
- **FTB Chunks (V2)** : marqueurs et, si possible, intégration avec ses claims pour la protection des réseaux.
- Une **couche d'abstraction commune** sert toutes les cartes (une seule source de marqueurs, plusieurs adaptateurs), pour éviter de dupliquer la logique.

## 10 quater. Support des créateurs de modpacks (V1)

Objectif : les gros modpacks n'intègrent un mod que s'ils peuvent l'adapter à leur progression.
- **Recettes** : toutes les recettes, y compris les recettes de traitement Create et les types de recettes propres au mod, sont définies en JSON (datapack) et **compatibles KubeJS**. Les types de recettes propres au mod doivent être exposés proprement à KubeJS.
- **Valeurs par machine** : la config contient les réglages globaux, et un **datapack** permet d'ajuster chaque machine (production, profondeur max, vitesse d'encrassement, capacité). Le datapack a priorité sur la config pour la machine concernée.
- **Génération du monde par tags de biomes** : les biomes où apparaissent les cheminées, les fosses, les gisements et les structures sont définis par des **tags de biomes** modifiables. Par défaut, ils couvrent les océans vanilla et les tags communs d'océans (`c:is_ocean`, `c:is_deep_ocean`), pour fonctionner aussi avec les océans d'autres mods.
- **Tags standards** : tous les items et blocs du mod utilisent les tags communs quand ils existent (lingots, plaques, minerais).
- **Documentation** : une page wiki (GitHub ou wiki du mod) qui liste les identifiants, tags, types de recettes et paramètres de datapack, avec des exemples KubeJS.

## 10 bis. Contenus additionnels

- **Bouées et phares (V1)** : une bouée posée en surface au-dessus d'un câble le **balise**, et un câble balisé ne peut plus être arraché par les ancres et les bateaux. Les phares éclairent la nuit, servent de repères et apparaissent sur les cartes bathymétriques. Le dommage « ancres et bateaux » passe donc en V1 pour que les bouées aient un rôle dès la sortie.
- **Arbre de progrès (V1, enrichi à chaque version)** : un onglet de succès Minecraft qui suit les 7 étapes du §9 (« Premier courant », « Sous pression », « Mégacentrale »…), avec un compteur d'énergie totale produite par joueur. Il guide sans contraindre.
- **Salle de contrôle (V2)** : des blocs de supervision compatibles avec les Display Links de Create, qui affichent la production par centrale, la marée en cours, l'état des cheminées et des alertes (intégrité basse, disjoncteur déclenché, surcharge).
- **Électrolyse et hydrogène (V2)** : un électrolyseur transforme l'eau en hydrogène avec le surplus d'électricité. L'hydrogène est stocké en réservoir et sert de carburant aux chaudières Create. C'est un stockage complémentaire aux batteries.
- **Concrétions récupérables (V2, avec l'entretien)** : nettoyer une machine encrassée rapporte des coquillages, du corail et du calcaire (déco et recettes). Le butin est proportionnel au niveau d'encrassement.
- **Météo marine (V2, avec la houle)** : pendant un orage, la houle est plus forte et les flotteurs produisent plus, mais ceux qui sont mal ancrés peuvent être arrachés (risque contre gain).
- **Désalinisation (V3)** : une usine électrique produit de l'eau douce et du sel à partir de l'eau de mer.
- **Pipelines sous-marins (V3)** : le transport de fluides (hydrogène, méthane, eau chaude des cheminées) jusqu'à la côte, avec la même logique de pose et de dangers que les câbles.

## 11. Configuration (exhaustive à compléter)
Toutes les valeurs d'équilibrage sont dans un fichier de config **serveur** (synchronisé avec le client si besoin), et les effets visuels dans un fichier **client**.
- Pression : seuils des paliers, vitesse de dégradation, casse on/off, pression joueur on/off.
- Obscurité : vanilla / croissant / noir total.
- Entretien : on/off, vitesse d'encrassement.
- Marées : amplitude, influence lunaire.
- Cheminées : durées et multiplicateurs de chaque état.
- Réseau : pertes, portée de pose manuelle, intensités max.
- Dangers : activation individuelle et fréquence.
- Ressources : vitesse de régénération.
- Génération du monde : fréquence des cheminées, fosses, structures.

---

## 12. Feuille de route

### Outils admin et créatifs (développés dès le début, utiles aux tests)
- **Commandes de debug** : `/deepcurrent tide <valeur>` (forcer la marée), `/deepcurrent vent <état>` (forcer l'état d'une cheminée proche, dont l'éruption), `/deepcurrent grid info` (état du réseau visé : production, consommation, pertes, tension), `/deepcurrent pressure` (profondeur et palier à la position du joueur). Réservées aux opérateurs.
- **Blocs créatifs** : source d'électricité infinie (réglable) et consommateur test (charge réglable), non craftables.

### Alpha publique — « Premier courant »
Objectif : publier tôt pour créer une communauté et recueillir des retours.
- Petite hydrolienne, alternateur, moteur électrique, câble BT en pose manuelle, petite batterie.
- Marées liées à la lune, palier de pression 1, dégradation progressive.
- Ponder de base, JEI/EMI, EN + FR, sur les deux versions de Minecraft.
- Bien indiquer « alpha » : les mondes de test peuvent devoir être recréés.
- Juste avant la publication : serveur Discord, bot et modèles GitHub prêts (§13 bis).

### V1 — « Premiers courants » (objectif : sortie publique)
- Hydrolienne (petite + grande), générateur hydrothermal, cheminées en génération de monde avec leurs cycles.
- Fosses abyssales en génération de monde.
- Alternateur, moteur électrique, câbles BT/HT (pose manuelle), transformateurs, disjoncteurs, petite et moyenne batterie.
- Paliers de pression 1 à 3, dégradation progressive.
- Nodules + sulfures, collecteur et foreuse.
- Sonar fixe, balises lumineuses.
- Marées liées à la lune.
- Bouées et phares, dommages aux câbles par les ancres et bateaux.
- Arbre de progrès (étapes disponibles en V1).
- Support des créateurs de modpacks (§10 quater) : recettes datapack et KubeJS, valeurs par machine en datapack, génération par tags de biomes, page wiki.
- Expérience joueur (§10 ter) : carnet offert et premier objectif, profondimètre, aides à la pose des câbles, nommage, propriété des réseaux, options de performance.
- Ponder pour tout le contenu, JEI/EMI, EN + FR.
- 1.20.1 Forge et 1.21.1 NeoForge.

### V2 — « Grands fonds »
- Marémotrice/houle, ETM.
- Entretien et corrosion, pression sur le joueur et scaphandres, obscurité croissante.
- Bathyscaphe, habitats pressurisés, cartographie des fonds.
- Hydrates de méthane et fuites, éruptions dangereuses, courants violents.
- Épaves industrielles, carnet d'ingénieur, câble THT.
- Salle de contrôle, électrolyse et hydrogène, concrétions récupérables, météo marine.
- Épave récupérable du bathyscaphe, alertes à distance.
- Compatibilité Xaero's Minimap / World Map et FTB Chunks.

### V3 — « Abysses »
- Fosses abyssales et palier 4, champs de cheminées, mégacentrale.
- ROV, navire câblier, sonar portatif.
- Faune et menaces, glissements de terrain, stations abandonnées, organismes bioluminescents.
- Compatibilités : véhicules Create, CC: Tweaked, mods électriques Create, convertisseur FE.
- Désalinisation, pipelines sous-marins.

---

## 13. Points techniques à préciser avec Claude Code
- Architecture multi-version (1.20.1 Forge / 1.21.1 NeoForge) : la recommandation est de garder le **code de logique indépendant du loader**, isolé de la couche plateforme, puis de choisir entre deux branches git et un outil multi-version. Le choix est à faire par Claude Code après étude.
- Version exacte de Create 6.x à cibler pour chaque version de Minecraft.
- Stockage de l'intégrité et de l'encrassement (données de block entity) et synchronisation client.
- Algorithme du réseau électrique (graphe, recalcul sur changement uniquement).
- Calcul des courants par zone (bruit déterministe calculé à partir de la seed).
- Liste des mods de véhicules Create et de mods électriques à supporter en V3 (à vérifier au moment du développement).
- Identifiant du mod : `deepcurrent`. Package Java : `fr.talfa.deepcurrent`. Auteur : **TALFA**.

## 13 bis. Communauté (à préparer juste avant l'alpha)

### Serveur Discord
Ouvert au moment de la publication de l'alpha, avec la première vidéo.

**Accueil** : le nouveau membre ne voit que `#bienvenue` et `#règles`. Il accepte les règles et choisit sa langue (FR ou EN) pour débloquer le serveur. Cette étape bloque aussi la plupart des bots de spam.

**Structure**
- **Commun** (visible par tous) : `#bienvenue`, `#règles`, `#annonces` (lecture seule, bilingue).
- **Catégorie FR** et **catégorie EN** (chacune visible seulement par sa langue) : `#général`, `#hors-sujet`, `#faq`, `#dev-log`, `#screenshots`, `#vos-centrales` (forum : un fil par centrale de joueur), `#support`, `#bugs`, `#suggestions`, `#modpacks`.
- **Privé** : `#soutiens` (avant-premières), `#créateurs-de-contenu` (accès anticipé), `#créateurs-de-modpacks`, `#modération` (journal du bot, discussions de l'équipe).

**Rôles** : Auteur, Modérateur, Soutien (Ko-fi / Patreon), Créateur de modpack, Créateur de contenu, Testeur, Traducteur, FR, EN, Mises à jour (rôle optionnel pour être notifié des nouvelles versions), Sessions de test (rôle optionnel pour être prévenu des sessions de test).

**Salons supplémentaires** : `#autopromo` (commun), `#suggestions` sous forme de forum avec tags, salon vocal verrouillé « Téléchargements : N » (compteur public).

### Bot Discord (dépôt séparé, réalisé par Claude Code)
**Stack** : TypeScript + discord.js, commandes slash. Hébergement sur un hébergeur gratuit ou peu cher, avec un guide de déploiement documenté. Secrets (tokens Discord, GitHub, Ko-fi/Patreon) en variables d'environnement uniquement.

1. **Mise en place du serveur** : une commande d'administration crée ou met à jour d'un coup les catégories, salons, rôles, permissions et le parcours d'accueil décrits ci-dessus. Elle est relançable sans doublons ; la structure est décrite dans un fichier de configuration.
2. **Accueil** : boutons d'acceptation des règles et de choix de langue, qui attribuent les rôles.
3. **Annonces de versions** : déclenchées par la publication (GitHub Actions ou webhook) :
   - publication **bilingue** dans `#annonces` : version, versions de Minecraft supportées, changelog, liens CurseForge et Modrinth, **image** de la nouveauté phare ;
   - **ping du rôle Mises à jour** uniquement ;
   - **avant-première de 24 h** dans `#soutiens` (annonce et téléchargement), puis publication publique automatique.
4. **Dev-log semi-automatique** : chaque semaine, le bot prépare un résumé des avancées à partir de GitHub (commits, issues fermées). Il l'envoie en privé à l'auteur pour validation et modification, puis le publie dans `#dev-log` FR et EN.
5. **Rapports de bugs → GitHub** : dans `#bugs`, un bouton ouvre un **formulaire guidé** (versions, mods, étapes, logs). Le rapport est mis en attente ; l'Auteur ou un Modérateur le **valide** (ou le marque comme doublon), et le bot crée alors l'issue GitHub pré-remplie avec un lien vers le message d'origine. Le joueur est notifié quand l'issue est fermée.
6. **Analyse des crashs** : quand un rapport de crash ou un `latest.log` est posté (fichier ou lien), le bot identifie le mod en cause et compare l'erreur à une **base de problèmes connus** (fichier versionné dans le dépôt du bot, enrichi au fil du temps) avec leur solution. Il propose ensuite d'ouvrir un rapport de bug.
7. **Commandes joueurs** :
   - `/wiki <sujet>` : lien vers la page du wiki correspondante (avec autocomplétion) ;
   - `/faq <question>` : réponses prêtes, en FR ou EN selon le rôle de langue, utilisables aussi par les modérateurs pour répondre à quelqu'un ;
   - `/version` : dernière version du mod pour chaque version de Minecraft, avec liens ;
   - `/compat <mod>` : compatible, problème connu ou non testé (liste maintenue dans un fichier).
8. **Modération** :
   - anti-spam et anti-liens (arnaques Nitro, liens suspects), avec suppression et journal dans `#modération` ;
   - anti-raid : détection d'une vague d'arrivées suspectes, verrouillage temporaire du serveur et alerte à l'équipe ;
   - `/warn` avec historique par membre, et sanctions graduées configurables.
9. **Rôle Soutien automatique** : liaison avec Ko-fi et Patreon (webhooks), qui attribue et retire le rôle selon le soutien actif.

**GitHub** :
- Modèles d'issues (formulaires) pour les bugs (version du mod, de Minecraft et du loader, de Create, liste des mods, étapes pour reproduire, logs) et pour les suggestions.
- Les issues sont la source unique du suivi des bugs ; Claude Code les traite directement.

### Règles du serveur
1. Respect : pas d'insultes, de harcèlement ni de discrimination.
2. Pas de publicité non sollicitée. Seule exception : `#autopromo` (voir ci-dessous).
3. Interdiction de partager le mod en dehors de CurseForge et Modrinth (pas de reupload), conformément à la licence.
4. Les bugs passent par le formulaire de `#bugs`, pas par messages privés à l'auteur.

### Animation
- **Sondages de fonctionnalités** : les membres votent pour la prochaine fonctionnalité parmi plusieurs choix proposés par l'auteur. Le vote des Soutiens compte double.
- **Sessions de test** : soirées de test en groupe avant une sortie, sur le serveur Minecraft de test.
- **Pas de système de niveaux ou d'XP** (il pousse au spam).
- **Suggestions en forum** : chaque suggestion est un fil avec votes par réaction. L'auteur ajoute un tag « acceptée », « refusée », « plus tard » ou « en cours ».
- **`#autopromo`** : un membre peut promouvoir son serveur, son modpack ou sa chaîne **uniquement s'il utilise ou présente Create: Deep Current**, une fois par semaine maximum (limite appliquée par le bot).

### Serveur Minecraft de test
- Pas de serveur public permanent. Un serveur est ouvert pour les **sessions de test** et pour les membres Testeur, avec la dernière version de développement. Le bot annonce son ouverture et sa fermeture.

### Soutiens (Ko-fi / Patreon)
- **Un palier unique** (environ 3 à 5 € par mois), ainsi que des dons ponctuels possibles sur Ko-fi.
- **Avantages** : rôle Soutien et `#soutiens`, avant-première de 24 h, **vote prioritaire** (compte double), **coulisses** (captures et vidéos de développement en avant-première), **nom dans les crédits** (carnet d'ingénieur et page du mod), **cosmétique en jeu** exclusif (par exemple une plaque nominative décorative).
- **[À VALIDER]** Le cosmétique en jeu doit être vérifié avec les règles d'utilisation de Minecraft avant d'être développé : il ne doit donner aucun avantage de jeu, et réserver un contenu du mod à des payants pourrait être interdit. Solution de repli : un cosmétique visible uniquement sur Discord ou sur la page du mod.
- **Le mod reste toujours entièrement gratuit** : aucune fonctionnalité de jeu n'est réservée aux soutiens.

### Créateurs de contenu
- **Accès anticipé** aux versions dans `#créateurs-de-contenu`.
- **Kit presse** : logo, captures en haute résolution, résumé du mod et des nouveautés de chaque version.
- **Mondes de démo** : mondes prêts à filmer avec des centrales déjà construites, mis à jour à chaque version majeure.
- **Mise en avant** : seules les **vidéos de présentation du mod** sont relayées dans `#annonces` et sur la page du mod.

### Traductions
- Les traductions communautaires se font sur **Crowdin**, synchronisé avec le dépôt GitHub.
- Le bot attribue le rôle Traducteur aux contributeurs et annonce les langues complétées.

### FAQ et wiki
- **FAQ** : un **fichier source unique** (dans le dépôt) alimente à la fois `#faq` (FR et EN), la commande `/faq` et la page FAQ du wiki. Une modification se répercute partout.
- **Wiki** : écrit en Markdown dans le dépôt du mod et publié automatiquement en site web gratuit (GitHub Pages). Claude Code le maintient à jour à chaque version. Il contient la documentation des joueurs et la documentation des créateurs de modpacks (§10 quater).

### Technique du bot
- **Stockage** : SQLite (avertissements, rapports en attente, liaisons des soutiens, limites d'autopromo, statistiques).
- **Déploiement** : image Docker, mise à jour automatique à chaque push sur la branche principale (GitHub Actions), redémarrage automatique en cas de crash.
- **Pas de tableau de bord web** : toute l'administration passe par des commandes Discord réservées à l'équipe et par des fichiers de configuration versionnés (FAQ, problèmes connus, compatibilités, structure du serveur).
- **Statistiques** :
  - rapport hebdomadaire privé à l'auteur : téléchargements CurseForge et Modrinth, croissance du Discord (arrivées, départs, membres actifs), bugs (ouverts, fermés, crashs les plus fréquents) ;
  - **compteur public** : un salon vocal verrouillé affiche le total de téléchargements, mis à jour régulièrement.

### Identité du bot : Bubbles
- **Mascotte** : Bubbles, un petit robot de plongée. Son avatar et ses illustrations sont réalisés par Claude Code, dans le style « Create marin » du mod.
- **Ton** : chaleureux, avec de petites touches marines (« Une nouvelle version vient de remonter des profondeurs ! »), sans excès d'humour ni d'emojis. Les messages d'erreur et de modération restent sobres et clairs.
- Tous les textes du bot sont en FR et EN, dans des fichiers de traduction (pas en dur dans le code).

### Bienvenue
- Message dans `#bienvenue` **et** message privé à l'arrivée (le MP échoue silencieusement si le membre les a désactivés).
- Contenu : une phrase de présentation du concept, les **liens essentiels** (téléchargement CurseForge et Modrinth, wiki, FAQ, formulaire de bug), des **boutons de notifications** (rôle Mises à jour, rôle Sessions de test).

### Sondages de fonctionnalités
- **Un par mois**, 3 ou 4 fonctionnalités proposées par l'auteur, **vote ouvert 7 jours**. Le vote des Soutiens compte double.
- Le bot annonce le sondage (ping du rôle Mises à jour), envoie un rappel 24 h avant la fin et publie le résultat dans `#annonces`.

### Support
- **Tickets privés** : un bouton dans `#support` ouvre un salon privé entre le joueur et l'équipe. À la fermeture, une transcription est archivée dans `#modération` puis le salon est supprimé.
- **Suggestion automatique discrète** : quand un message dans `#support` ressemble à une entrée de la FAQ ou des problèmes connus (recherche par mots-clés, sans IA), le bot répond en fil avec la réponse probable et un bouton « Ça a résolu mon problème ». Un humain peut toujours compléter.
- **Formulaire de bug** :
  - versions du mod, de Minecraft, du loader et de Create (listes déroulantes alimentées par les versions publiées) ;
  - contexte : solo, serveur ou les deux ;
  - description et étapes pour reproduire ;
  - lien vers les logs (mclo.gs ou fichier joint), **analysé automatiquement** par le module de crashs ;
  - captures ou vidéo (facultatives).

### Modération
- **Au lancement, pas de modérateur** : l'auteur gère seul, aidé par le bot. Le rôle Modérateur existe déjà pour plus tard (attribué sur invitation quand le serveur grandit).
- **Échelle de sanctions progressive** (appliquée automatiquement par `/warn`) :
  1. 1er avertissement : rappel des règles en MP ;
  2. 2e : mute 1 h ;
  3. 3e : mute 24 h ;
  4. 4e : exclusion temporaire (7 jours) ;
  5. 5e : ban.
  L'équipe peut sauter des étapes en cas de faute grave (`/sanction` directe), avec une raison obligatoire.
- **Expiration** : un avertissement expire après **90 jours** pour le calcul de l'échelle. L'historique reste visible par l'équipe.
- **Appel** : un membre sanctionné peut ouvrir un **ticket d'appel**. Dès qu'il y a des modérateurs, l'appel est traité par une autre personne que celle qui a sanctionné ; tant que l'auteur est seul, il le traite lui-même.
- Toutes les actions de modération (automatiques ou manuelles) sont journalisées dans `#modération`.

### Données personnelles (RGPD)
- **Minimum de données** : uniquement l'identifiant Discord et les données nécessaires (sanctions, tickets, liaison du soutien). Jamais d'email ni de contenu de message au-delà de ce qu'exige un ticket ou une sanction.
- **Politique de confidentialité** : une page courte sur le wiki (données, finalités, durées de conservation), liée dans `#règles`.
- **`/mes-données`** : chaque membre peut consulter ses données et demander leur suppression. Les données de sanctions actives et de bans sont conservées (intérêt légitime), ce qui est indiqué dans la politique.
- **Départ du serveur** : les données d'un membre sont supprimées automatiquement **6 mois** après son départ, sauf en cas de ban.

### Fiabilité
- **Sauvegardes** : copie quotidienne automatique de la base SQLite **hors de l'hébergeur**, conservée 30 jours, avec une procédure de restauration documentée et testée.
- **Surveillance** : un service gratuit vérifie que le bot répond. En cas de panne, l'auteur reçoit une alerte en MP (ou par email si Discord lui-même est inaccessible), et une **page de statut publique** est liée dans `#faq`.
- **Environnement de test** : un second bot sur un Discord privé de test. Chaque nouvelle fonction y est déployée et testée avant la production. Les tests automatisés (unitaires) couvrent la logique : échelle de sanctions, analyse des crashs, recherche FAQ.

### Fonctions supplémentaires du bot
**Outils joueurs**
- `/recette <objet>` : affiche la recette d'un objet du mod (y compris les étapes Create). Les données sont **générées automatiquement depuis les fichiers du mod** à chaque version (export JSON lors du build), donc jamais de recette obsolète.
- `/calcul` : un calculateur avec deux modes :
  - **production** : machine, coefficient de marée ou état de cheminée, profondeur → production estimée et palier de coque requis ;
  - **câble** : puissance et distance → tension et câble recommandés, pertes estimées.
  Les formules et valeurs viennent du même export que les recettes, pour rester identiques au jeu (valeurs par défaut de la config).
- `/maree` : explique le cycle des marées et indique les moments de production maximale selon la phase lunaire.
- `/bug-corrige` : un joueur s'abonne à une issue GitHub ; il reçoit un MP quand une version publiée contient le correctif (détecté via le lien entre l'issue fermée et la release).

**Engagement**
- **Galerie des centrales** : l'auteur marque une création de `#vos-centrales` (réaction ou commande dédiée) ; le bot l'ajoute, avec l'accord du joueur, à une **galerie sur le wiki** (image, nom du joueur, description).
- **Anniversaire du mod** : chaque année, un message spécial avec les statistiques de l'année (téléchargements, membres, centrales partagées, versions publiées).

**Intégrations**
- **Événements Discord** : chaque session de test est créée comme événement Discord programmé, avec rappels automatiques (24 h et 1 h avant) au rôle Sessions de test.
- **Vidéos YouTube** : les nouvelles vidéos de présentation de la chaîne officielle sont annoncées automatiquement dans `#annonces` (FR et EN).

**Traduction**
- **Traduction automatique sur `#bugs`** : chaque rapport de bug est traduit automatiquement dans l'autre langue (FR ↔ EN), affiché sous le rapport, pour que toute l'équipe et la communauté le comprennent. Elle utilise une API de traduction avec un palier gratuit (clé en variable d'environnement), avec un quota surveillé et un repli silencieux si le quota est atteint.

## 14. Monétisation (rappel)
- Le mod est gratuit (règles de Mojang). Publication sur **CurseForge** et **Modrinth**, qui reversent une part des revenus aux créateurs.
- Ko-fi ou Patreon (soutien, avant-premières de screenshots, vote sur les prochaines fonctionnalités).
- Vidéos de présentation (centrale illuminée au fond, éruption, mégacentrale) pour la visibilité.
- La licence « tous droits réservés » autorise l'usage dans les modpacks, mais interdit la republication et les forks sans accord.
