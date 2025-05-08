# 1. Project / Folder Structure
```
project-root/
├── android/                  # Android launcher and settings
├── desktop/                  # Desktop launcher
├── html/                     # (Optional) HTML5/WebGL launcher
├── core/                     # All game logic, assets, and data definitions
│   ├── assets/               # JSON, Tiled maps, sprites, UI skins, audio
│   │   ├── data/             # Definitions for classes, items, races, events (quests, skills, enemies implemented)
│   │   │   ├── classes/      # Tier 1–3 class definitions (JSON)
│   │   │   ├── items/        # Item/item affix definitions (Next up)
│   │   │   ├── races/        # Race traits & modifiers
│   │   │   └── events/       # Dialog trees & random-event scripts
│   │   ├── quests/           # Quest definitions (JSON) - Implemented
│   │   ├── skills/           # Skill definitions (JSON) - Implemented
│   │   └── enemies/          # Enemy definitions (JSON) - Implemented
│   ├── graphics/             # Texture atlases, shaders, icons
│   ├── ui/                   # Scene2D skins, styles, layout XMLs
│   ├── com/yourgame/         # Root package for all logic (com.danbramos.ringprototype)
│   │   ├── game/             # Main game bootstrap & screens (RingPrototypeGame.java) - Implemented
│   │   ├── map/              # Overworld map & tile/node logic (MapScreen.java) - Implemented
│   │   ├── party/            # PartyManager, Character, Race, Class trees - Implemented (PartyManager, Character, GameClass)
│   │   ├── combat/           # CombatManager, BattleGrid, AI, Skills (BattleScreen, BattleCharacter, Enemy, Skill, SkillType, IBattleActor) - Implemented
│   │   ├── resources/        # ResourceManager, ResourceTypes - Implemented
│   │   ├── dialog/           # DialogManager, DialogParser, EventTriggers (DialogScreen, QuestManager) - Implemented
│   │   ├── rest/             # RestSystem, CampEvents, DialogHooks
│   │   ├── ui/               # Screens (MapScreen, CombatScreen, UIScreens - BattleUiManager, MapUiManager) - Implemented
│   │   └── util/             # Helpers (MathUtils, DataLoader, Logging)
└── build.gradle / pom.xml    # Build scripts
```

# 2. Core Packages & Classes

Below is the order of priority for building out modules and key classes. Each "phase" builds on the previous.

### Phase 1: Foundation & Boot (Largely Done)

*   **RingPrototypeGame (was GameBootstrap)**
    *   Extends `Game` (LibGDX). Loads config, assets (basic), skins. Initializes core managers. - **Implemented**
*   **Screen Management** (Implicit via LibGDX `Game` and `setScreen()`)
    *   Manages transitions between `MapScreen`, `BattleScreen`, `DialogScreen`. - **Implemented**
*   **AssetManager (LibGDX)**
    *   Used for loading skins, textures; JSON loaded directly. - **Partially Implemented** (Opportunity to centralize all asset loading)

### Phase 2: Map & Navigation (Largely Done)

*   **OverworldMap** (Handled by `MapScreen` using TiledMap)
    *   Loads from Tiled JSON. - **Implemented**
*   **Tile / MapNode** (Implicit in TiledMap, extended by `MapInteraction.java` for quest givers)
    *   Properties: `terrainType` (from Tiled), `eventTriggers` (via `MapInteraction`). - **Partially Implemented**
*   **MapScreen** (`com.danbramos.ringprototype.screens`) - **Implemented**
    *   Renders Tiled map, handles camera, basic party movement via input handler.
*   **RouteCalculator**
    *   Finds paths, accounts for terrain cost, collapsed bridges, dynamic hazards. (Not yet implemented)

### Phase 3: Resources & Party (Largely Done)

*   **ResourceManager** (`com.danbramos.ringprototype.resources`) - **Implemented**
    *   Tracks supplies (Food, Firewood, Gold, Hope).
    *   API: `setResourceAmount`, `addResource`, `spendResource`.
*   **Character & PartyManager** (`com.danbramos.ringprototype.party`) - **Implemented**
    *   **Character**: `name`, `gameClass`, `level`, `healthPoints`, `manaPoints`, `skills`, `inventory` (basic), `battleSprite`, `movementRange`. - **Implemented**
    *   **PartyManager**: List of active members, add/remove. - **Implemented**
*   **Skill, ClassData** (`com.danbramos.ringprototype.battle.Skill`, `com.danbramos.ringprototype.party.ClassData`) - **Implemented**
    *   Skills loaded from JSON (`skills.json`, `skill-schema.json`).
    *   ClassData loads placeholder class info. (Perks, full ClassTree not yet implemented)
