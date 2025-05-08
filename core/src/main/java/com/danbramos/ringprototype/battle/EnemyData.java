package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that loads and manages enemy definitions from JSON files
 */
public class EnemyData {
    // Map of enemy ID to enemy definition
    private final Map<String, EnemyDefinition> enemyDefinitions;
    
    // Singleton instance
    private static EnemyData instance;
    
    /**
     * Get the singleton instance of EnemyData
     * @return The EnemyData instance
     */
    public static EnemyData getInstance() {
        if (instance == null) {
            instance = new EnemyData();
        }
        return instance;
    }
    
    /**
     * Private constructor - loads enemy definitions
     */
    private EnemyData() {
        enemyDefinitions = new HashMap<>();
        loadEnemyDefinitions();
    }
    
    /**
     * Load all enemy definitions from JSON files in the data/enemies directory
     */
    private void loadEnemyDefinitions() {
        // Get all JSON files in the enemies directory
        FileHandle enemiesDir = Gdx.files.internal("data/enemies");
        if (!enemiesDir.exists()) {
            Gdx.app.error("EnemyData", "Enemies directory not found: " + enemiesDir.path());
            return;
        }
        
        // Skip the schema file
        for (FileHandle fileHandle : enemiesDir.list(".json")) {
            if (fileHandle.name().equals("enemy-schema.json")) {
                continue;
            }
            
            try {
                EnemyDefinition enemyDefinition = loadEnemyDefinition(fileHandle);
                if (enemyDefinition != null) {
                    enemyDefinitions.put(enemyDefinition.getId(), enemyDefinition);
                    Gdx.app.debug("EnemyData", "Loaded enemy: " + enemyDefinition.getName());
                }
            } catch (Exception e) {
                Gdx.app.error("EnemyData", "Error loading enemy definition from " + fileHandle.path(), e);
            }
        }
        
        // Log how many enemies were loaded
        Gdx.app.log("EnemyData", "Loaded " + enemyDefinitions.size() + " enemy definitions");
    }
    
    /**
     * Load an enemy definition from a JSON file
     * @param fileHandle The file handle to load from
     * @return The enemy definition, or null if there was an error
     */
    private EnemyDefinition loadEnemyDefinition(FileHandle fileHandle) {
        Json json = new Json();
        // Configure the Json object to correctly deserialize lists and maps
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        return json.fromJson(EnemyDefinition.class, fileHandle);
    }
    
    /**
     * Get an enemy definition by ID
     * @param id The enemy ID
     * @return The enemy definition, or null if not found
     */
    public EnemyDefinition getEnemyDefinition(String id) {
        return enemyDefinitions.get(id);
    }
    
    /**
     * Get all available enemy definitions
     * @return A list of all enemy definitions
     */
    public List<EnemyDefinition> getAllEnemyDefinitions() {
        return new ArrayList<>(enemyDefinitions.values());
    }
    
    /**
     * Create an Enemy instance from an enemy definition
     * @param id The enemy definition ID
     * @param characterSheet The character sheet texture
     * @param x The initial X position
     * @param y The initial Y position
     * @return The Enemy instance, or null if the enemy definition was not found
     */
    public Enemy createEnemy(String id, Texture characterSheet, float x, float y) {
        EnemyDefinition def = getEnemyDefinition(id);
        if (def == null) {
            Gdx.app.error("EnemyData", "Enemy definition not found: " + id);
            return null;
        }
        
        // Create a texture region for the enemy sprite
        TextureRegion sprite = null;
        if (characterSheet != null && def.getSpriteInfo() != null) {
            int tileWidth = 16; // Standard tile width
            int tileHeight = 16; // Standard tile height
            int spriteX = def.getSpriteInfo().getSpriteSheetX();
            int spriteY = def.getSpriteInfo().getSpriteSheetY();
            sprite = new TextureRegion(characterSheet, 
                                       spriteX * tileWidth, 
                                       spriteY * tileHeight, 
                                       tileWidth, tileHeight);
        }
        
        return new Enemy(def.getName(), def.getMaxHp(), def.getDamageRoll(), sprite, x, y, def.getMovementRange());
    }
    
