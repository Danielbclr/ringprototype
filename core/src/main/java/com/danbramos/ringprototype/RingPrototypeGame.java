package com.danbramos.ringprototype;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.party.ClassData;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.party.GameClass;
import com.danbramos.ringprototype.party.PartyManager;
import com.danbramos.ringprototype.party.DefaultPartyManager;
import com.danbramos.ringprototype.screens.MapScreen;
import com.danbramos.ringprototype.items.Item;
import com.danbramos.ringprototype.items.ItemType;
import com.danbramos.ringprototype.battle.Enemy;
import com.danbramos.ringprototype.battle.EnemyData;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.battle.SkillType;
import com.danbramos.ringprototype.quests.QuestManager;
import com.danbramos.ringprototype.resources.ResourceManager; // Import ResourceManager interface
import com.danbramos.ringprototype.resources.DefaultResourceManager; // Import DefaultResourceManager implementation
import com.danbramos.ringprototype.resources.ResourceType;   // Import ResourceType
import com.badlogic.gdx.utils.Array;
import com.danbramos.ringprototype.battle.SkillData;
import com.danbramos.ringprototype.setup.GameSetup; // Import the new setup class

/**
 * Main game class that handles game initialization and resource management
 */
public class RingPrototypeGame extends Game {
    public SpriteBatch batch;
    public AssetManager assetManager;
    public PartyManager partyManager;
    public ResourceManager resourceManager;
    public Skin skin;
    public Texture characterSheet;
    public Array<Enemy> currentBattleEnemies;

    @Override
    public void create() {
        batch = new SpriteBatch();
        assetManager = new AssetManager();
        partyManager = new DefaultPartyManager();
        resourceManager = new DefaultResourceManager();
        currentBattleEnemies = new Array<>();

        // Initialize the data systems first so other systems can use them
        ClassData.getInstance();
        Gdx.app.log("RingPrototypeGame", "ClassData initialized");
        
        EnemyData.getInstance();
        Gdx.app.log("RingPrototypeGame", "EnemyData initialized");
        
        QuestManager.getInstance();
        Gdx.app.log("RingPrototypeGame", "QuestManager initialized");
        
        SkillData skillData = SkillData.getInstance();
        Gdx.app.log("RingPrototypeGame", "SkillData initialized");
        
        // Debug log to verify skills are loaded
        StringBuilder skillsLog = new StringBuilder("Loaded skills: ");
        for (String skillId : skillData.getAllSkills().keySet()) {
            Skill skill = skillData.getSkill(skillId);
            if (skill != null) {
                skillsLog.append(skill.getName()).append(" (").append(skillId).append("), ");
            } else {
                 Gdx.app.error("RingPrototypeGame", "Skill ID '" + skillId + "' returned null skill!");
            }
        }
        Gdx.app.log("RingPrototypeGame", skillsLog.toString());

        // Load assets (basic for now)
        try {
            characterSheet = new Texture(Gdx.files.internal("spritesheets/colored-transparent_packed.png"));
            Gdx.app.log("RingPrototypeGame", "Character sheet loaded successfully.");
        } catch (Exception e) {
            Gdx.app.error("RingPrototypeGame", "Could not load character spritesheet.", e);
        }

        try {
            skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
            Gdx.app.log("RingPrototypeGame", "UI Skin loaded successfully.");
        } catch (Exception e) {
            Gdx.app.error("RingPrototypeGame", "Could not load UI skin. Ensure 'ui/uiskin.json' and its dependencies are in assets.", e);
            skin = createFallbackSkin();
            Gdx.app.log("RingPrototypeGame", "Using fallback UI skin.");
        }
        
        // Initialize game state using the new setup class
        GameSetup gameSetup = new GameSetup();
        gameSetup.initializeInitialResources(resourceManager);
        gameSetup.initializeInitialParty(partyManager, skillData, characterSheet);
        gameSetup.initializePartyPosition(partyManager);

        // Finalize setup specific to RingPrototypeGame
        initializeGameData();

        Gdx.app.log("RingPrototypeGame", "Game created, setting MapScreen.");
        this.setScreen(new MapScreen(this));
    }

