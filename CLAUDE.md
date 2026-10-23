# CLAUDE.md — Create: Deep Current

## Contexte
Addon Minecraft pour **Create** : énergie marine, électricité transportée par câbles sous-marins, pression des grands fonds.
La spécification complète est dans `DEEP_CURRENT_GDD.md`. **Lis-la entièrement avant toute tâche** ; c'est la source de vérité du design.

## Cibles
- Minecraft **1.20.1 / Forge** et **1.21.1 / NeoForge**.
- Dépendance : **Create 6.x** (version exacte à déterminer par cible).
- Identifiant du mod : `deepcurrent`. Package Java : `fr.talfa.deepcurrent`. Auteur : TALFA.
- Java 17 pour 1.20.1, Java 21 pour 1.21.1.

## Méthode de travail
1. Commence par proposer l'architecture du dépôt (stratégie multi-version, découpage en modules) et attends la validation avant de coder.
2. Travaille **jalon par jalon** en suivant la feuille de route V1 du GDD (§12). Un jalon = une fonctionnalité jouable et testée.
3. Avant chaque jalon, résume ce que tu vas faire. Après chaque jalon, liste ce qui a été fait, ce qu'il reste à faire et comment le tester en jeu.
4. Si une règle du GDD est ambiguë ou marquée **[À VALIDER]**, pose la question au lieu de deviner.
5. Ne pas ajouter de fonctionnalité absente du GDD sans la proposer d'abord.

## Conventions de code
- Code, identifiants, commentaires et commits en **anglais**. Traductions dans `en_us.json` et `fr_fr.json`.
- Séparer clairement :
  - la **logique** (pression, réseau électrique, marées, cycles des cheminées), indépendante du loader autant que possible ;
  - le **code client** (rendus, particules, sons, effets d'obscurité, interfaces) ;
  - le **code commun et serveur** (blocs, block entities, génération du monde, réseau de paquets) ;
  - la **config** (fichier serveur pour l'équilibrage, fichier client pour les effets visuels).
- Toutes les valeurs d'équilibrage passent par la config, jamais en dur.
- Utiliser les API de Create (kinetic block entities, Ponder, recettes de traitement) plutôt que de les réimplémenter.

## Communauté (juste avant l'alpha)
Quand l'alpha est presque prête, tu prépares l'outillage communautaire décrit dans le GDD (§13 bis) :
- le **bot Discord** en TypeScript + discord.js, dans un **dépôt séparé** du mod, avec toutes les fonctions du GDD (§13 bis) : mise en place du serveur, accueil, annonces, dev-log, rapports de bugs vers GitHub, analyse des crashs, commandes joueurs, modération, rôle Soutien. Développe-le par modules, en commençant par la mise en place du serveur et les annonces ;
- dans le build du mod, une tâche qui **exporte en JSON** les recettes et les valeurs d'équilibrage par défaut, utilisée par les commandes `/recette` et `/calcul` du bot ;
- les **modèles d'issues GitHub** (bug et suggestion) dans le dépôt du mod ;
- un guide de déploiement du bot sur un hébergeur gratuit ou peu cher.
Les tokens restent dans des variables d'environnement, jamais dans le code. Ne commence pas ce travail avant que l'auteur le demande.

## Performance (critique)
- Réseau électrique : modèle en **graphe**, recalculé uniquement quand un nœud change. Aucun parcours de réseau à chaque tick.
- Pression, encrassement et intégrité : mise à jour à intervalle (pas à chaque tick), stockés dans les block entities.
- Effets d'ambiance côté client uniquement, avec limite de particules.
- Tester avec `/spark` sur un monde avec un grand réseau.

## Qualité
- Chaque machine a sa scène Ponder et ses recettes visibles dans JEI/EMI.
- Tester sur les deux versions de Minecraft avant de valider un jalon.
## Modélisation et textures (à ta charge)
Tu réalises **tous les modèles 3D et toutes les textures** du mod.
- **Modèles** : fichiers JSON de modèles Minecraft (blocs et items), avec un fichier `.bbmodel` Blockbench source pour chaque modèle complexe afin que l'auteur puisse l'ouvrir et le retoucher. Modèles animés : utiliser les partial models et le rendu de Create (Flywheel) pour les pièces qui tournent.
- **Textures** : pixel art 16×16 (ou multiples de 16 pour les grandes pièces), générées par script (Python + Pillow). Palette cohérente avec Create : laiton, cuivre, rivets, avec la touche marine du GDD (patine verte, hublots, peinture anti-corrosion rouge ou jaune). Commence par définir une **palette de couleurs commune** dans un fichier, réutilisée par toutes les textures.
- **Variantes visuelles** à prévoir : encrassement (overlay de concrétions), états des cheminées (dormante, active, éruption), lueur des câbles haute tension (texture émissive).
- Le script de génération des textures reste dans le dépôt (`tools/textures/`) pour régénérer ou ajuster facilement.
- Après chaque lot de modèles, fournis des captures en jeu (ou un rendu) pour validation par l'auteur avant de passer au suivant.
