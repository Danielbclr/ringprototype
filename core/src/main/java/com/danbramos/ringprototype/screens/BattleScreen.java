package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer; // Import Timer for delayed screen transition
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.battle.*;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.battle.skills.SkillType;
import com.danbramos.ringprototype.input.BattleInputHandler;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.quests.QuestManager;
import com.danbramos.ringprototype.resources.ResourceType; // Import ResourceType for rewards
import com.danbramos.ringprototype.screens.ui.BattleUiManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BattleScreen implements Screen {
    private final RingPrototypeGame game;
    private TiledMap map;
    private TiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private Stage stage;
    private ShapeRenderer shapeRenderer;

    private int tileWidth;
    private int tileHeight;
    private int mapWidthInTiles;
    private int mapHeightInTiles;

    private static final float VIEWPORT_WIDTH_IN_TILES = 20f;
    private static final float VIEWPORT_HEIGHT_IN_TILES = 15f;

    // Battle Map Constants
    private static final int PLAYER_SIDE_X_MIN = 1;
    private static final int PLAYER_SIDE_X_MAX = 5;
    private static final int ENEMY_SIDE_X_MIN = 10;
    private static final int ENEMY_SIDE_X_MAX = 14;
    private static final int MIN_ENEMIES = 2;
    private static final int MAX_ENEMIES = 3;
    private static final String[] ENEMY_TYPES = {"orc_grunt", "goblin_archer", "warg"};

    private Random random = new Random();

    // Turn Management
    private boolean battleEnded = false; // Flag to prevent multiple end-game logic calls

    // Managers
    private BattleUiManager uiManager;
    private BattleInputHandler inputHandler;

    private InputMultiplexer inputMultiplexer;

    // +++ Added TurnManager +++
    private TurnManager turnManager;

    public BattleScreen(RingPrototypeGame game) {
        this.game = game;
    }

    public int getMapWidthInTiles() {
        return mapWidthInTiles;
    }

    public int getMapHeightInTiles() {
        return mapHeightInTiles;
    }

    public int getTileWidth() {
        return tileWidth;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public BattleUiManager getUiManager() {
        return uiManager;
    }

    @Override
    public void show() {
        Gdx.app.log("BattleScreen", "Showing BattleScreen.");
        battleEnded = false; // Reset battle end flag
        turnManager = new TurnManager(); // Instantiate TurnManager

        map = new TmxMapLoader().load("tilemaps/battle_map.tmx");
        shapeRenderer = new ShapeRenderer();

        if (map.getProperties().containsKey("tilewidth") && map.getProperties().containsKey("tileheight")) {
            tileWidth = map.getProperties().get("tilewidth", Integer.class);
            tileHeight = map.getProperties().get("tileheight", Integer.class);
        } else if (map.getLayers().getCount() > 0 && map.getLayers().get(0) instanceof TiledMapTileLayer) {
            TiledMapTileLayer firstLayer = (TiledMapTileLayer) map.getLayers().get(0);
            tileWidth = (int) firstLayer.getTileWidth();
            tileHeight = (int) firstLayer.getTileHeight();
        } else {
            tileWidth = 16; tileHeight = 16;
            Gdx.app.error("BattleMapLoad", "Could not determine tile size, using default 16x16.");
        }

        mapWidthInTiles = map.getProperties().get("width", Integer.class);
        mapHeightInTiles = map.getProperties().get("height", Integer.class);
        int mapPixelWidth = mapWidthInTiles * tileWidth;
        int mapPixelHeight = mapHeightInTiles * tileHeight;

        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f);
        camera = new OrthographicCamera();
        stage = new Stage(new ScreenViewport());

        uiManager = new BattleUiManager(game, stage, this);
        inputHandler = new BattleInputHandler(this, camera, tileWidth, tileHeight, mapWidthInTiles, mapHeightInTiles);

        inputMultiplexer = new InputMultiplexer();
        inputMultiplexer.addProcessor(stage);
        inputMultiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(inputMultiplexer);

        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(mapPixelWidth / 2f, mapPixelHeight / 2f, 0);
        camera.zoom = Math.max(1f, Math.max(mapPixelWidth / camera.viewportWidth, mapPixelHeight / camera.viewportHeight));
        camera.update();

        // Generate random enemies and positions
        generateRandomEncounter();

        // Initialize turn system using TurnManager
        turnManager.initializeTurnOrder(game.partyManager.getMembers(), game.currentBattleEnemies);
        startTurnFor(turnManager.getCurrentActor()); // Start turn for the first actor
    }

    /**
     * Generates a random battle encounter with 2-3 enemies
     * and positions all actors randomly on the battle map
     */
    private void generateRandomEncounter() {
        // Clear previous battle state
        game.currentBattleEnemies.clear();

        // 1. Generate random enemy positions on the right side
        int numEnemies = MIN_ENEMIES + random.nextInt(MAX_ENEMIES - MIN_ENEMIES + 1);

        // Create a list of occupied positions to avoid overlap
        List<Vector2> occupiedPositions = new ArrayList<>();

        // 2. Generate random enemies
        for (int i = 0; i < numEnemies; i++) {
            generateRandomEnemy(occupiedPositions);
        }

        // 3. Randomly place player characters on the left side
        randomlyPositionPartyMembers(occupiedPositions);
    }

    /**
     * Generates a random enemy and adds it to the battle
     */
    private void generateRandomEnemy(List<Vector2> occupiedPositions) {
        if (game.characterSheet == null) {
            Gdx.app.error("BattleScreen", "Cannot create enemies, character sheet is null");
            return;
        }

        // Get random position on right side of map
        Vector2 position = getRandomUnoccupiedPosition(
            ENEMY_SIDE_X_MIN, ENEMY_SIDE_X_MAX,
            1, mapHeightInTiles - 2,
            occupiedPositions
        );

        // Select a random enemy type from the enemy IDs array
        String enemyId = ENEMY_TYPES[random.nextInt(ENEMY_TYPES.length)];

        // Create the enemy using the EnemyData system
        Enemy enemy = EnemyData.getInstance().createEnemy(enemyId, game.characterSheet, position.x, position.y);

        if (enemy == null) {
            // Fallback to the old method if EnemyData failed to create the enemy
            Gdx.app.error("BattleScreen", "Failed to create enemy from ID: " + enemyId + ". Using fallback method.");

            // Create enemy with appropriate sprite and stats (fallback)
            int enemySpriteX, enemySpriteY;
            int hp;
            String damageRoll;
            int moveRange;
            String enemyType;

            switch (enemyId) {
                case "warg":
                    enemySpriteX = 30;
                    enemySpriteY = 3;
                    hp = 12;
                    damageRoll = "1d8";
                    moveRange = 5;
                    enemyType = "Warg";
                    break;
                case "goblin_archer":
                    enemySpriteX = 25;
                    enemySpriteY = 1;
                    hp = 6;
                    damageRoll = "1d4";
                    moveRange = 4;
                    enemyType = "Goblin Archer";
                    break;
                case "orc_grunt":
                default:
                    enemySpriteX = 27;
                    enemySpriteY = 2;
                    hp = 8;
                    damageRoll = "1d6";
                    moveRange = 3;
                    enemyType = "Orc Grunt";
                    break;
            }

            TextureRegion enemySprite = new TextureRegion(
                game.characterSheet,
                enemySpriteX * tileWidth,
                enemySpriteY * tileHeight,
                tileWidth,
                tileHeight
            );

            enemy = new Enemy(
                enemyType,
                hp,
                damageRoll,
                enemySprite,
                position.x,
                position.y,
                moveRange
            );
        }

        game.currentBattleEnemies.add(enemy);
        occupiedPositions.add(position);

        Gdx.app.log("BattleScreen", enemy.getName() + " added at position " + position);
    }

    /**
     * Randomly positions party members on the left side of the battlefield
     */
    private void randomlyPositionPartyMembers(List<Vector2> occupiedPositions) {
        // For each party member, find a random position on the left side
        for (GameCharacter character : game.partyManager.getMembers()) {
            if (character != null) {
                Vector2 position = getRandomUnoccupiedPosition(
                    PLAYER_SIDE_X_MIN, PLAYER_SIDE_X_MAX,
                    1, mapHeightInTiles - 2,
                    occupiedPositions
                );

                // Set the character's battle position to this random position
                character.setBattleMapPosition(position.x, position.y);
                occupiedPositions.add(position);

                Gdx.app.log("BattleScreen", character.getName() + " positioned at " + position);
            }
        }
    }

    /**
     * Gets a random unoccupied position within the given bounds
     */
    private Vector2 getRandomUnoccupiedPosition(int xMin, int xMax, int yMin, int yMax, List<Vector2> occupiedPositions) {
        Vector2 position;
        boolean validPosition;

        // Try to find an unoccupied position
        do {
            int x = xMin + random.nextInt(xMax - xMin + 1);
            int y = yMin + random.nextInt(yMax - yMin + 1);
            position = new Vector2(x, y);

            // Check if this position is already occupied
            validPosition = true;
            for (Vector2 occupied : occupiedPositions) {
                if (occupied.epsilonEquals(position)) {
                    validPosition = false;
                    break;
                }
            }
        } while (!validPosition);

        return position;
    }

    private void startTurnFor(IBattleActor actor) {
        if (actor == null || battleEnded) return;
        inputHandler.resetState();

        actor.startTurn();
        Gdx.app.log("BattleScreen", "Starting turn for: " + actor.getName());
        uiManager.updateSkillButtons(actor);

        if (actor instanceof BattleCharacter) {
            BattleCharacter bc = (BattleCharacter) actor;
            // Always calculate movement tiles at turn start if movement > 0
            if (bc.getRemainingMovement() > 0) {
                inputHandler.calculateMovementReachableTiles(bc);
            }
            uiManager.setEndTurnButtonDisabled(false);
        } else if (actor instanceof Enemy) {
            uiManager.setEndTurnButtonDisabled(true);
        }
        uiManager.updateTurnInfo(actor);
    }

    public void advanceTurn() {
        if (battleEnded) return;

        IBattleActor currentActor = turnManager.getCurrentActor();
        if (currentActor != null) {
            currentActor.endTurn(); // End turn for the actor who just finished
        }
        inputHandler.resetState(); // Reset input state for the new turn

        IBattleActor nextActor = turnManager.advanceTurn();

        if (nextActor == null) { // advanceTurn returns null if battle is over
            if (!battleEnded) { // Check battleEnded flag to prevent multiple calls
                 Gdx.app.log("BattleScreen", "Battle Over condition met via TurnManager.");
                 handleBattleEnd();
            }
            return;
        }

        startTurnFor(nextActor);
    }

    public boolean isTileWithinMapBounds(float tileX, float tileY) {
        return tileX >= 0 && tileX < mapWidthInTiles && tileY >= 0 && tileY < mapHeightInTiles;
    }

    // Delegated to TurnManager
    public boolean isBattleOver() {
        return turnManager.isBattleOver() || battleEnded; // Also check local flag
    }

    private void handleBattleEnd() {
        if (battleEnded) return; // Prevent this from running multiple times
        battleEnded = true;

        Gdx.app.log("BattleScreen", "Handling battle end.");
        // Current actor is already null or irrelevant as battle ended via TurnManager check
        inputHandler.resetState();
        uiManager.setEndTurnButtonDisabled(true);
        uiManager.clearSkillButtons();
        uiManager.hidePopupMenu(); // Ensure popup is hidden

        // Determine winner based on who is left in TurnManager's final state
        boolean playersWon = false;
        boolean enemiesStillAlive = false; // Check if any enemies survived
        int livingPlayerCount = 0;

        for(IBattleActor actor : turnManager.getTurnOrder()) { // Check the final turn order list
            if(actor.isAlive()){
                if(actor instanceof BattleCharacter){
                    livingPlayerCount++;
                } else if (actor instanceof Enemy){
                    enemiesStillAlive = true;
                }
            }
        }

        if (livingPlayerCount > 0 && !enemiesStillAlive) {
            playersWon = true;
        }

        if(playersWon) {
            Gdx.app.log("BattleScreen", "PLAYER VICTORY!");
            uiManager.updateTurnInfo(null); // Clear turn info

            // Display victory message in the battle log
            uiManager.updateBattleLog("VICTORY! Your party is triumphant!");

            // Update UI to show "Victory!"
            if (uiManager != null) { // Check if uiManager is initialized
                if (uiManager.turnInfoLabel != null) uiManager.turnInfoLabel.setText("VICTORY!");
            }

            // Apply XP, loot, etc. to the *original* GameCharacters
            for(GameCharacter originalChar : game.partyManager.getMembers()){
                boolean survived = false;
                int finalHp = 0;
                 for(IBattleActor battleActor : turnManager.getTurnOrder()){
                     if(battleActor instanceof BattleCharacter && ((BattleCharacter)battleActor).getSourceCharacter() == originalChar && battleActor.isAlive()){
                         survived = true;
                         finalHp = battleActor.getCurrentHp();
                         break;
                     }
                 }
                 if(survived){
                     originalChar.setHealthPoints(finalHp); // Apply final HP
                     // TODO: Apply XP gain here
                     Gdx.app.log("BattleScreen", originalChar.getName() + " survived. HP updated.");
                 } else {
                      Gdx.app.log("BattleScreen", originalChar.getName() + " did not survive.");
                      // Handle character death persistence if needed
                 }
            }

            // Award resources based on number and type of enemies defeated
            int baseGoldReward = 30;
            int baseFoodReward = 1;
            int enemyCount = game.currentBattleEnemies.size; // Use the initial enemy list for reward scaling

            int goldReward = baseGoldReward * enemyCount + random.nextInt(20);
            int foodReward = baseFoodReward + random.nextInt(enemyCount + 1);

            game.resourceManager.addResource(ResourceType.GOLD, goldReward);
            game.resourceManager.addResource(ResourceType.FOOD, foodReward);
            Gdx.app.log("BattleScreen", "Awarded " + goldReward + " Gold and " + foodReward + " Food.");

            // Display rewards in battle log
            uiManager.updateBattleLog("VICTORY!\nYour party gained " + goldReward + " Gold and " + foodReward + " Food.\nReturning to map in 3 seconds...");

            // Transition back to MapScreen after a delay
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    if (game != null) { // Ensure game still exists
                        game.setScreen(new MapScreen(game));
                    }
                }
            }, 3); // 3 second delay
        } else {
            Gdx.app.log("BattleScreen", "PLAYER DEFEAT - GAME OVER!");

            // Display defeat message in the battle log
            uiManager.updateBattleLog("DEFEAT! Your party has fallen in battle...");

            if (uiManager != null) {
                if (uiManager.turnInfoLabel != null) uiManager.turnInfoLabel.setText("GAME OVER!");
            }
            // Handle game over:
            // - Show a game over message prominently
            // - Disable all input except maybe a "Quit" or "Main Menu" button
            // - For now, we just log and stop further battle processing.
            // A proper game over screen or logic to return to a main menu would go here.
            Gdx.input.setInputProcessor(null); // Disable further input for this screen
        }
    }

    public IBattleActor getCurrentTurnActor() {
        return turnManager.getCurrentActor();
    }

    public boolean isTileOccupied(float tileX, float tileY) {
        for (IBattleActor actor : turnManager.getTurnOrder()) { // Use turnManager list
            if (actor.isAlive() && actor.getBattleMapPosition().epsilonEquals(tileX, tileY)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTileOccupiedByAlly(float tileX, float tileY, IBattleActor askingActor) {
         IBattleActor actorOnTile = getActorAtTile(tileX, tileY);
         if (actorOnTile == null || actorOnTile == askingActor || !actorOnTile.isAlive()) {
             return false;
         }
         // Check if they are the same type (both players or both enemies)
         return (askingActor instanceof BattleCharacter && actorOnTile instanceof BattleCharacter) ||
                (askingActor instanceof Enemy && actorOnTile instanceof Enemy);
    }

    public IBattleActor getActorAtTile(float tileX, float tileY) {
        for (IBattleActor actor : turnManager.getTurnOrder()) { // Use turnManager list
            if (actor.isAlive() && actor.getBattleMapPosition().epsilonEquals(tileX, tileY)) {
                return actor;
            }
        }
        return null;
    }

    public void selectSkill(Skill skill) {
        if (battleEnded) return;
        if (turnManager.getCurrentActor() instanceof BattleCharacter) {
            inputHandler.selectSkill(skill, (BattleCharacter) turnManager.getCurrentActor());
        }
    }

    public void handleCharacterMove(BattleCharacter mover, float tileX, float tileY, int movementCost) {
        if (battleEnded) return;
        // Normal movement should not be allowed if a major action has already been performed.
        if (mover.hasPerformedMajorAction()) {
            Gdx.app.log("BattleScreen", mover.getName() + " cannot move normally after performing a major action.");
            return;
        }

        Gdx.app.log("BattleScreen", mover.getName() + " moving to " + tileX + "," + tileY + " (cost: " + movementCost + ")");
        mover.setBattleMapPosition(tileX, tileY);
        mover.useMovement(movementCost); // Deduct movement points

        // Normal movement itself does NOT set hasPerformedMajorAction.
        // The player can still use a major action skill after moving.

        inputHandler.clearAllHighlights();
        uiManager.updateSkillButtons(mover); // Skills might still be usable
        uiManager.updateTurnInfo(mover);

        if (mover.getRemainingMovement() > 0) {
            inputHandler.calculateMovementReachableTiles(mover); // Recalculate for remaining move
            inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
        } else {
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE); // No movement left
            Gdx.app.log("BattleScreen", mover.getName() + " has no normal movement left.");
        }
    }

    public void executeSingleTargetSkill(BattleCharacter caster, Skill skill, IBattleActor target) {
        if (battleEnded) return;
        // Mana cost check
        if (caster.getSourceCharacter().getManaPoints() < skill.getManaCost()) {
            uiManager.updateBattleLog(caster.getName() + " does not have enough mana for " + skill.getName() + "!");
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE); // Reset state
            return;
        }
        caster.getSourceCharacter().setManaPoints(caster.getSourceCharacter().getManaPoints() - skill.getManaCost());


        Gdx.app.log("BattleScreen", caster.getName() + " attempts " + skill.getName() + " on " + target.getName());

        List<IBattleActor> targets = new ArrayList<>();
        targets.add(target);
        skill.performExecution(caster, targets, this); // DELEGATE HERE

        caster.setHasPerformedMajorAction(true);
        inputHandler.clearAllHighlights();
        uiManager.updateSkillButtons(caster);
        uiManager.updateTurnInfo(caster);

        if (caster.getRemainingMovement() > 0) {
            inputHandler.calculateMovementReachableTiles(caster);
            inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
        } else {
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE);
        }

        if (!target.isAlive()) {
            Gdx.app.log("BattleScreen", target.getName() + " has been defeated!");
            uiManager.updateBattleLog(target.getName() + " has been defeated!");
            if (target instanceof Enemy) {
                String enemyType = target.getName().toLowerCase().replace(" ", "_");
                QuestManager.getInstance().updateKillObjectives(enemyType);
            }
        }
        if(isBattleOver()){ handleBattleEnd(); }
    }

    public void executeSupportSkill(BattleCharacter caster, Skill skill) { // Self-target support
        if (battleEnded || caster == null || skill == null) return;
        if (caster.hasPerformedMajorAction() && !skill.getId().equals("skill_nimble_movement")) { // Nimble is free
            Gdx.app.log("BattleScreen", caster.getName() + " has already performed a major action.");
            return;
        }
        // Mana cost check
        if (caster.getSourceCharacter().getManaPoints() < skill.getManaCost()) {
            uiManager.updateBattleLog(caster.getName() + " does not have enough mana for " + skill.getName() + "!");
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE); // Reset state
            return;
        }
        caster.getSourceCharacter().setManaPoints(caster.getSourceCharacter().getManaPoints() - skill.getManaCost());

        Gdx.app.log("BattleScreen", caster.getName() + " attempts support skill: " + skill.getName());

        List<IBattleActor> targets = new ArrayList<>();
        targets.add(caster); // Self-target for skills like Stealth
        skill.performExecution(caster, targets, this); // DELEGATE HERE

        // Nimble Movement is a special case - it's a free action
        if (!skill.getId().equals("skill_nimble_movement")) {
            caster.setHasPerformedMajorAction(true);
        }

        uiManager.updateSkillButtons(caster);
        uiManager.updateTurnInfo(caster);

        // State transition logic after skill execution
        if (!skill.getId().equals("skill_nimble_movement")) { // Nimble movement has its own state transition
            if (caster.getRemainingMovement() > 0 && !caster.hasPerformedMajorAction()) { // Check major action again if skill was free
                inputHandler.calculateMovementReachableTiles(caster);
                inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
            } else if (caster.getRemainingMovement() > 0 && caster.hasPerformedMajorAction()){
                inputHandler.calculateMovementReachableTiles(caster);
                inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
            }
            else {
                inputHandler.setActionState(BattleInputHandler.ActionState.IDLE);
            }
        }
        if(isBattleOver()){ handleBattleEnd(); } // Check if a support skill somehow ended the battle
    }

    public void executeAoeSkill(BattleCharacter caster, Skill skill, Vector2 centerTile) {
        if (battleEnded) return;
        // Mana cost check
        if (caster.getSourceCharacter().getManaPoints() < skill.getManaCost()) {
            uiManager.updateBattleLog(caster.getName() + " does not have enough mana for " + skill.getName() + "!");
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE); // Reset state
            return;
        }
        caster.getSourceCharacter().setManaPoints(caster.getSourceCharacter().getManaPoints() - skill.getManaCost());

        Gdx.app.log("BattleScreen", caster.getName() + " attempts " + skill.getName() + " centered at " + centerTile);

        List<IBattleActor> affectedTargets = new ArrayList<>();
        Array<Vector2> aoeTiles = new Array<>(); // Calculate AoE tiles as before
        for (int x = 0; x < mapWidthInTiles; x++) { /* ... populate aoeTiles ... */ }

        for (Vector2 affectedTilePos : aoeTiles) {
            IBattleActor victim = getActorAtTile(affectedTilePos.x, affectedTilePos.y);
            if (victim != null && victim.isAlive()) {
                // Add logic to differentiate between hitting enemies/allies if skill requires
                if (victim instanceof Enemy || (victim != caster && skill.getType() != SkillType.HEAL)) { // Example: Don't hit self unless it's a heal
                    affectedTargets.add(victim);
                }
            }
        }
        skill.performExecution(caster, affectedTargets, this); // DELEGATE HERE

        caster.setHasPerformedMajorAction(true);

        // Clear skill targeting highlights but recalculate movement if there's still movement points left
        inputHandler.clearSkillHighlights(); // Or clearAllHighlights
        if (caster.getRemainingMovement() > 0) {
            inputHandler.calculateMovementReachableTiles(caster);
            inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
        } else {
            inputHandler.clearAllHighlights(); // Ensure all are cleared if no move left
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE);
        }
        uiManager.updateSkillButtons(caster);
        uiManager.updateTurnInfo(caster);
        if(isBattleOver()){ handleBattleEnd(); }
    }

    private void renderHighlights() {
        if (shapeRenderer == null || battleEnded) return;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        BattleInputHandler.ActionState currentState = inputHandler.getCurrentActionState();
        Skill currentSkill = inputHandler.getSelectedSkill(); // Keep this for AoE check
        Vector2 currentAoeCenter = inputHandler.getAoeCenterTile(); // Keep this for AoE check

        // Movement tiles (Normal Movement)
        if (currentState == BattleInputHandler.ActionState.IDLE || currentState == BattleInputHandler.ActionState.MOVING) {
            shapeRenderer.setColor(0.3f, 0.5f, 1f, 0.3f); // Light blue
            for (Vector2 tilePos : inputHandler.getMovementReachableTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }

        // Nimble Movement tiles
        if (currentState == BattleInputHandler.ActionState.TARGETING_NIMBLE_MOVEMENT) {
            shapeRenderer.setColor(0.2f, 0.8f, 0.8f, 0.35f); // Teal for nimble movement
            for (Vector2 tilePos : inputHandler.getNimbleMovementTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }

        // Skill Range tiles (for attacks, targeted support, AoE placement)
        if (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_TILE || currentState == BattleInputHandler.ActionState.TARGETING_SKILL_ACTOR) {
            shapeRenderer.setColor(0, 1f, 0, 0.25f); // Green
            for (Vector2 tilePos : inputHandler.getSkillRangeTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }

        // Skill AoE tiles (preview/confirmation)
        if (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_AOE_CONFIRM ||
            (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_TILE && currentSkill != null && currentSkill.getAoeRadius() > 0 && currentAoeCenter != null)) {
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.35f); // Orange
            for (Vector2 tilePos : inputHandler.getSkillAoeTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        if (!battleEnded) {
            renderHighlights();
        }

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        IBattleActor currentActor = turnManager.getCurrentActor(); // Get current actor for highlighting
        
        // First pass: Draw all character/enemy sprites
        for (IBattleActor actor : turnManager.getTurnOrder()) {
            if (actor.isAlive() && actor.getBattleSprite() != null && actor.getBattleMapPosition() != null) {
                Vector2 pos = actor.getBattleMapPosition();
                float worldX = pos.x * tileWidth;
                float worldY = pos.y * tileHeight;
                
                // Set color based on actor type and turn status
                if (actor == currentActor && !battleEnded) {
                    // Current actor gets highlighted with a brighter shade
                    if (actor instanceof BattleCharacter) {
                        game.batch.setColor(1f, 1f, 0.3f, 1f); // Bright yellow for current player
                    } else if (actor instanceof Enemy) {
                        game.batch.setColor(1f, 0.3f, 0.3f, 1f); // Bright red for current enemy
                    }
                } else {
                    // Non-current actors get normal coloring
                    if (actor instanceof Enemy) {
                        game.batch.setColor(0.9f, 0.4f, 0.4f, 1f); // Red tint for all enemies
                    } else {
                        game.batch.setColor(Color.WHITE); // Normal color for allies
                    }
                }
                
                // Draw the sprite
                game.batch.draw(actor.getBattleSprite(), worldX, worldY, tileWidth, tileHeight);
            }
        }
        
        // Second pass: Draw turn indicator and status effects above characters
        for (IBattleActor actor : turnManager.getTurnOrder()) {
            if (actor.isAlive() && actor.getBattleMapPosition() != null) {
                Vector2 pos = actor.getBattleMapPosition();
                float worldX = pos.x * tileWidth;
                float worldY = pos.y * tileHeight;
                
                // Reset color to white for drawing indicators
                game.batch.setColor(Color.WHITE);
                
                // Draw active turn indicator (circle or arrow)
                if (actor == currentActor && !battleEnded) {
                    if (actor instanceof BattleCharacter) {
                        // Draw a small rotation arrow above the character
                        TextureRegion turnIndicator = getTurnIndicator(true);
                        if (turnIndicator != null) {
                            game.batch.draw(
                                turnIndicator,
                                worldX + tileWidth/2 - 8, // Center the 16x16 indicator
                                worldY + tileHeight + 4,  // Position above character
                                16, 16                    // Size of indicator
                            );
                        }
                    } else if (actor instanceof Enemy) {
                        // Draw a red rotation arrow for enemy turns
                        TextureRegion turnIndicator = getTurnIndicator(false);
                        if (turnIndicator != null) {
                            game.batch.draw(
                                turnIndicator, 
                                worldX + tileWidth/2 - 8,
                                worldY + tileHeight + 4,
                                16, 16
                            );
                        }
                    }
                }
                
                // Draw status effect icons
                drawStatusEffects(actor, worldX, worldY);
            }
        }
        
        game.batch.setColor(Color.WHITE); // Reset color
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // Enemy AI Turn - Check using TurnManager's current actor
        currentActor = turnManager.getCurrentActor(); // Re-get in case it changed during player actions
        if (!battleEnded && currentActor instanceof Enemy && !currentActor.hasPerformedMajorAction()) {
            Enemy enemy = (Enemy) currentActor;
            enemy.performSimpleAI(turnManager.getTurnOrder(), this);
            // Check for battle end immediately after AI action, before advancing turn via main logic
            if(isBattleOver()){
                 handleBattleEnd();
            } else {
                // Advance turn only if the battle didn't end during the AI's action
                advanceTurn();
            }
        }
        
        // Handle hover detection for character info popups
        if (!battleEnded) {
            handleHoverDetection();
        }
    }
    
    /**
     * Draws status effect icons above the actor
     */
    private void drawStatusEffects(IBattleActor actor, float worldX, float worldY) {
        List<StatusEffect> effects = null;
        
        if (actor instanceof BattleCharacter) {
            effects = ((BattleCharacter)actor).getActiveEffects();
        } else if (actor instanceof Enemy) {
            effects = ((Enemy)actor).getActiveEffects();
        }
        
        if (effects != null && !effects.isEmpty()) {
            // Position status icons in a row above the character
            float iconSize = 12; // Size of each status icon
            float spacing = 2;   // Spacing between icons
            float totalWidth = effects.size() * (iconSize + spacing) - spacing;
            float startX = worldX + (tileWidth - totalWidth) / 2;
            float iconY = worldY + tileHeight + 2;
            
            // Draw each status effect icon
            for (int i = 0; i < effects.size(); i++) {
                StatusEffect effect = effects.get(i);
                TextureRegion effectIcon = getStatusEffectIcon(effect.getType());
                
                if (effectIcon != null) {
                    float iconX = startX + i * (iconSize + spacing);
                    game.batch.draw(effectIcon, iconX, iconY, iconSize, iconSize);
                }
            }
        }
    }
    
    /**
     * Gets the appropriate icon for a status effect type
     */
    private TextureRegion getStatusEffectIcon(String effectType) {
        // Check if we already have this texture
        String key = "status_" + effectType.toLowerCase();
        
        TextureRegion region = game.getDynamicTexture(key);
        
        // If not found, create a new one
        if (region == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            
            // Choose color based on effect type
            switch (effectType) {
                case "INVISIBLE":
                    pixmap.setColor(0, 0.8f, 0.8f, 1); // Cyan
                    break;
                case "BURN":
                    pixmap.setColor(0.8f, 0.2f, 0, 1); // Orange-red
                    break;
                case "STUN":
                    pixmap.setColor(0.8f, 0.8f, 0, 1); // Yellow
                    break;
                case "SLOW":
                    pixmap.setColor(0, 0, 0.8f, 1); // Blue
                    break;
                case "DAMAGE_REDUCTION":
                    pixmap.setColor(0, 0.8f, 0, 1); // Green
                    break;
                case "NIMBLE_MOVEMENT_ACTIVE":
                    pixmap.setColor(0.8f, 0, 0.8f, 1); // Purple
                    break;
                default:
                    pixmap.setColor(0.7f, 0.7f, 0.7f, 1); // Gray
                    break;
            }
            
            pixmap.fillCircle(8, 8, 6);
            Texture iconTexture = new Texture(pixmap);
            pixmap.dispose();
            
            // Create a new region
            region = new TextureRegion(iconTexture);
            
            // Store for reuse
            game.addDynamicTexture(key, region);
        }
        
        return region;
    }
    
    /**
     * Handles detection of mouse hover over characters/enemies
     */
    private void handleHoverDetection() {
        // Get mouse position
        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();
        
        // Convert to world coordinates
        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int)(worldCoords.x / tileWidth);
        int tileY = (int)(worldCoords.y / tileHeight);
        
        // Check if hovering over an actor
        IBattleActor hoveredActor = getActorAtTile(tileX, tileY);
        
        if (hoveredActor != null && hoveredActor.isAlive()) {
            // Show character info popup
            uiManager.showCharacterInfoPopup(hoveredActor, worldCoords.x, worldCoords.y);
        } else {
            // Hide popup if not hovering over a character
            uiManager.hideCharacterInfoPopup();
        }
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("BattleScreen", "Resizing to " + width + "x" + height);
        float viewportPixelWidth = VIEWPORT_WIDTH_IN_TILES * tileWidth;
        float viewportPixelHeight = VIEWPORT_HEIGHT_IN_TILES * tileHeight;
        camera.viewportWidth = viewportPixelWidth;
        camera.viewportHeight = viewportPixelHeight;
        camera.update();
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() { Gdx.app.log("BattleScreen", "BattleScreen paused.");}
    @Override
    public void resume() { Gdx.app.log("BattleScreen", "BattleScreen resumed.");}

    @Override
    public void hide() {
        Gdx.app.log("BattleScreen", "Hiding BattleScreen.");
        if (inputMultiplexer != null) Gdx.input.setInputProcessor(null);
        // Clean up timer if screen is hidden abruptly to prevent issues
        Timer.instance().clear();
    }

    @Override
    public void dispose() {
        Gdx.app.log("BattleScreen", "Disposing BattleScreen.");
        if (map != null) map.dispose();
        if (mapRenderer instanceof OrthogonalTiledMapRenderer) ((OrthogonalTiledMapRenderer) mapRenderer).dispose();
        if (stage != null) stage.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        Timer.instance().clear(); // Clear any pending timers
    }

    public void handleCharacterFreeMove(BattleCharacter caster, float tileX, float tileY, Skill skillUsed) {
        if (battleEnded || caster == null || skillUsed == null) return;

        Gdx.app.log("BattleScreen", caster.getName() + " uses " + skillUsed.getName() + " to freely move to " + tileX + "," + tileY);
        caster.setBattleMapPosition(tileX, tileY);

        // Apply the status effect from the skill to mark it as "used" for this turn
        if (skillUsed.getStatusEffects() != null && !skillUsed.getStatusEffects().isEmpty()) {
            // Assuming the first status effect is the NIMBLE_MOVEMENT_ACTIVE one
            StatusEffect effectToApply = skillUsed.getStatusEffects().get(0).copy();
            caster.addStatusEffect(effectToApply);
            Gdx.app.log("BattleScreen", "Applied " + effectToApply.getType() + " to " + caster.getName() + " after free move.");
        }

        // This is a FREE move, so it does NOT setHasPerformedMajorAction(true).

        inputHandler.clearAllHighlights();
        uiManager.updateSkillButtons(caster); // Nimble Movement button might appear disabled/used
        uiManager.updateTurnInfo(caster);

        // After free move, player can still do normal move or major action.
        // Transition to MOVING if movement points remain and no major action taken, otherwise to IDLE.
        if (caster.getRemainingMovement() > 0 && !caster.hasPerformedMajorAction()) {
            inputHandler.calculateMovementReachableTiles(caster);
            inputHandler.setActionState(BattleInputHandler.ActionState.MOVING);
        } else {
            inputHandler.setActionState(BattleInputHandler.ActionState.IDLE);
        }
    }

    public boolean isTargetAdjacentToAlly(BattleCharacter caster, IBattleActor target) {
        if (caster == null || target == null || !(target instanceof Enemy)) {
            return false;
        }
        Vector2 targetPos = target.getBattleMapPosition();

        // Iterate through all actors in the turn order
        for (IBattleActor potentialAlly : turnManager.getTurnOrder()) {
            // An ally must be a BattleCharacter, alive, and not the caster themselves
            if (potentialAlly instanceof BattleCharacter && potentialAlly.isAlive() && potentialAlly != caster) {
                Vector2 allyPos = potentialAlly.getBattleMapPosition();
                // Check for orthogonal adjacency (Manhattan distance of 1)
                if (Math.abs(targetPos.x - allyPos.x) + Math.abs(targetPos.y - allyPos.y) == 1) {
                    Gdx.app.debug("BattleScreen", target.getName() + " at " + targetPos + " is adjacent to ally " + potentialAlly.getName() + " at " + allyPos);
                    return true; // Found an ally adjacent to the target
                }
            }
        }
        return false; // No ally found adjacent to the target
    }

    /**
     * Creates or retrieves a turn indicator based on whether it's for a player or enemy
     */
    private TextureRegion getTurnIndicator(boolean isPlayerTurn) {
        String key = isPlayerTurn ? "turn_indicator_player" : "turn_indicator_enemy";
        
        TextureRegion region = game.getDynamicTexture(key);
        
        // If not found, create a new one
        if (region == null) {
            com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(16, 16, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
            pixmap.setColor(isPlayerTurn ? Color.YELLOW : Color.RED);
            
            // Draw a simple arrow pointing down
            pixmap.fillTriangle(8, 2, 2, 10, 14, 10);
            pixmap.fillRectangle(6, 8, 4, 6);
            
            Texture iconTexture = new Texture(pixmap);
            pixmap.dispose();
            
            // Create a new region
            region = new TextureRegion(iconTexture);
            
            // Store for reuse
            game.addDynamicTexture(key, region);
        }
        
        return region;
    }

}
