# The RIng Goes South | Prototype

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project is a tactical roguelike inspired by Lord of the Rings, focusing on party management, exploration, and turn-based combat.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
Useful Gradle tasks:
- `./gradlew.bat lwjgl3:run` (Windows) or `./gradlew lwjgl3:run` (Linux/macOS): Starts the application.
- `./gradlew.bat build` or `./gradlew build`: Builds sources and archives.
- `./gradlew.bat clean`: Removes `build` folders.

## High-level features - Current Status

### 🎮 1. Project Setup
✅ Goals:
- Establish the base project - ✅ Done
- Organize the architecture - ✅ Done (Initial structure in place)

🔧 Tasks:
- Create a new LibGDX project using gdx-setup - ✅ Done
- Choose Java as language - ✅ Done
- Set up a module-based structure (core, lwjgl3, assets) - ✅ Done
- Add libraries (LibGDX, Scene2D, Gdx-Json) - ✅ Done

### 🌍 2. Overworld Map System
✅ Goals:
- Build a grid-based map with a static layout - ✅ Done
- Dynamic elements (weather, hazards) - ⬜ To Do

🔧 Tasks:
- Design a Tiled map (`battle_map.tmx`, `lotr_map.tmx`) - ✅ Done
- LibGDX TiledMap + OrthographicCamera for rendering (`MapScreen`) - ✅ Done
- Implement region types (implicit via Tiled layers) - ✅ Done (Basic)
- Add event system (Quest givers on map via `MapInteraction`) - ✅ Done (Basic for Quests)
- Use a graph or tile-based system for route calculation - ⬜ To Do (Currently direct tile movement)
- Create a Fog of War mechanic - ⬜ To Do

### 🧭 3. Exploration & Resource System
✅ Goals:
- Track resources and affect path choices - ✅ Done (Tracking implemented)

🔧 Tasks:
- Create `ResourceManager` (Food, Firewood, Gold, Hope) - ✅ Done
- Tie resources to actions (e.g., entering cold zones costs firewood) - ⬜ To Do (Planned)
- UI panel for visible resource counters (`MapScreen` displays some) - ✅ Done (Basic)
- Create Node types with specific logic (Quest givers are a start) - ✅ Done (Basic)

### 🧑‍🤝‍🧑 4. Party System & RPG Logic
✅ Goals:
- Party members, classes, races, traits, and leveling - ✅ Done (Party, Classes, Skills, Leveling implemented)

🔧 Tasks:
- Define `Character` structure (`GameCharacter` interface, `Character` class) - ✅ Done
- Implement a Class Tree structure for progression - ⬜ To Do (Basic `GameClass` enum exists)
- Track HP, XP, statuses, exploration perks - ✅ Done (HP, Basic Skills, Movement. XP, Perks ⬜ To Do)
- Design prefab and randomly generated character pools (`RingPrototypeGame` initializes party) - ✅ Done (Basic)

### 💬 5. Dialog & Event System
✅ Goals:
- Support narrative choices, random events, character development - ✅ Done (Quest dialogs implemented)

🔧 Tasks:
- Create a DialogParser (JSON for Quests: `QuestManager`, `DialogScreen`) - ✅ Done
- Implement a state machine for conversations (`DialogScreen` handles choices) - ✅ Done
- Allow events to affect party status, relationships, resources (Quest rewards implemented) - ✅ Done (Basic)
- Add trigger conditions: terrain, time, party composition (Quest givers on map) - ✅ Done (Basic)

### 🔁 6. Turn-Based Combat (Tactics Style)
✅ Goals:
- Build a simple tactics battle system with skills and turns - ✅ Done

