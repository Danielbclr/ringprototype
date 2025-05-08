package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.Timer; // Import Timer for delayed screen transition
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.input.BattleInputHandler;
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.battle.Enemy;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.resources.ResourceType; // Import ResourceType for rewards
import com.danbramos.ringprototype.screens.ui.BattleUiManager;

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

    // Turn Management
    private Array<IBattleActor> turnOrder;
    private int currentTurnIndex;
    private IBattleActor currentTurnActor;
    private boolean battleEnded = false; // Flag to prevent multiple end-game logic calls

    // Managers
    private BattleUiManager uiManager;
    private BattleInputHandler inputHandler;

    private InputMultiplexer inputMultiplexer;

    public BattleScreen(RingPrototypeGame game) {
        this.game = game;
        this.turnOrder = new Array<>();
    }

    public int getMapWidthInTiles() {
        return mapWidthInTiles;
    }

    public int getMapHeightInTiles() {
        return mapHeightInTiles;
    }

    @Override
    public void show() {
        Gdx.app.log("BattleScreen", "Showing BattleScreen.");
        battleEnded = false; // Reset battle end flag
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

        initializeTurnSystem();
    }

    private void initializeTurnSystem() {
        turnOrder.clear();
        for (Character character : game.partyManager.getMembers()) {
            if (character != null && character.getHealthPoints() > 0) { // Only add living characters
                turnOrder.add(new BattleCharacter(character));
            }
        }
        for (Enemy enemy : game.currentBattleEnemies) {
            if (enemy != null && enemy.isAlive()) {
                turnOrder.add(enemy);
            }
        }

        if (turnOrder.size > 0) {
            // Simple initiative: players first, then enemies (can be expanded)
            turnOrder.sort((a1, a2) -> {
                if (a1 instanceof BattleCharacter && a2 instanceof Enemy) return -1;
                if (a1 instanceof Enemy && a2 instanceof BattleCharacter) return 1;
                return 0; // Or sort by a stat like speed/initiative
            });

            currentTurnIndex = 0;
            currentTurnActor = turnOrder.get(currentTurnIndex);
            startTurnFor(currentTurnActor);
        } else {
            currentTurnActor = null;
            Gdx.app.error("BattleScreen", "No combatants to start battle turns.");
            uiManager.updateTurnInfo(null);
            handleBattleEnd(); // If no one to fight, battle ends immediately
        }
    }

    private void startTurnFor(IBattleActor actor) {
        if (actor == null || battleEnded) return;
        inputHandler.resetState();

        actor.startTurn();
        Gdx.app.log("BattleScreen", "Starting turn for: " + actor.getName());
        uiManager.updateSkillButtons(actor);

        if (actor instanceof BattleCharacter) {
            BattleCharacter bc = (BattleCharacter) actor;
            if (!bc.hasPerformedMajorAction()) {
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

        if (currentTurnActor != null) {
            currentTurnActor.endTurn();
        }
        inputHandler.resetState();

        for (int i = turnOrder.size - 1; i >= 0; i--) {
            IBattleActor actor = turnOrder.get(i);
            if (!actor.isAlive()) {
                Gdx.app.log("BattleScreen", actor.getName() + " is defeated, removing from turn order.");
                turnOrder.removeIndex(i);
                if (actor == currentTurnActor) {
                    currentTurnActor = null;
                }
            }
        }

        if (isBattleOver()) {
            Gdx.app.log("BattleScreen", "Battle Over condition met.");
            handleBattleEnd();
            return;
        }

        if (turnOrder.isEmpty()) { // Should be caught by isBattleOver, but as a safeguard
            Gdx.app.error("BattleScreen", "Turn order became empty unexpectedly.");
            handleBattleEnd();
            return;
        }

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size;
        currentTurnActor = turnOrder.get(currentTurnIndex);
        startTurnFor(currentTurnActor);
    }

    public boolean isTileWithinMapBounds(float tileX, float tileY) {
        return tileX >= 0 && tileX < mapWidthInTiles && tileY >= 0 && tileY < mapHeightInTiles;
    }

    public boolean isBattleOver() {
        if (turnOrder.isEmpty() && !battleEnded) return true; // No one left

        boolean playersAlive = false;
        boolean enemiesAlive = false;
        for (IBattleActor actor : turnOrder) {
            if (actor.isAlive()) {
                if (actor instanceof BattleCharacter) {
                    playersAlive = true;
                } else if (actor instanceof Enemy) {
                    enemiesAlive = true;
                }
            }
        }
        return (!playersAlive || !enemiesAlive) && !battleEnded;
    }

    private void handleBattleEnd() {
        if (battleEnded) return; // Prevent this from running multiple times
        battleEnded = true;

        Gdx.app.log("BattleScreen", "Handling battle end.");
        currentTurnActor = null; // Stop further actions
        inputHandler.resetState();
        uiManager.setEndTurnButtonDisabled(true);
        uiManager.clearSkillButtons();

        boolean playersWon = false;
        int livingPlayerCount = 0;
        for(IBattleActor actor : turnOrder) {
            if(actor instanceof BattleCharacter && actor.isAlive()) {
                livingPlayerCount++;
            }
        }
        // Check if any enemies are still alive
        boolean enemiesStillAlive = false;
        for(IBattleActor actor : turnOrder) {
            if(actor instanceof Enemy && actor.isAlive()) {
                enemiesStillAlive = true;
                break;
            }
        }

        if (livingPlayerCount > 0 && !enemiesStillAlive) {
            playersWon = true;
        }


        if(playersWon) {
            Gdx.app.log("BattleScreen", "PLAYER VICTORY!");
            uiManager.updateTurnInfo(null); // Clear turn info
            // Update UI to show "Victory!"
            // You might want a dedicated label for this in BattleUiManager
            // For now, let's assume uiManager.updateTurnInfo can show a custom message
            if (uiManager != null) { // Check if uiManager is initialized
                // A bit of a hack, ideally BattleUiManager has a method for this
                if (uiManager.turnInfoLabel != null) uiManager.turnInfoLabel.setText("VICTORY!");
            }


            // Apply XP, loot, etc.
            for(IBattleActor actor : turnOrder) { // Iterate original turn order to find player characters
                if(actor instanceof BattleCharacter) {
                    BattleCharacter bc = (BattleCharacter) actor;
                    if(bc.isAlive()){ // Only apply to survivors
                        bc.applyEndOfBattleState();
                        Gdx.app.log("BattleScreen", bc.getName() + " survived. HP updated.");
                    }
                }
            }

            // Award resources
            int goldReward = 50 + (int)(Math.random() * 51); // 50-100 gold
            int foodReward = 2 + (int)(Math.random() * 4);   // 2-5 food
            game.resourceManager.addResource(ResourceType.GOLD, goldReward);
            game.resourceManager.addResource(ResourceType.FOOD, foodReward);
            Gdx.app.log("BattleScreen", "Awarded " + goldReward + " Gold and " + foodReward + " Food.");


            // Transition back to MapScreen after a delay
            Timer.schedule(new Timer.Task() {
                @Override
                public void run() {
                    game.setScreen(new MapScreen(game));
                }
            }, 3); // 3 second delay
        } else {
            Gdx.app.log("BattleScreen", "PLAYER DEFEAT - GAME OVER!");
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
        return currentTurnActor;
    }

    public boolean isTileOccupied(float tileX, float tileY) {
        for (IBattleActor actor : turnOrder) {
            if (actor.isAlive() && actor.getBattleMapPosition().epsilonEquals(tileX, tileY)) {
                return true;
            }
        }
        return false;
    }

    public boolean isTileOccupiedByAlly(float tileX, float tileY, IBattleActor askingActor) {
        for (IBattleActor actorOnTile : turnOrder) {
            if (actorOnTile.isAlive() && actorOnTile != askingActor && actorOnTile.getBattleMapPosition().epsilonEquals(tileX, tileY)) {
                if (askingActor instanceof Enemy && actorOnTile instanceof Enemy) {
                    return true;
                }
            }
        }
        return false;
    }

    public IBattleActor getActorAtTile(float tileX, float tileY) {
        for (IBattleActor actor : turnOrder) {
            if (actor.isAlive() && actor.getBattleMapPosition().epsilonEquals(tileX, tileY)) {
                return actor;
            }
        }
        return null;
    }

    public void selectSkill(Skill skill) {
        if (battleEnded) return;
        if (currentTurnActor instanceof BattleCharacter) {
            inputHandler.selectSkill(skill, (BattleCharacter) currentTurnActor);
        }
    }

    public void handleCharacterMove(BattleCharacter mover, float tileX, float tileY) {
        if (battleEnded) return;
        Gdx.app.log("BattleScreen", mover.getName() + " moving to " + tileX + "," + tileY);
        mover.setBattleMapPosition(tileX, tileY);
        mover.setHasPerformedMajorAction(true);
        inputHandler.clearAllHighlights();
        uiManager.updateSkillButtons(mover);
        uiManager.updateTurnInfo(mover);
        inputHandler.resetState();
    }

    public void executeSingleTargetSkill(BattleCharacter caster, Skill skill, IBattleActor target) {
        if (battleEnded) return;
        Gdx.app.log("BattleScreen", caster.getName() + " uses " + skill.getName() + " on " + target.getName());
        int damage = skill.rollDamage();
        Gdx.app.log("BattleScreen", skill.getName() + " deals " + damage + " damage.");
        target.takeDamage(damage);

        caster.setHasPerformedMajorAction(true);
        inputHandler.resetState();
        uiManager.updateSkillButtons(caster);
        uiManager.updateTurnInfo(caster);

        if (!target.isAlive()) {
            Gdx.app.log("BattleScreen", target.getName() + " has been defeated!");
        }
        // Check for battle end after action
        if(isBattleOver()){
            handleBattleEnd();
        }
    }

    public void executeAoeSkill(BattleCharacter caster, Skill skill, Vector2 centerTile) {
        if (battleEnded) return;
        Gdx.app.log("BattleScreen", caster.getName() + " uses " + skill.getName() + " centered at " + centerTile);

        Array<Vector2> aoeTiles = new Array<>();
        for (int x = 0; x < mapWidthInTiles; x++) {
            for (int y = 0; y < mapHeightInTiles; y++) {
                double distSq = Math.pow(x - centerTile.x, 2) + Math.pow(y - centerTile.y, 2);
                if (distSq <= Math.pow(skill.getAoeRadius(), 2)) {
                    aoeTiles.add(new Vector2(x, y));
                }
            }
        }

        for (Vector2 affectedTile : aoeTiles) {
            IBattleActor victim = getActorAtTile(affectedTile.x, affectedTile.y);
            if (victim != null && victim.isAlive()) {
                if (victim instanceof Enemy || victim != caster) {
                    int damage = skill.rollDamage();
                    Gdx.app.log("BattleScreen", skill.getName() + " hits " + victim.getName() + " for " + damage + " damage.");
                    victim.takeDamage(damage);
                    if (!victim.isAlive()) {
                        Gdx.app.log("BattleScreen", victim.getName() + " has been defeated by AoE!");
                    }
                }
            }
        }
        caster.setHasPerformedMajorAction(true);
        inputHandler.resetState();
        uiManager.updateSkillButtons(caster);
        uiManager.updateTurnInfo(caster);
        // Check for battle end after action
        if(isBattleOver()){
            handleBattleEnd();
        }
    }

    private void renderHighlights() {
        if (shapeRenderer == null || battleEnded) return; // Don't render highlights if battle ended
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        BattleInputHandler.ActionState currentState = inputHandler.getCurrentActionState();
        Skill currentSkill = inputHandler.getSelectedSkill();
        Vector2 currentAoeCenter = inputHandler.getAoeCenterTile();

        if (currentState == BattleInputHandler.ActionState.IDLE || currentState == BattleInputHandler.ActionState.MOVING) {
            shapeRenderer.setColor(0.3f, 0.5f, 1f, 0.3f);
            for (Vector2 tilePos : inputHandler.getMovementReachableTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }

        if (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_TILE || currentState == BattleInputHandler.ActionState.TARGETING_SKILL_ACTOR) {
            shapeRenderer.setColor(0, 1f, 0, 0.25f);
            for (Vector2 tilePos : inputHandler.getSkillRangeTiles()) {
                shapeRenderer.rect(tilePos.x * tileWidth, tilePos.y * tileHeight, tileWidth, tileHeight);
            }
        }

        if (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_AOE_CONFIRM ||
            (currentState == BattleInputHandler.ActionState.TARGETING_SKILL_TILE && currentSkill != null && currentSkill.getAoeRadius() > 0 && currentAoeCenter != null)) {
            shapeRenderer.setColor(1f, 0.5f, 0f, 0.35f);
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

        if (!battleEnded) { // Only render highlights if battle is ongoing
            renderHighlights();
        }

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (IBattleActor actor : turnOrder) { // Still render actors even if battle ended for a moment
            if (actor.isAlive() && actor.getBattleSprite() != null && actor.getBattleMapPosition() != null) {
                Vector2 pos = actor.getBattleMapPosition();
                float worldX = pos.x * tileWidth;
                float worldY = pos.y * tileHeight;
                if (currentTurnActor == actor && !battleEnded) { // Only highlight current turn if battle ongoing
                    if (actor instanceof BattleCharacter) game.batch.setColor(Color.YELLOW);
                    else if (actor instanceof Enemy) game.batch.setColor(Color.RED);
                }
                game.batch.draw(actor.getBattleSprite(), worldX, worldY, tileWidth, tileHeight);
                if (currentTurnActor == actor && !battleEnded) game.batch.setColor(Color.WHITE);
            }
        }
        game.batch.end();

        stage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        stage.draw();

        // Enemy AI Turn
        if (!battleEnded && currentTurnActor instanceof Enemy && !currentTurnActor.hasPerformedMajorAction()) {
            Enemy enemy = (Enemy) currentTurnActor;
            enemy.performSimpleAI(turnOrder, this);
            // Check for battle end after AI action
            if(isBattleOver()){
                handleBattleEnd();
            } else {
                advanceTurn();
            }
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
}