    /**
     * Performs final initialization steps after core data and state are loaded.
     * Currently sets the party marker sprite.
     */
    private void initializeGameData() {
        int tileWidth = 16;
        int tileHeight = 16;

        if (characterSheet != null) {
            // Example: Assuming your party marker is a specific sprite on the sheet
            int markerSpriteX = 27; // tile X on spritesheet
            int markerSpriteY = 0; // tile Y on spritesheet
            partyManager.setPartyMarkerSprite(new TextureRegion(characterSheet, markerSpriteX * tileWidth, markerSpriteY * tileHeight, tileWidth, tileHeight));
            Gdx.app.log("RingPrototypeGame", "Party marker sprite set.");
        } else {
            Gdx.app.error("RingPrototypeGame", "Cannot set party marker sprite, characterSheet is null.");
        }
        
        // Enemy initialization moved to BattleScreen
        // Party initialization moved to GameSetup
        // Resource initialization moved to GameSetup
        // Party position initialization moved to GameSetup
    }

    private Skin createFallbackSkin() {
        Skin skin = new Skin();
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font, BitmapFont.class);

        // Create a white pixel texture for backgrounds
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        com.badlogic.gdx.graphics.Texture pixmapTexture = new com.badlogic.gdx.graphics.Texture(pixmap);
        pixmap.dispose();
        
        // Add the white texture to the skin
        skin.add("white-pixel", new TextureRegion(pixmapTexture), TextureRegion.class);
        TextureRegionDrawable whiteDrawable = new TextureRegionDrawable(skin.getRegion("white-pixel"));
        
        // Create darker drawables for selection and background
        com.badlogic.gdx.graphics.Pixmap darkPixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        darkPixmap.setColor(new com.badlogic.gdx.graphics.Color(0.2f, 0.2f, 0.2f, 1f));
        darkPixmap.fill();
        com.badlogic.gdx.graphics.Texture darkTexture = new com.badlogic.gdx.graphics.Texture(darkPixmap);
        darkPixmap.dispose();
        
        skin.add("dark-pixel", new TextureRegion(darkTexture), TextureRegion.class);
        TextureRegionDrawable darkDrawable = new TextureRegionDrawable(skin.getRegion("dark-pixel"));

        // Label styles
        com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle labelStyle = new com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle();
        labelStyle.font = skin.getFont("default-font");
        labelStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        skin.add("default", labelStyle);

        // TextButton styles
        com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle textButtonStyle = new com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle();
        textButtonStyle.font = skin.getFont("default-font");
        textButtonStyle.fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        textButtonStyle.up = darkDrawable;
        textButtonStyle.down = whiteDrawable; 
        textButtonStyle.over = new TextureRegionDrawable(skin.getRegion("dark-pixel")).tint(new com.badlogic.gdx.graphics.Color(0.3f, 0.3f, 0.3f, 1f));
        skin.add("default", textButtonStyle);

        // List styles
        com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle listStyle = new com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle();
        listStyle.font = skin.getFont("default-font");
        listStyle.fontColorSelected = com.badlogic.gdx.graphics.Color.WHITE;
        listStyle.fontColorUnselected = com.badlogic.gdx.graphics.Color.LIGHT_GRAY;
        listStyle.selection = darkDrawable;
        listStyle.background = whiteDrawable.tint(new com.badlogic.gdx.graphics.Color(0.15f, 0.15f, 0.15f, 1f));
        skin.add("default", listStyle);

        // ScrollPane style
        com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle scrollPaneStyle = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle();
        scrollPaneStyle.background = darkDrawable;
        skin.add("default", scrollPaneStyle);
        
        // Window style for DialogScreen
        com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle windowStyle = new com.badlogic.gdx.scenes.scene2d.ui.Window.WindowStyle();
        windowStyle.titleFont = skin.getFont("default-font");
        windowStyle.background = darkDrawable;
        windowStyle.titleFontColor = com.badlogic.gdx.graphics.Color.WHITE;
        skin.add("default", windowStyle);
        
        Gdx.app.log("RingPrototypeGame", "Created fallback skin with all required styles");
        
        return skin;
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        Gdx.app.log("RingPrototypeGame", "Game disposed.");
        if (screen != null) {
            screen.dispose();
        }
        batch.dispose();
        assetManager.dispose();
        if (skin != null) {
            skin.dispose();
        }
        if (characterSheet != null) {
            characterSheet.dispose();
        }
    }
}