🔧 Tasks:
- Use a 2D grid system (`BattleScreen` with TiledMap) - ✅ Done
- Characters take turns (Initiative-based in `BattleScreen`) - ✅ Done
- Skills with AoE, effects, and costs (Data-driven from `skills.json`) - ✅ Done
- Movement + attack range highlighting (`BattleInputHandler`, `BattleScreen`) - ✅ Done
- Enemy AI with simple decision-making (`Enemy.performSimpleAI`) - ✅ Done (Basic)
- Refined combat controls: Move with mouse, skill then move, popup action menu - ✅ Done

### 🏕️ 7. Rest System + Interactions
✅ Goals:
- Allow party to rest, restore, and trigger inter-character dialogs - ⬜ To Do

🔧 Tasks:
- Implement Rest Nodes or Rest Mode anywhere - ⬜ To Do
- Chance-based events or mandatory rest every X turns - ⬜ To Do
- Dialog options or automatic interactions based on relationships - ⬜ To Do
- Rest benefits: heal, cure, gain buffs or XP - ⬜ To Do

### 📦 8. UI & UX
✅ Goals:
- Clean layout for map, party stats, inventory, and event/dialog UI - ✅ In Progress (Map, Battle, Dialog UI exist)

🔧 Tasks:
- Use Scene2D.UI for layout and styling - ✅ Done
- Overlays for dialogs, menus (Battle popup), inventory (placeholder), rest screen (ToDo) - ✅ In Progress
- Tooltips for skills, traits, etc. - ⬜ To Do
- Keyboard/mouse and maybe gamepad support - ✅ Keyboard/Mouse basic support

### 📁 9. Data & Content Pipeline
✅ Goals:
- Load content via JSON - ✅ Done (Quests, Skills, Enemies, Classes)

🔧 Tasks:
- Character definitions (Partially in `RingPrototypeGame`, `ClassData`) - ✅ Done (Basic)
- Item and skill definitions (`skills.json`, `skill-schema.json` - ✅ Done. Items - ⏳ Next Up)
- Events, dialog trees (`quests.json` for quest dialogs) - ✅ Done
- Prefab map layout and dynamic event flags (`.tmx` maps) - ✅ Done

### 🧪 10. Testing & Debug Tools
✅ Goals:
- Tools to debug AI, paths, events, dialog, and combat - ⬜ To Do (Basic logging exists)

🔧 Tasks:
- Add dev commands (toggle fog, add XP, spawn events) - ⬜ To Do
- Console output for game state - ✅ Done (Extensive Gdx.app.log)
- Seeded randomness for repeatable testing - ⬜ To Do
- Unit tests (`SkillDataTest.java` exists) - ✅ Done (Basic)

# Feature roadmap (Updated)

---

## 🛠️ Prerequisites

- Java 11+ or Kotlin-ready JDK
- Gradle
- LibGDX setup tool (if starting from scratch)
- Git (for version control)

---

## 🚀 Installation

1. Clone this repository
   ```bash
   # Replace with your actual repository URL if different
   git clone https://github.com/yourname/ring-prototype.git 
   cd ring-prototype
   ```

2. Import into your IDE (IntelliJ IDEA recommended) as a Gradle project.
3. Run the desktop launcher:

   ```bash
   # On Windows
   .\gradlew.bat lwjgl3:run
   # On Linux/macOS
   ./gradlew lwjgl3:run
   ```

---

## 🗺️ Development Roadmap

### Phase 1: Project Setup & Core Systems (Largely ✅ Done)
* [✅] Initialize LibGDX project (core, lwjgl3 modules)
* [✅] Configure build scripts (Gradle)
* [✅] Add dependencies: JSON, Scene2D.UI, TiledMap support
* [✅] Basic `RingPrototypeGame` structure, Asset loading (basic), Skin setup (with fallback)
* [✅] `ResourceManager` (food, firewood, gold, hope, time)
* [✅] `PartyManager` and `Character` class (name, class, level, HP, MP, skills, inventory basics)
* [✅] `ClassData` (loading basic class info from JSON)
* [✅] `EnemyData` (loading enemies from JSON)
* [✅] `SkillData` (loading skills from JSON, `skill-schema.json`)
* [✅] `QuestManager` (loading quests from JSON, `quest-schema.json`)

