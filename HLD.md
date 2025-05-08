# 1. Project / Folder Structure
```
project-root/
├── android/                  # Android launcher and settings
├── desktop/                  # Desktop launcher
├── html/                     # (Optional) HTML5/WebGL launcher
├── core/                     # All game logic, assets, and data definitions
│   ├── assets/               # JSON, Tiled maps, sprites, UI skins, audio
│   ├── data/                 # Definitions for classes, items, races, events
│   │   ├── classes/          # Tier 1–3 class definitions (JSON)
│   │   ├── items/            # Item/item affix definitions
│   │   ├── races/            # Race traits & modifiers
│   │   └── events/           # Dialog trees & random-event scripts
│   ├── graphics/             # Texture atlases, shaders, icons
│   ├── ui/                   # Scene2D skins, styles, layout XMLs
│   ├── com/yourgame/         # Root package for all logic
│   │   ├── game/             # Main game bootstrap & screens
│   │   ├── map/              # Overworld map & tile/node logic
│   │   ├── party/            # PartyManager, Character, Race, Class trees
│   │   ├── combat/           # CombatManager, BattleGrid, AI, Skills
│   │   ├── resources/        # ResourceManager, ResourceTypes
│   │   ├── dialog/           # DialogManager, DialogParser, EventTriggers
│   │   ├── rest/             # RestSystem, CampEvents, DialogHooks
│   │   ├── ui/               # Screens (MapScreen, CombatScreen, UIScreens)
│   │   └── util/             # Helpers (MathUtils, DataLoader, Logging)
└── build.gradle / pom.xml    # Build scripts
```

# 2. Core Packages & Classes

Below is the order of priority for building out modules and key classes. Each “phase” builds on the previous.

### Phase 1: Foundation & Boot

*   **GameBootstrap**
    *   Extends `Game` (LibGDX). Loads config, assets, skins.
*   **ScreenManager**
    *   Manages transitions between `MapScreen`, `CombatScreen`, `DialogScreen`, `RestScreen`.
*   **AssetManager**
    *   Wraps LibGDX’s `AssetManager` for loading JSON, atlases, audio.

### Phase 2: Map & Navigation

*   **OverworldMap** (`com.yourgame.map`)
    *   Holds `Tile[][]` or Node graph.
    *   Loads from Tiled JSON or custom map definition.
*   **Tile / MapNode**
    *   Properties: `terrainType`, `regionType`, `eventTriggers`, `resourceModifiers`.
*   **MapScreen** (`com.yourgame.ui`)
    *   Renders map, handles camera pan/zoom, click to select next node.
*   **RouteCalculator**
    *   Finds paths, accounts for terrain cost, collapsed bridges, dynamic hazards.

### Phase 3: Resources & Party

*   **ResourceManager** (`com.yourgame.resources`)
    *   Tracks supplies, firewood, gold, morale, time.
    *   API: `spend(ResourceType, amount)`, `gain(...)`, event listeners.
*   **Character & PartyManager** (`com.yourgame.party`)
    *   **Character**: `race`, `classTree`, `level`, `skills`, `perks`, `stats`.
    *   **PartyManager**: list of active members, add/remove, apply global buffs.
*   **ClassTree, Skill, Perk**
    *   Data classes loaded from JSON (`core/data/classes/*.json`).
*   **Race, Trait**
    *   Simple data + modifiers to stats or exploration rolls.

### Phase 4: Dialog & Events

*   **DialogParser** (`com.yourgame.dialog`)
    *   Reads JSON or Ink-style markup, constructs node tree.
*   **DialogManager**
    *   Presents choices, applies outcome effects (resources, morale, unlocks).
*   **EventTrigger**
    *   Attaches to `MapNode` or to `RestSystem`; decides when to fire a random event.

### Phase 5: Combat System

*   **BattleGrid** (`com.yourgame.combat`)
    *   2D grid representation, tile occupancy, range calculations.
*   **Combatant** (extends `Character`)
    *   Holds HP, statuses, `turnOrder` weight, skills ready to use.
*   **CombatManager**
    *   Orchestrates turn loop, skill resolution, victory/defeat.
*   **AIController**
    *   Basic decision tree for enemies (target selection, skill use).
*   **CombatScreen**
    *   Renders grid, highlights moves, handles input for skill/target.

### Phase 6: Rest & Camp

*   **RestSystem** (`com.yourgame.rest`)
    *   Called when party chooses to camp: heals, consumes resources, triggers dialogues or ambushes.
*   **CampEvent**
    *   Special sub-class of `EventTrigger` for rest-only scenarios.
*   **RestScreen**
    *   UI for choosing rest duration, invoking Inter-Character Dialogs, allocating medical supplies.

### Phase 7: UI Polish & Tools

*   Scene2D.UI screens for inventory, party roster, skill trees, map legend.
*   **Debug Console** (`com.yourgame.util.DebugConsole`) to spawn events, give XP, toggle fog.

# 3. Implementation Roadmap

| Sprint | Goals                                                 |
| :----- | :---------------------------------------------------- |
| 1      | Project setup, bootstrap, asset loading, empty screens  |
| 2      | Map data loading, MapScreen, basic movement & fog     |
| 3      | ResourceManager + UI panel, party load from data      |
| 4      | DialogParser + sample event on a map node             |
| 5      | Basic combat loop: grid rendering + dummy characters    |
| 6      | Skills framework + AIController                       |
| 7      | RestSystem + camp events + rest UI                    |
| 8      | ClassTree progression UI + skill/perk unlocks         |
| 9      | Data-driven events: random event tables, map hazards  |
| 10     | Polish: transitions, audio, particle FX, tooltips     |

# 4. Data Formats & Assets

*   JSON Schemas for classes, skills, perks, races, items, events
*   TiledMap (TMX + JSON) for overworld layout
*   Texture Atlases for character portraits, map icons, combat sprites
*   Scene2D Skins (JSON + atlas) for UI theme
