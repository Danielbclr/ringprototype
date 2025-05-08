package com.danbramos.ringprototype.quests;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a quest in the game.
 */
public class Quest {
    private String id;
    private String title;
    private String description;
    private QuestStatus status;
    private String giver;
    private GiverSpriteInfo giverSpriteInfo;
    private GiverMapPosition giverMapPosition;
    private DialogueContainer dialogues;
    private List<QuestObjective> objectives;
    private QuestRewards rewards;

    /**
     * Default constructor for JSON deserialization
     */
    public Quest() {
        objectives = new ArrayList<>();
        status = QuestStatus.NOT_STARTED;
    }
    
    // Getters and setters for all fields
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public QuestStatus getStatus() {
        return status;
    }

    public void setStatus(QuestStatus status) {
        this.status = status;
    }

    public String getGiver() {
        return giver;
    }

    public void setGiver(String giver) {
        this.giver = giver;
    }

    public GiverSpriteInfo getGiverSpriteInfo() {
        return giverSpriteInfo;
    }

    public void setGiverSpriteInfo(GiverSpriteInfo giverSpriteInfo) {
        this.giverSpriteInfo = giverSpriteInfo;
    }

    public GiverMapPosition getGiverMapPosition() {
        return giverMapPosition;
    }

    public void setGiverMapPosition(GiverMapPosition giverMapPosition) {
        this.giverMapPosition = giverMapPosition;
    }

    public DialogueContainer getDialogues() {
        return dialogues;
    }

    public void setDialogues(DialogueContainer dialogues) {
        this.dialogues = dialogues;
    }

    public List<QuestObjective> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<QuestObjective> objectives) {
        this.objectives = objectives;
    }

    public QuestRewards getRewards() {
        return rewards;
    }

    public void setRewards(QuestRewards rewards) {
        this.rewards = rewards;
    }

    /**
     * Get the appropriate dialogue based on the quest status
     * @return The dialogue to display
     */
    public List<DialogueLine> getCurrentDialogue() {
        if (dialogues == null) {
            return new ArrayList<>();
        }
        
        switch (status) {
            case NOT_STARTED:
                return dialogues.getOffer();
            case IN_PROGRESS:
                return dialogues.getInProgress();
            case COMPLETED:
                return dialogues.getCompletion();
            default:
                return new ArrayList<>();
        }
    }
    
    /**
     * Represents the quest status
     */
    public enum QuestStatus {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
    
    /**
     * Represents a dialogue container for a quest
     */
    public static class DialogueContainer {
        private List<DialogueLine> offer;
        private List<DialogueLine> inProgress;
        private List<DialogueLine> completion;

        /**
         * Default constructor for JSON deserialization
         */
        public DialogueContainer() {
            offer = new ArrayList<>();
            inProgress = new ArrayList<>();
            completion = new ArrayList<>();
        }

        public List<DialogueLine> getOffer() {
            return offer;
        }

        public void setOffer(List<DialogueLine> offer) {
            this.offer = offer;
        }

        public List<DialogueLine> getInProgress() {
            return inProgress;
        }

        public void setInProgress(List<DialogueLine> inProgress) {
            this.inProgress = inProgress;
        }

        public List<DialogueLine> getCompletion() {
            return completion;
        }

        public void setCompletion(List<DialogueLine> completion) {
            this.completion = completion;
        }
    }
    
    /**
     * Represents a single line of dialogue
     */
    public static class DialogueLine {
        private String speaker;
        private String text;
        private List<DialogueChoice> choices;

        /**
         * Default constructor for JSON deserialization
         */
        public DialogueLine() {
            choices = new ArrayList<>();
        }

        public String getSpeaker() {
            return speaker;
        }

        public void setSpeaker(String speaker) {
            this.speaker = speaker;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public List<DialogueChoice> getChoices() {
            return choices;
        }

        public void setChoices(List<DialogueChoice> choices) {
            this.choices = choices;
        }
    }
    
    /**
     * Represents a dialogue choice
     */
    public static class DialogueChoice {
        private String text;
        private String nextDialogId;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getNextDialogId() {
            return nextDialogId;
        }

        public void setNextDialogId(String nextDialogId) {
            this.nextDialogId = nextDialogId;
        }
    }
    
    /**
     * Represents a quest objective
     */
    public static class QuestObjective {
        private String id;
        private String description;
        private ObjectiveType type;
        private String targetId;
        private int count;
        private boolean completed;
        private int progress; // Current progress towards objective (not in JSON)
        
        /**
         * Default constructor for JSON deserialization
         */
        public QuestObjective() {
            progress = 0;
            completed = false;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public ObjectiveType getType() {
            return type;
        }

        public void setType(ObjectiveType type) {
            this.type = type;
        }

        public String getTargetId() {
            return targetId;
        }

        public void setTargetId(String targetId) {
            this.targetId = targetId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public boolean isCompleted() {
            return completed;
        }

        public void setCompleted(boolean completed) {
            this.completed = completed;
        }
        
        public int getProgress() {
            return progress;
        }
        
        public void setProgress(int progress) {
            this.progress = progress;
            if (progress >= count) {
                completed = true;
            }
        }
        
        public void incrementProgress() {
            progress++;
            if (progress >= count) {
                completed = true;
            }
        }
    }
    
    /**
     * The type of quest objective
     */
    public enum ObjectiveType {
        KILL, COLLECT, TALK_TO, REACH_LOCATION
    }
    
    /**
     * Represents quest rewards
     */
    public static class QuestRewards {
        private int gold;
        private int experience;
        private List<ItemReward> items;

        /**
         * Default constructor for JSON deserialization
         */
        public QuestRewards() {
            items = new ArrayList<>();
        }

        public int getGold() {
            return gold;
        }

        public void setGold(int gold) {
            this.gold = gold;
        }

        public int getExperience() {
            return experience;
        }

        public void setExperience(int experience) {
            this.experience = experience;
        }

        public List<ItemReward> getItems() {
            return items;
        }

        public void setItems(List<ItemReward> items) {
            this.items = items;
        }
    }
    
    /**
     * Represents an item reward
     */
    public static class ItemReward {
        private String itemId;
        private int count;

        public String getItemId() {
            return itemId;
        }

        public void setItemId(String itemId) {
            this.itemId = itemId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
    
    /**
     * Represents the position of a quest giver on the map
     */
    public static class GiverMapPosition {
        private int x;
        private int y;

        public int getX() {
            return x;
        }

        public void setX(int x) {
            this.x = x;
        }

        public int getY() {
            return y;
        }

        public void setY(int y) {
            this.y = y;
        }
    }
    
    /**
     * Represents the sprite information for a quest giver
     */
    public static class GiverSpriteInfo {
        private int spriteSheetX;
        private int spriteSheetY;

        public int getSpriteSheetX() {
            return spriteSheetX;
        }

        public void setSpriteSheetX(int spriteSheetX) {
            this.spriteSheetX = spriteSheetX;
        }

        public int getSpriteSheetY() {
            return spriteSheetY;
        }

        public void setSpriteSheetY(int spriteSheetY) {
            this.spriteSheetY = spriteSheetY;
        }
    }
} 