### Phase 2: Map & Navigation (Largely ✅ Done)
* [✅] Implement `OverworldMap` (`MapScreen`) and `MapNode` (implicit via Tiled, `MapInteraction`)
* [✅] Load & render Tiled map in `MapScreen`
* [✅] Basic party movement on map, camera controls
* [⬜] Fog of War shader/mask
* [⬜] `RouteCalculator` (terrain costs + dynamic hazards)

### Phase 3: Dialog & Events (Largely ✅ Done)
* [✅] Develop `DialogParser` (JSON for Quests handled by `QuestManager`)
* [✅] Create `DialogScreen` for choice resolution
* [✅] Implement `EventTrigger` for map-based quest givers (`MapInteraction`)
* [✅] Integrate quest events into map traversal (initiating dialogs)
* [✅] Quest objective tracking (e.g., enemy kills in `BattleScreen`)

### Phase 4: Combat System (Largely ✅ Done)
* [✅] Build `BattleGrid` (`BattleScreen` using TiledMap)
* [✅] Extend `Character` to `Combatant` (`BattleCharacter`, `Enemy` using `IBattleActor`)
* [✅] Implement `CombatManager` logic (turn loop, skill resolution, damage, movement in `BattleScreen`)
* [✅] Add basic `AIController` for enemy behavior (`Enemy.performSimpleAI`)
* [✅] Design `CombatScreen` UI (`BattleUiManager`)
* [✅] Mouse-based movement, skill-then-move, popup action menu for combat

### Phase 5: Item System (⏳ Next Up)
* [⬜] Define `Item` class and `ItemType` enum
* [⬜] Create `item-schema.json` and `items.json`
* [⬜] Implement `ItemDataManager` to load item data
* [⬜] Integrate item effects (e.g., health potion consumable from battle popup menu)
* [⬜] Basic inventory display and management UI

### Phase 6: Class Progression & Deeper RPG Elements
* [⬜] Full `ClassTree` implementation with tiers and prerequisites (JSON defined)
* [⬜] `Perk` system (JSON defined) and integration with character progression
* [⬜] `Race` system with traits affecting stats/abilities (JSON defined)
* [⬜] UI screens for Party Roster, Character Details (stats, skills, perks, equipment), Skill Tree

### Phase 7: Rest & Camp System
* [⬜] Implement `RestSystem` (healing, resource consumption)
* [⬜] Define `CampEvent` subclasses (dialogs, ambushes)
* [⬜] Create `RestScreen` UI with inter-character dialog hooks

### Phase 8: UI Polish & Advanced Features
* [⬜] Advanced AI behavior for enemies
* [⬜] More varied event triggers (time-based, resource-based)
* [⬜] Tooltips for UI elements (skills, items, stats)
* [⬜] Sound effects and music integration
* [⬜] Particle FX for skills and environment
* [⬜] Save/Load game state
* [⬜] Debug console (`com.yourgame.util.DebugConsole`) to spawn events, grant XP/resources

---

## 📦 Data & Assets

* [✅] JSON schemas for: Skills, Quests. (Basic for Classes, Enemies)
* [⏳] JSON schemas for: Items, Perks, Races, Events (Next: Items)
* [✅] TiledMap files & JSON exports for overworld layout (`lotr_map.tmx`, `battle_map.tmx`)
* [✅] Texture atlases for sprites, icons, UI (Using `colored-transparent_packed.png`)
* [✅] Scene2D skins and style definitions (`uiskin.json`, fallback skin in `RingPrototypeGame`)

---

## 🎯 Next Steps / Stretch Goals (Long Term)

* [ ] Analytics integration for playtesting metrics
* [ ] Modular mod-support for custom content
* [ ] Localization framework
* [ ] Accessibility options (colorblind modes, remappable controls)

---
