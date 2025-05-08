package com.danbramos.ringprototype.quests;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all quests in the game, loading them from JSON files.
 */
public class QuestManager {
    // Map of quest ID to quest definition
    private final Map<String, Quest> quests;
    
    // Singleton instance
    private static QuestManager instance;
    
    /**
     * Get the singleton instance of QuestManager
     * @return The QuestManager instance
     */
    public static QuestManager getInstance() {
        if (instance == null) {
            instance = new QuestManager();
        }
        return instance;
    }
    
    /**
     * Private constructor - loads quest definitions
     */
    private QuestManager() {
        quests = new HashMap<>();
        loadQuests();
    }
    
    /**
     * Load all quests from JSON files in the data/quests directory
     */
    private void loadQuests() {
        // Get all JSON files in the quests directory
        FileHandle questsDir = Gdx.files.internal("data/quests");
        if (!questsDir.exists()) {
            Gdx.app.error("QuestManager", "Quests directory not found: " + questsDir.path());
            return;
        }
        
        // Skip the schema file
        for (FileHandle fileHandle : questsDir.list(".json")) {
            if (fileHandle.name().equals("quest-schema.json")) {
                continue;
            }
            
            try {
                Quest quest = loadQuest(fileHandle);
                if (quest != null) {
                    quests.put(quest.getId(), quest);
                    Gdx.app.debug("QuestManager", "Loaded quest: " + quest.getTitle());
                }
            } catch (Exception e) {
                Gdx.app.error("QuestManager", "Error loading quest from " + fileHandle.path(), e);
            }
        }
        
        // Log how many quests were loaded
        Gdx.app.log("QuestManager", "Loaded " + quests.size() + " quests");
    }
    
    /**
     * Load a quest from a JSON file
     * @param fileHandle The file handle to load from
     * @return The quest, or null if there was an error
     */
    private Quest loadQuest(FileHandle fileHandle) {
        Json json = new Json();
        // Configure the Json object to correctly deserialize lists and maps
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        return json.fromJson(Quest.class, fileHandle);
    }
    
    /**
     * Get a quest by ID
     * @param id The quest ID
     * @return The quest, or null if not found
     */
    public Quest getQuest(String id) {
        return quests.get(id);
    }
    
    /**
     * Get all quests
     * @return A list of all quests
     */
    public List<Quest> getAllQuests() {
        return new ArrayList<>(quests.values());
    }
    
    /**
     * Get all active quests (in progress)
     * @return A list of active quests
     */
    public List<Quest> getActiveQuests() {
        List<Quest> activeQuests = new ArrayList<>();
        for (Quest quest : quests.values()) {
            if (quest.getStatus() == Quest.QuestStatus.IN_PROGRESS) {
                activeQuests.add(quest);
            }
        }
        return activeQuests;
    }
    
    /**
     * Accept a quest, changing its status to IN_PROGRESS
     * @param questId The ID of the quest to accept
     * @return true if the quest was found and accepted, false otherwise
     */
    public boolean acceptQuest(String questId) {
        Quest quest = quests.get(questId);
        if (quest != null && quest.getStatus() == Quest.QuestStatus.NOT_STARTED) {
            quest.setStatus(Quest.QuestStatus.IN_PROGRESS);
            Gdx.app.log("QuestManager", "Quest accepted: " + quest.getTitle());
            return true;
        }
        return false;
    }
    
    /**
     * Update quest progress for a kill objective
     * @param enemyId The ID of the enemy killed
     */
    public void updateKillObjectives(String enemyId) {
        for (Quest quest : getActiveQuests()) {
            for (Quest.QuestObjective objective : quest.getObjectives()) {
                if (objective.getType() == Quest.ObjectiveType.KILL && 
                    objective.getTargetId().equals(enemyId) &&
                    !objective.isCompleted()) {
                    
                    // Increment the current count
                    objective.incrementProgress();
                    Gdx.app.log("QuestManager", "Updated kill objective for " + quest.getTitle() + 
                                ": " + objective.getProgress() + "/" + objective.getCount());
                    
                    // Check if the objective is now complete
                    if (objective.getProgress() >= objective.getCount()) {
                        objective.setCompleted(true);
                        Gdx.app.log("QuestManager", "Objective completed: " + objective.getDescription());
                        
                        // Check if all objectives are complete
                        checkQuestCompletion(quest);
                    }
                }
            }
        }
    }
    
    /**
     * Check if all objectives for a quest are complete
     * @param quest The quest to check
     */
    private void checkQuestCompletion(Quest quest) {
        boolean allComplete = true;
        for (Quest.QuestObjective objective : quest.getObjectives()) {
            if (!objective.isCompleted()) {
                allComplete = false;
                break;
            }
        }
        
        if (allComplete) {
            quest.setStatus(Quest.QuestStatus.COMPLETED);
            Gdx.app.log("QuestManager", "Quest completed: " + quest.getTitle());
            // TODO: Award quest rewards here or when turning in the quest
        }
    }
    
    /**
     * Create a TextureRegion for a quest giver based on its sprite info
     * @param quest The quest
     * @param characterSheet The character sheet texture
     * @return The TextureRegion for the quest giver
     */
    public TextureRegion createQuestGiverSprite(Quest quest, Texture characterSheet) {
        if (characterSheet != null && quest.getGiverSpriteInfo() != null) {
            int tileWidth = 16; // Standard tile width
            int tileHeight = 16; // Standard tile height
            int spriteX = quest.getGiverSpriteInfo().getSpriteSheetX();
            int spriteY = quest.getGiverSpriteInfo().getSpriteSheetY();
            return new TextureRegion(characterSheet, 
                                   spriteX * tileWidth, 
                                   spriteY * tileHeight, 
                                   tileWidth, tileHeight);
        }
        return null;
    }
    
    /**
     * Get all quest givers with their positions
     * @return A map of quest ID to position
     */
    public Map<String, Vector2> getQuestGiverPositions() {
        Map<String, Vector2> positions = new HashMap<>();
        for (Quest quest : quests.values()) {
            if (quest.getGiverMapPosition() != null) {
                positions.put(quest.getId(), new Vector2(
                    quest.getGiverMapPosition().getX(),
                    quest.getGiverMapPosition().getY()
                ));
            }
        }
        return positions;
    }
} 