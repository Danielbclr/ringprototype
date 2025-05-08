# The RIng Goes South | Prototype

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## High-level features
### 🎮 1. Project Setup
✅ Goals:
- Establish the base project
- Organize the architecture

🔧 Tasks:
- Create a new LibGDX project using gdx-setup
- Choose Kotlin or Java as your language
- Set up a module-based structure:

  - core (game logic)
  - desktop (testing)
  - assets (art, sounds, data)

- Add libraries (e.g., JSON handling, entity systems, UI toolkit like Scene2D)

### 🌍 2. Overworld Map System
✅ Goals:
- Build a grid-based map with a static layout but dynamic elements (weather, hazards)

🔧 Tasks:
- Design a Tiled map or use LibGDX's TiledMap + OrthographicCamera
- Implement region types: towns, forests, rivers, ruins, etc.
- Add event system: bridges collapsing, storms blocking paths
- Use a graph or tile-based system for route calculation
- Create a Fog of War mechanic using masking or blending

### 🧭 3. Exploration & Resource System
✅ Goals:
- Track resources and affect path choices

🔧 Tasks:
- Create ResourceManager to handle things like food, firewood, currency
- Tie resources to actions (e.g., entering cold zones costs firewood)
- UI panel for visible resource counters
- Create Node types with specific logic (e.g., town = rest point)

### 🧑‍🤝‍🧑 4. Party System & RPG Logic
✅ Goals:
- Party members, classes, races, traits, and leveling

🔧 Tasks:
- Define character structure:

````kotlin
class Character {
    val race: Race
    val clazz: Class
    val traits: List<Trait>
    val skills: List<Skill>
    val items: List<Item>
}
````
- Implement a Class Tree structure for progression
- Track HP, XP, statuses, and exploration perks
- Design prefab and randomly generated character pools

### 💬 5. Dialog & Event System
✅ Goals:
- Support narrative choices, random events, character development

🔧 Tasks:
- Create a DialogParser (e.g., from JSON or Ink-style markup)
- Implement a state machine for conversations
- Allow events to affect party status, relationships, resources
- Add trigger conditions: terrain, time, party composition

### 🔁 6. Turn-Based Combat (Tactics Style)
✅ Goals:
- Build a simple tactics battle system with skills and turns

🔧 Tasks:
- Use a 2D grid system (like Vector2[][]) for the battlefield
- Characters take turns (Speed-based or fixed initiative)
- Skills with AoE, effects, and costs
- Movement + attack range highlighting
- Enemy AI with simple decision-making

### 🏕️ 7. Rest System + Interactions
✅ Goals:
- Allow party to rest, restore, and trigger inter-character dialogs

🔧 Tasks:
- Implement Rest Nodes or Rest Mode anywhere
- Chance-based events or mandatory rest every X turns
- Dialog options or automatic interactions based on relationships
- Rest benefits: heal, cure, gain buffs or XP

### 📦 8. UI & UX
✅ Goals:
- Clean layout for map, party stats, inventory, and event/dialog UI

🔧 Tasks:
- Use Scene2D.UI for layout and styling
- Overlays for dialogs, menus, inventory, rest screen
- Tooltips for skills, traits, etc.
- Keyboard/mouse and maybe gamepad support

### 📁 9. Data & Content Pipeline
✅ Goals:
- Load content via JSON, XML, or script format

🔧 Tasks:
- Character definitions
- Item and skill definitions
- Events, dialog trees
- Prefab map layout and dynamic event flags

### 🧪 10. Testing & Debug Tools
✅ Goals:
- Tools to debug AI, paths, events, dialog, and combat

🔧 Tasks:
- Add dev commands (toggle fog, add XP, spawn events)
- Console output for game state
- Seeded randomness for repeatable testing

# Feature roadmap

---

## 🛠️ Prerequisites

- Java 11+ or Kotlin-ready JDK  
- Gradle or Maven  
- LibGDX setup tool  
- Git (for version control)

---

## 🚀 Installation

1. Clone this repository 
   ```bash
   // PLACEHOLDER CHANGE THIS LATER
   git clone https://github.com/yourname/fellowship-roguelike.git
   cd fellowship-roguelike
   ````

2. Import into your IDE as a Gradle project.
3. Run the desktop launcher:

   ```bash
   ./gradlew desktop:run
   ```

---

## 🗺️ Development Roadmap

### Phase 1: Project Setup

* [ ] Initialize LibGDX project (core, desktop, html modules)
* [ ] Configure build scripts (Gradle/Maven)
* [ ] Add dependencies: JSON, Scene2D.UI, TiledMap support

### Phase 2: Map & Navigation

* [ ] Implement `OverworldMap` and `MapNode` classes
* [ ] Load & render Tiled map in `MapScreen`
* [ ] Add Fog of War shader/mask
* [ ] Create `RouteCalculator` (terrain costs + dynamic hazards)

### Phase 3: Resources & Party

* [ ] Build `ResourceManager` (food, firewood, gold, morale, time)
* [ ] UI panel for resource display
* [ ] Define `Character` and `PartyManager` classes
* [ ] Load ClassTree, Skill, Perk, Race, Trait data from JSON

### Phase 4: Dialog & Events

* [ ] Develop `DialogParser` (JSON/Ink style)
* [ ] Create `DialogManager` (choice resolution)
* [ ] Implement `EventTrigger` for map & rest events
* [ ] Integrate events into map traversal

### Phase 5: Combat System

* [ ] Build `BattleGrid` (2D tile grid)
* [ ] Extend `Character` to `Combatant` (HP, status, turn order)
* [ ] Implement `CombatManager` (turn loop, skill resolution)
* [ ] Add `AIController` for enemy behavior
* [ ] Design `CombatScreen` (movement, targeting, skill UI)

### Phase 6: Rest & Camp

* [ ] Implement `RestSystem` (healing, resource consumption)
* [ ] Define `CampEvent` subclasses
* [ ] Create `RestScreen` UI with inter-character dialog hooks

### Phase 7: UI Polish & Tools

* [ ] Scene2D screens: inventory, party roster, skill tree
* [ ] Debug console (spawn events, grant XP/resources)
* [ ] Tooltips, pop-up tutorials, accessibility checks

---

## 📦 Data & Assets

* [ ] JSON schemas for Classes, Skills, Perks, Races, Items, Events
* [ ] TiledMap files & JSON exports for overworld layout
* [ ] Texture atlases for sprites, icons, UI
* [ ] Scene2D skins and style definitions

---

## 🎯 Next Steps / Stretch Goals

* [ ] Analytics integration for playtesting metrics
* [ ] Modular mod-support for custom content
* [ ] Localization framework
* [ ] Accessibility options (colorblind modes, remappable controls)

---
