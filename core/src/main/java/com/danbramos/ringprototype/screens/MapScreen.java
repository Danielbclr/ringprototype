package com.danbramos.ringprototype.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.danbramos.ringprototype.RingPrototypeGame;
import com.danbramos.ringprototype.input.MapInputHandler;
import com.danbramos.ringprototype.quests.Quest;
import com.danbramos.ringprototype.quests.QuestManager;
import com.danbramos.ringprototype.resources.ResourceType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class MapScreen implements Screen {
    private final RingPrototypeGame game;
    private TiledMap map;
    private TiledMapRenderer mapRenderer;
    private OrthographicCamera camera;
    private MapInputHandler inputHandler;

    private int tileWidth;
    private int tileHeight;
    private int mapWidthInTiles;    // Declare as instance field
    private int mapHeightInTiles;   // Declare as instance field

    // UI for Resources
    private Stage uiStage;
    private Skin skin;
    private Table resourceTable;
    private EnumMap<ResourceType, Label> resourceLabels;
    
    // Quest giver management
    private Map<String, Vector2> questGiverPositions;
    private Map<String, TextureRegion> questGiverSprites;


    public MapScreen(RingPrototypeGame game) {
        this.game = game;
        this.skin = game.skin; // Get skin from the main game class
        this.questGiverPositions = new HashMap<>();
        this.questGiverSprites = new HashMap<>();
    }

    @Override
    public void show() {
        Gdx.app.log("MapScreen", "Showing MapScreen.");
        map = new TmxMapLoader().load("tilemaps/overworld.tmx"); // Ensure this is your map file

        if (map.getProperties().containsKey("tilewidth") && map.getProperties().containsKey("tileheight")) {
            tileWidth = map.getProperties().get("tilewidth", Integer.class);
            tileHeight = map.getProperties().get("tileheight", Integer.class);
        } else if (map.getLayers().getCount() > 0 && map.getLayers().get(0) instanceof TiledMapTileLayer) {
            TiledMapTileLayer firstLayer = (TiledMapTileLayer) map.getLayers().get(0);
            tileWidth = (int) firstLayer.getTileWidth();
            tileHeight = (int) firstLayer.getTileHeight();
        } else {
            tileWidth = 16; // Default if not found
            tileHeight = 16;
            Gdx.app.error("MapScreen", "Could not determine tile size from map, using default 16x16.");
        }

        // Initialize instance fields
        this.mapWidthInTiles = map.getProperties().get("width", Integer.class);
        this.mapHeightInTiles = map.getProperties().get("height", Integer.class);
        Gdx.app.log("MapScreen", "MapScreen initialized. Map dimensions: " + this.mapWidthInTiles + "x" + this.mapHeightInTiles + " tiles.");


        mapRenderer = new OrthogonalTiledMapRenderer(map, 1f / tileWidth); // Adjust unit scale if needed
        camera = new OrthographicCamera();
        // Set camera to view a certain number of tiles
        float viewportWidthInTiles = 30f;
        float viewportHeightInTiles = 20f;
        camera.setToOrtho(false, viewportWidthInTiles, viewportHeightInTiles);
        camera.position.set(viewportWidthInTiles / 2f, viewportHeightInTiles / 2f, 0); // Center camera
        camera.update();

        inputHandler = new MapInputHandler(this, game); // 'this' refers to the MapScreen instance

        Gdx.input.setInputProcessor(inputHandler);

        // Initialize UI Stage and Resource Display
        uiStage = new Stage(new ScreenViewport());
        resourceLabels = new EnumMap<>(ResourceType.class);
        setupResourceUI();
        
        // Initialize quest givers
        loadQuestGivers();

        // Add uiStage to the input processor if it has interactive elements,
        // or ensure MapInputHandler doesn't consume all input if UI needs clicks.
        // For now, resource display is passive. If you add buttons to it, you'll need an InputMultiplexer.
        // Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, inputHandler));
        // For now, inputHandler is fine as primary.
    }
    
    /**
     * Load quest givers from the QuestManager
     */
    private void loadQuestGivers() {
        questGiverPositions = QuestManager.getInstance().getQuestGiverPositions();
        
        // Create sprites for each quest giver
        for (String questId : questGiverPositions.keySet()) {
            Quest quest = QuestManager.getInstance().getQuest(questId);
            if (quest != null) {
                TextureRegion sprite = QuestManager.getInstance().createQuestGiverSprite(quest, game.characterSheet);
                if (sprite != null) {
                    questGiverSprites.put(questId, sprite);
                    Gdx.app.log("MapScreen", "Loaded quest giver sprite for: " + quest.getTitle());
                }
            }
        }
    }

    public void attemptCharacterMove(int deltaX, int deltaY) {
        if (game.partyManager == null) {
            Gdx.app.error("MapScreen", "PartyManager is null, cannot move character.");
            return;
        }

        Vector2 currentPartyPosition = game.partyManager.getMapPosition();
        if (currentPartyPosition == null) {
            Gdx.app.error("MapScreen", "Party map position is null, cannot move character.");
            return;
        }

        float newX = currentPartyPosition.x + deltaX;
        float newY = currentPartyPosition.y + deltaY;

        // Basic boundary check using instance fields
        if (newX >= 0 && newX < this.mapWidthInTiles && newY >= 0 && newY < this.mapHeightInTiles) {
            // Check if the position contains a quest giver
            String questGiverId = getQuestGiverAtPosition(newX, newY);
            if (questGiverId != null) {
                // Interact with quest giver instead of moving
                interactWithQuestGiver(questGiverId);
                return;
            }
            
            // TODO: Add more sophisticated collision/traversability check here
            // For example, check a property of the TiledMapTileLayer:
            // TiledMapTileLayer collisionLayer = (TiledMapTileLayer) map.getLayers().get("CollisionLayerName");
            // if (collisionLayer != null) {
            //     Cell cell = collisionLayer.getCell((int)newX, (int)newY);
            //     if (cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey("blocked")) {
            //         Gdx.app.log("MapScreen", "Move blocked by collision layer at " + newX + "," + newY);
            //         return; // Do not move
            //     }
            // }

            game.partyManager.setMapPosition(newX, newY);
            Gdx.app.log("MapScreen", "Character moved to: " + newX + ", " + newY);

            // Optional: Camera follow logic
            // If the character moves, you might want the camera to follow.
            // A simple way is to keep the character in the center,
            // or pan when they get close to the edge of the viewport.
            // For now, we'll let the camera be manually controlled or fixed.
            // updateCameraPosition(newX, newY);

        } else {
            Gdx.app.log("MapScreen", "Move to " + newX + "," + newY + " is out of map bounds.");
        }
    }
    
    /**
     * Check if there's a quest giver at the given position
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The quest ID if there's a quest giver, null otherwise
     */
    private String getQuestGiverAtPosition(float x, float y) {
        for (Map.Entry<String, Vector2> entry : questGiverPositions.entrySet()) {
            if (entry.getValue().epsilonEquals(x, y)) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    /**
     * Interact with a quest giver
     * @param questId The ID of the quest given by this NPC
     */
    private void interactWithQuestGiver(String questId) {
        Gdx.app.log("MapScreen", "Interacting with quest giver for quest: " + questId);
        
        // Open the dialog screen for this quest
        game.setScreen(new DialogScreen(game, questId));
    }

    private Label createLabel(String text, String styleName) {
        if (skin.has(styleName, Label.LabelStyle.class)) {
            return new Label(text, skin, styleName);
        } else if (skin.has("default", Label.LabelStyle.class)) {
            Gdx.app.log("MapScreen", "LabelStyle '" + styleName + "' not found. Falling back to 'default'.");
            return new Label(text, skin, "default");
        } else {
            Gdx.app.error("MapScreen", "LabelStyle '" + styleName + "' and 'default' not found. Creating programmatic fallback.");
            Label.LabelStyle programmaticStyle = new Label.LabelStyle();
            programmaticStyle.font = skin.getFont("default-font"); // Assumes "default-font" exists
            if (programmaticStyle.font == null) programmaticStyle.font = new BitmapFont(); // Absolute fallback
            programmaticStyle.fontColor = Color.WHITE;
            return new Label(text, programmaticStyle);
        }
    }

    private void setupResourceUI() {
        resourceTable = new Table();
        resourceTable.setFillParent(true);
        resourceTable.top().left().pad(10); // Position top-left

        for (ResourceType type : ResourceType.values()) {
            Label nameLabel = createLabel(type.getDisplayName() + ": ", "default"); // Using "default" style
            Label valueLabel = createLabel("0", "default");
            resourceLabels.put(type, valueLabel);

            resourceTable.add(nameLabel).align(Align.left);
            resourceTable.add(valueLabel).width(50).align(Align.left); // Give some width for the number
            resourceTable.row(); // New row for the next resource
        }
        uiStage.addActor(resourceTable);
    }

    private void updateResourceUI() {
        if (game.resourceManager == null || resourceLabels == null) return;

        for (ResourceType type : ResourceType.values()) {
            Label valueLabel = resourceLabels.get(type);
            if (valueLabel != null) {
                valueLabel.setText(String.valueOf(game.resourceManager.getResourceAmount(type)));
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1f); // Dark clear color

        // Update camera based on input (handled by MapInputHandler)
        camera.update();
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render party marker or other map elements
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        
        // Draw quest givers
        for (Map.Entry<String, Vector2> entry : questGiverPositions.entrySet()) {
            String questId = entry.getKey();
            Vector2 position = entry.getValue();
            TextureRegion sprite = questGiverSprites.get(questId);
            
            if (sprite != null) {
                // Draw the quest giver
                game.batch.draw(sprite, position.x, position.y, 1f, 1f);
                
                // Draw an exclamation mark or question mark based on quest status
                Quest quest = QuestManager.getInstance().getQuest(questId);
                if (quest != null) {
                    // Draw indicator above quest giver - safely handle missing fonts
                    String indicator = "!"; // Default for new quests
                    if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
                        indicator = "?"; // Question mark for in-progress quests
                    } else if (quest.getStatus() == Quest.QuestStatus.COMPLETED) {
                        indicator = "✓"; // Checkmark for completed quests
                    }
                    
                    // Create a simple text indicator instead of using the font
                    // Draw a colored rectangle to indicate quest status
                    float indicatorX = position.x + 0.7f;
                    float indicatorY = position.y + 0.7f;
                    float indicatorSize = 0.25f;
                    
                    // Set color based on quest status
                    if (quest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
                        game.batch.setColor(Color.YELLOW); // Yellow for new quests
                    } else if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
                        game.batch.setColor(Color.CYAN); // Cyan for in-progress
                    } else {
                        game.batch.setColor(Color.GREEN); // Green for completed
                    }
                    
                    // Draw using a colored portion of the quest giver sprite as an indicator
                    game.batch.draw(sprite, 
                        indicatorX, indicatorY, 
                        indicatorSize, indicatorSize);
                    
                    // Reset color
                    game.batch.setColor(Color.WHITE);
                }
            }
        }
        
        // Ensure PartyManager and its methods exist and are populated
        if (game.partyManager != null && game.partyManager.getPartyMarkerSprite() != null && game.partyManager.getMapPosition() != null) {
            Vector2 partyPos = game.partyManager.getMapPosition();
            // The drawing coordinates need to align with your map's unit scale.
            // The mapRenderer is initialized with `1f / tileWidth`.
            // If partyPos.x and partyPos.y are tile coordinates, drawing at (partyPos.x, partyPos.y)
            // with a width/height of 1 world unit will make the sprite one tile large.
            game.batch.draw(game.partyManager.getPartyMarkerSprite(),
                partyPos.x, // This should be in world units (tile coordinates if unit scale is 1/tileWidth)
                partyPos.y, // This should be in world units
                1f,         // Width in world units (1 tile wide)
                1f);        // Height in world units (1 tile high)
        }
        game.batch.end();

        // Update and draw the UI
        updateResourceUI();
        uiStage.act(Math.min(Gdx.graphics.getDeltaTime(), 1 / 30f));
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        Gdx.app.log("MapScreen", "Resizing to " + width + "x" + height);
        // Adjust camera viewport to maintain aspect ratio or tile visibility
        // This example maintains tile visibility based on initial setup
        float viewportWidthInTiles = camera.viewportWidth;
        float viewportHeightInTiles = camera.viewportHeight;
        camera.setToOrtho(false, viewportWidthInTiles, viewportHeightInTiles);
        // If you want the camera to zoom based on window size, you'd adjust viewportWidth/Height here
        // camera.viewportWidth = (width / (float)tileWidth);
        // camera.viewportHeight = (height / (float)tileHeight);
        camera.update();

        uiStage.getViewport().update(width, height, true); // Update UI viewport
    }

    @Override
    public void pause() {
        Gdx.app.log("MapScreen", "MapScreen paused.");
    }

    @Override
    public void resume() {
        Gdx.app.log("MapScreen", "MapScreen resumed.");
    }

    @Override
    public void hide() {
        Gdx.app.log("MapScreen", "Hiding MapScreen.");
        // Consider if you need to clear the input processor or just let the new screen set it
        // Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        Gdx.app.log("MapScreen", "Disposing MapScreen.");
        if (map != null) map.dispose();
        if (mapRenderer instanceof OrthogonalTiledMapRenderer) ((OrthogonalTiledMapRenderer) mapRenderer).dispose();
        if (uiStage != null) uiStage.dispose();
    }
}