*   **Race, Trait** (Not yet implemented)
    *   Simple data + modifiers to stats or exploration rolls.

### Phase 4: Dialog & Events (Largely Done)

*   **Dialog/Quest System** (`com.danbramos.ringprototype.screens.DialogScreen`, `com.danbramos.ringprototype.quests.QuestManager`) - **Implemented**
    *   Reads Quest data from JSON (`quests.json`, `quest-schema.json`).
    *   `DialogScreen` presents choices.
    *   `QuestManager` tracks quest state and objectives (e.g., enemy kills).
*   **EventTrigger** (Partially via `MapInteraction` for quest givers, broader event system not yet)
    *   Attaches to `MapNode` or to `RestSystem`; decides when to fire a random event.

### Phase 5: Combat System (Largely Done)

*   **BattleGrid** (Handled by `BattleScreen` using TiledMap) - **Implemented**
    *   2D grid representation, tile occupancy.
*   **Combatant** (`com.danbramos.ringprototype.battle.BattleCharacter`, `com.danbramos.ringprototype.battle.Enemy` implementing `IBattleActor`) - **Implemented**
    *   Holds HP, battle map position, skills, movement. `StatusEffect` class created.
*   **Combat Logic** (Mostly in `BattleScreen`, `BattleInputHandler`, `BattleUiManager`) - **Implemented**
    *   Orchestrates turn loop, skill resolution (single target, AoE), damage calculation, movement.
*   **AIController** (Basic implementation in `Enemy.performSimpleAI()`) - **Implemented (Basic)**
    *   Basic decision tree for enemies (target selection, move, basic attack).
*   **CombatScreen** (`com.danbramos.ringprototype.screens.BattleScreen`) - **Implemented**
    *   Renders grid, actors, highlights moves/targets. Handles input for skill/target selection via popup menu.

### Phase 6: Rest & Camp

*   **RestSystem** (`com.yourgame.rest`)
    *   Called when party chooses to camp: heals, consumes resources, triggers dialogues or ambushes. (Not yet implemented)
*   **CampEvent**
    *   Special sub-class of `EventTrigger` for rest-only scenarios. (Not yet implemented)
*   **RestScreen**
    *   UI for choosing rest duration, invoking Inter-Character Dialogs, allocating medical supplies. (Not yet implemented)

### Phase 7: UI Polish & Tools

*   Scene2D.UI screens for inventory, party roster, skill trees, map legend. (Combat UI popup is a good step)
*   **Debug Console** (`com.yourgame.util.DebugConsole`) to spawn events, give XP, toggle fog. (Not yet implemented)

# 3. Implementation Roadmap

| Sprint | Goals                                                                 | Status                       |
| :----- | :-------------------------------------------------------------------- | :--------------------------- |
| 1      | Project setup, bootstrap, asset loading, empty screens                | ✅ Done                      |
| 2      | Map data loading, MapScreen, basic movement & fog                   | ✅ Done (Fog not implemented)  |
| 3      | ResourceManager + UI panel, party load from data                    | ✅ Done                      |
| 4      | DialogParser + sample event on a map node (Quest System from JSON)    | ✅ Done                      |
| 5      | Basic combat loop: grid rendering + characters/enemies                | ✅ Done                      |
| 6      | Skills framework (from JSON) + AIController (basic)                 | ✅ Done                      |
| 7      | Combat UI Polish (Popup Menu, refined movement)                     | ✅ Done                      |
| 8      | Item System (JSON schema, data, basic use - e.g. Potion)            | ⏳ Next Up                   |
| 9      | ClassTree progression UI + skill/perk unlocks                       | ⬜ To Do                     |
| 10     | RestSystem + camp events + rest UI                                  | ⬜ To Do                     |
| 11     | Data-driven events: random event tables, map hazards                | ⬜ To Do                     |
| 12     | Polish: transitions, audio, particle FX, tooltips, expanded AI      | ⬜ To Do                     |


# 4. Data Formats & Assets

*   JSON Schemas for:
    *   `quests.json` - ✅ Done
    *   `skills.json` - ✅ Done
    *   `enemies.json` (Implicit, `EnemyData.java` loads from structured JSON) - ✅ Done
    *   `classes.json` (Basic, `ClassData.java` loads) - ✅ Done (Basic)
    *   `items.json` - ⏳ Next Up
    *   `perks.json` - ⬜ To Do
    *   `races.json` - ⬜ To Do
    *   `events.json` - ⬜ To Do
*   TiledMap (TMX + JSON) for overworld and battle layouts - ✅ Done
*   Texture Atlases for character portraits, map icons, combat sprites (Using single spritesheet currently) - ✅ Done (Basic)
*   Scene2D Skins (JSON + atlas) for UI theme - ✅ Done (Basic skin + fallback implemented)
