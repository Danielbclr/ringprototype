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
            skillsLog.append(skill.getName()).append(" (").append(skillId).append("), ");
        }
        Gdx.app.log("RingPrototypeGame", skillsLog.toString());

        // Example: Set some initial resources after ResourceManager is created
        resourceManager.setResourceAmount(ResourceType.FOOD, 20);
        resourceManager.setResourceAmount(ResourceType.FIREWOOD, 10);
        resourceManager.setResourceAmount(ResourceType.GOLD, 100);
        resourceManager.setResourceAmount(ResourceType.HOPE, 75);
        Gdx.app.log("RingPrototypeGame", "Initial resources: " + resourceManager.toString());

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

        initializeGameData();

        Gdx.app.log("RingPrototypeGame", "Game created, setting MapScreen.");
        this.setScreen(new MapScreen(this));
    }

    private void initializeGameData() {
        int tileWidth = 16;
        int tileHeight = 16;

        if (characterSheet != null) {
            // Example: Assuming your party marker is a specific sprite on the sheet
            int markerSpriteX = 27; // tile X on spritesheet
            int markerSpriteY = 0; // tile Y on spritesheet
            partyManager.setPartyMarkerSprite(new TextureRegion(characterSheet, markerSpriteX * tileWidth, markerSpriteY * tileHeight, tileWidth, tileHeight));
        }

        // Set initial party position near the quest giver
        partyManager.setMapPosition(12, 10);
        Gdx.app.log("RingPrototypeGame", "Party positioned on map at: " + partyManager.getMapPosition());

        // --- Get Skills from SkillData ---
        SkillData skillData = SkillData.getInstance();
        Skill aragornAttack = skillData.getSkill("skill_slash");
        Skill legolasShoot = skillData.getSkill("skill_precise_shot");
        Skill legolasExplosiveArrow = skillData.getSkill("skill_explosive_arrow");

        // --- Initialize Party Members ---
        GameCharacter aragorn = new Character("Aragorn", GameClass.WARRIOR);
        aragorn.setMovementRange(4);
        if (characterSheet != null) {
            aragorn.setBattleSprite(new TextureRegion(characterSheet, 27 * tileWidth, 0 * tileHeight, tileWidth, tileHeight));
        }
        aragorn.setBattleMapPosition(5, 8);
        aragorn.learnSkill(aragornAttack);
        Item anduril = new Item("ITM_ANDURIL", "Andúril", "Flame of the West, Sword Reforged.", ItemType.ARTIFACT);
        anduril.addStatBonus("strength", 2);
        aragorn.addItemToInventory(anduril);
        aragorn.equipItem(anduril);
        aragorn.addItemToInventory(new Item("ITM_POT_HEALTH_S", "Small Health Potion", "Restores a small amount of health.", ItemType.CONSUMABLE));
        aragorn.addItemToInventory(new Item("ITM_LEATHER_GLOVES", "Leather Gloves", "Basic hand protection.", ItemType.ARMOR_HANDS));
        partyManager.addMember(aragorn);
        Gdx.app.log("RingPrototypeGame", "Aragorn (Warrior) added to party. Skills: " + aragorn.getKnownSkills());


        GameCharacter legolas = new Character("Legolas", GameClass.RANGER);
        legolas.setMovementRange(5);
        if (characterSheet != null) {
            legolas.setBattleSprite(new TextureRegion(characterSheet, 24 * tileWidth, 1 * tileHeight, tileWidth, tileHeight));
        }
        legolas.setBattleMapPosition(7, 8);
        legolas.learnSkill(legolasShoot);
        legolas.learnSkill(legolasExplosiveArrow);
        Item elvenBow = new Item("ITM_LONGBOW_ELF", "Elven Longbow", "A finely crafted bow.", ItemType.WEAPON);
        elvenBow.addStatBonus("dexterity", 2);
        legolas.addItemToInventory(elvenBow);
        legolas.equipItem(elvenBow);
        legolas.addItemToInventory(new Item("ITM_QUIVER_ARROWS", "Quiver of Arrows", "Holds many arrows.", ItemType.MISCELLANEOUS));
        partyManager.addMember(legolas);
        Gdx.app.log("RingPrototypeGame", "Legolas (Ranger) added to party. Skills: " + legolas.getKnownSkills());

        Gdx.app.log("RingPrototypeGame", "Party initialized. Members: " + partyManager.getPartySize());

        // --- Initialize Enemies ---
        currentBattleEnemies.clear();
        
        if (characterSheet != null) {
            // Create an orc enemy using the EnemyData system
            Enemy orc = EnemyData.getInstance().createEnemy("orc_grunt", characterSheet, 6, 5);
            if (orc != null) {
                currentBattleEnemies.add(orc);
                Gdx.app.log("RingPrototypeGame", orc.getName() + " added to battle enemies at " + 
                            orc.getBattleMapPosition() + " with move " + orc.getMovementRange());
            } else {
                // Fallback to hardcoded enemy if enemy data loading fails
                Gdx.app.error("RingPrototypeGame", "Failed to load orc_grunt from EnemyData, using fallback");
            int orcSpriteSheetX = 27; // Example sprite
            int orcSpriteSheetY = 2;  // Example sprite
            TextureRegion orcSprite = new TextureRegion(characterSheet,
                orcSpriteSheetX * tileWidth,
                orcSpriteSheetY * tileHeight,
                tileWidth,
                tileHeight);

                orc = new Enemy("Orc Grunt", 8, "1d6", orcSprite, 6, 5, 3);
            currentBattleEnemies.add(orc);
                Gdx.app.log("RingPrototypeGame", orc.getName() + " added to battle enemies at " + 
                            orc.getBattleMapPosition() + " with move " + orc.getMovementRange());
            }

            // Add a warg enemy
            Enemy warg = EnemyData.getInstance().createEnemy("warg", characterSheet, 8, 3);
            if (warg != null) {
                currentBattleEnemies.add(warg);
                Gdx.app.log("RingPrototypeGame", warg.getName() + " added to battle enemies at " + 
                             warg.getBattleMapPosition());
            }
        } else {
            Gdx.app.error("RingPrototypeGame", "Cannot create enemies, characterSheet is null.");
        }
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