    /**
     * Represents an enemy definition loaded from JSON
     */
    public static class EnemyDefinition {
        private String id;
        private String name;
        private String description;
        private int maxHp;
        private String damageRoll;
        private SpriteInfo spriteInfo;
        private int movementRange;
        private String aiType;
        private List<SkillDefinition> skills;
        private List<DropItem> dropTable;
        private int experienceValue;
        
        // Default constructor for JSON deserialization
        public EnemyDefinition() {
            skills = new ArrayList<>();
            dropTable = new ArrayList<>();
        }
        
        public String getId() {
            return id;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getMaxHp() {
            return maxHp;
        }
        
        public String getDamageRoll() {
            return damageRoll;
        }
        
        public SpriteInfo getSpriteInfo() {
            return spriteInfo;
        }
        
        public int getMovementRange() {
            return movementRange;
        }
        
        public String getAiType() {
            return aiType;
        }
        
        public List<SkillDefinition> getSkills() {
            return skills;
        }
        
        public List<DropItem> getDropTable() {
            return dropTable;
        }
        
        public int getExperienceValue() {
            return experienceValue;
        }
        
        // Setters for JSON deserialization
        public void setId(String id) {
            this.id = id;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public void setMaxHp(int maxHp) {
            this.maxHp = maxHp;
        }
        
        public void setDamageRoll(String damageRoll) {
            this.damageRoll = damageRoll;
        }
        
        public void setSpriteInfo(SpriteInfo spriteInfo) {
            this.spriteInfo = spriteInfo;
        }
        
        public void setMovementRange(int movementRange) {
            this.movementRange = movementRange;
        }
        
        public void setAiType(String aiType) {
            this.aiType = aiType;
        }
        
        public void setSkills(List<SkillDefinition> skills) {
            this.skills = skills;
        }
        
        public void setDropTable(List<DropItem> dropTable) {
            this.dropTable = dropTable;
        }
        
        public void setExperienceValue(int experienceValue) {
            this.experienceValue = experienceValue;
        }
    }
    
    /**
     * Sprite information for an enemy
     */
    public static class SpriteInfo {
        private int spriteSheetX;
        private int spriteSheetY;
        
        public SpriteInfo() {
        }
        
        public int getSpriteSheetX() {
            return spriteSheetX;
        }
        
        public int getSpriteSheetY() {
            return spriteSheetY;
        }
        
        public void setSpriteSheetX(int spriteSheetX) {
            this.spriteSheetX = spriteSheetX;
        }
        
        public void setSpriteSheetY(int spriteSheetY) {
            this.spriteSheetY = spriteSheetY;
        }
    }
    
    /**
     * Definition of a skill used by an enemy
     */
    public static class SkillDefinition {
        private String name;
        private String description;
        private String type;
        private int range;
        private String damageRoll;
        private int aoeRadius;
        
        public SkillDefinition() {
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getType() {
            return type;
        }
        
        public int getRange() {
            return range;
        }
        
        public String getDamageRoll() {
            return damageRoll;
        }
        
        public int getAoeRadius() {
            return aoeRadius;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public void setType(String type) {
            this.type = type;
        }
        
        public void setRange(int range) {
            this.range = range;
        }
        
        public void setDamageRoll(String damageRoll) {
            this.damageRoll = damageRoll;
        }
        
        public void setAoeRadius(int aoeRadius) {
            this.aoeRadius = aoeRadius;
        }
    }
    
    /**
     * An item that can be dropped by an enemy
     */
    public static class DropItem {
        private String itemId;
        private float chance;
        
        public DropItem() {
        }
        
        public String getItemId() {
            return itemId;
        }
        
        public float getChance() {
            return chance;
        }
        
        public void setItemId(String itemId) {
            this.itemId = itemId;
        }
        
        public void setChance(float chance) {
            this.chance = chance;
        }
    }
} 