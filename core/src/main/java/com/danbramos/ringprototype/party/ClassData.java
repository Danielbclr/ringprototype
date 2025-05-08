package com.danbramos.ringprototype.party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that loads and manages character class definitions from JSON files
 */
public class ClassData {
    // Map of class ID to class data
    private final Map<String, ClassDefinition> classDefinitions;
    
    // Singleton instance
    private static ClassData instance;
    
    /**
     * Get the singleton instance of ClassData
     * @return The ClassData instance
     */
    public static ClassData getInstance() {
        if (instance == null) {
            instance = new ClassData();
        }
        return instance;
    }
    
    /**
     * Private constructor - loads class definitions
     */
    private ClassData() {
        classDefinitions = new HashMap<>();
        loadClassDefinitions();
    }
    
    /**
     * Load all class definitions from JSON files in the data/classes directory
     */
    private void loadClassDefinitions() {
        // Get all JSON files in the classes directory
        FileHandle classesDir = Gdx.files.internal("data/classes");
        if (!classesDir.exists()) {
            Gdx.app.error("ClassData", "Classes directory not found: " + classesDir.path());
            return;
        }
        
        // Skip the schema file
        for (FileHandle fileHandle : classesDir.list(".json")) {
            if (fileHandle.name().equals("class-schema.json")) {
                continue;
            }
            
            try {
                ClassDefinition classDefinition = loadClassDefinition(fileHandle);
                if (classDefinition != null) {
                    classDefinitions.put(classDefinition.getId(), classDefinition);
                    Gdx.app.debug("ClassData", "Loaded class: " + classDefinition.getDisplayName());
                }
            } catch (Exception e) {
                Gdx.app.error("ClassData", "Error loading class definition from " + fileHandle.path(), e);
            }
        }
        
        // Log how many classes were loaded
        Gdx.app.log("ClassData", "Loaded " + classDefinitions.size() + " class definitions");
    }
    
    /**
     * Load a class definition from a JSON file using manual parsing
     * @param fileHandle The file handle to load from
     * @return The class definition, or null if there was an error
     */
    private ClassDefinition loadClassDefinition(FileHandle fileHandle) {
        try {
            // Parse the JSON manually to handle the Map field correctly
            JsonReader jsonReader = new JsonReader();
            JsonValue jsonData = jsonReader.parse(fileHandle);
            
            ClassDefinition definition = new ClassDefinition();
            
            // Parse basic properties
            definition.setId(jsonData.getString("id", ""));
            definition.setDisplayName(jsonData.getString("displayName", ""));
            definition.setDescription(jsonData.getString("description", ""));
            definition.setStartingHealth(jsonData.getInt("startingHealth", 10));
            definition.setStartingMana(jsonData.getInt("startingMana", 10));
            definition.setMovementRange(jsonData.getInt("movementRange", 3));
            definition.setHealthPerLevel(jsonData.getInt("healthPerLevel", 5));
            definition.setManaPerLevel(jsonData.getInt("manaPerLevel", 2));
            
            // Parse baseStats as a Map
            JsonValue baseStats = jsonData.get("baseStats");
            if (baseStats != null) {
                Map<String, Integer> statsMap = new HashMap<>();
                for (JsonValue stat = baseStats.child; stat != null; stat = stat.next) {
                    statsMap.put(stat.name, stat.asInt());
                }
                definition.setBaseStats(statsMap);
            }
            
            // Parse skills array
            JsonValue skills = jsonData.get("startingSkills");
            if (skills != null) {
                List<String> skillsList = new ArrayList<>();
                for (JsonValue skill = skills.child; skill != null; skill = skill.next) {
                    skillsList.add(skill.asString());
                }
                definition.setStartingSkills(skillsList);
            }
            
            // Parse items array
            JsonValue items = jsonData.get("startingItems");
            if (items != null) {
                List<String> itemsList = new ArrayList<>();
                for (JsonValue item = items.child; item != null; item = item.next) {
                    itemsList.add(item.asString());
                }
                definition.setStartingItems(itemsList);
            }
            
            return definition;
        } catch (Exception e) {
            Gdx.app.error("ClassData", "Error parsing class definition from " + fileHandle.path(), e);
            return null;
        }
    }
    
    /**
     * Get a class definition by ID
     * @param id The class ID
     * @return The class definition, or null if not found
     */
    public ClassDefinition getClassDefinition(String id) {
        return classDefinitions.get(id);
    }
    
    /**
     * Get a class definition by GameClass enum
     * @param gameClass The GameClass enum
     * @return The class definition, or null if not found
     */
    public ClassDefinition getClassDefinition(GameClass gameClass) {
        return getClassDefinition(gameClass.getId());
    }
    
    /**
     * Get all available class definitions
     * @return A list of all class definitions
     */
    public List<ClassDefinition> getAllClassDefinitions() {
        return new ArrayList<>(classDefinitions.values());
    }
    
    /**
     * Represents a character class definition loaded from JSON
     */
    public static class ClassDefinition {
        private String id;
        private String displayName;
        private String description;
        private Map<String, Integer> baseStats;
        private int startingHealth;
        private int startingMana;
        private int movementRange;
        private int healthPerLevel;
        private int manaPerLevel;
        private List<String> startingSkills;
        private List<String> startingItems;
        
        // Default constructor for JSON deserialization
        public ClassDefinition() {
            baseStats = new HashMap<>();
            startingSkills = new ArrayList<>();
            startingItems = new ArrayList<>();
        }
        
        public String getId() {
            return id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
        
        public int getBaseStat(String statName) {
            return baseStats.getOrDefault(statName, 5); // Default value if stat not found
        }
        
        public Map<String, Integer> getBaseStats() {
            return baseStats;
        }
        
        public int getStartingHealth() {
            return startingHealth;
        }
        
        public int getStartingMana() {
            return startingMana;
        }
        
        public int getMovementRange() {
            return movementRange;
        }
        
        public int getHealthPerLevel() {
            return healthPerLevel;
        }
        
        public int getManaPerLevel() {
            return manaPerLevel;
        }
        
        public List<String> getStartingSkills() {
            return startingSkills;
        }
        
        public List<String> getStartingItems() {
            return startingItems;
        }
        
        // Setters for JSON deserialization
        public void setId(String id) {
            this.id = id;
        }
        
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        
        public void setDescription(String description) {
            this.description = description;
        }
        
        public void setBaseStats(Map<String, Integer> baseStats) {
            this.baseStats = baseStats;
        }
        
        public void setStartingHealth(int startingHealth) {
            this.startingHealth = startingHealth;
        }
        
        public void setStartingMana(int startingMana) {
            this.startingMana = startingMana;
        }
        
        public void setMovementRange(int movementRange) {
            this.movementRange = movementRange;
        }
        
        public void setHealthPerLevel(int healthPerLevel) {
            this.healthPerLevel = healthPerLevel;
        }
        
        public void setManaPerLevel(int manaPerLevel) {
            this.manaPerLevel = manaPerLevel;
        }
        
        public void setStartingSkills(List<String> startingSkills) {
            this.startingSkills = startingSkills;
        }
        
        public void setStartingItems(List<String> startingItems) {
            this.startingItems = startingItems;
        }
    }
} 