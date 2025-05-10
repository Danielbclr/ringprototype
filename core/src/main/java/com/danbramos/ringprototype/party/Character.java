package com.danbramos.ringprototype.party;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.battle.skills.SkillData; // Import SkillData
import com.danbramos.ringprototype.items.Item;
// TODO: Import ItemData when it's created, e.g.:
// import com.danbramos.ringprototype.items.ItemData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Default implementation of a character in the game world
 */
public class Character implements GameCharacter {
    private String name;
    private final GameClass gameClass;
    private int level;

    // Core Stats (Persistent)
    private int healthPoints; // Current health outside of battle
    private int maxHealthPoints;
    private int manaPoints;
    private int maxManaPoints;
    private int experiencePoints;
    private int experienceToNextLevel;

    // Attributes (BASE stats - Persistent)
    private int strength;
    private int dexterity;
    private int intelligence;
    private int constitution;
    private int wisdom;
    private int charisma;

    private final List<Skill> knownSkills;
    private final List<Item> inventory;
    private final List<Item> equippedItems;

    // --- Persistent Battle-Related Setup ---
    // These are used by BattleCharacter to initialize its state
    private transient TextureRegion battleSprite;
    private final Vector2 battleMapPosition; // Default or last known starting position for battles
    private int movementRange;         // Base movement range

    /**
     * Creates a new character with the given name and class
     *
     * @param name The character's name
     * @param gameClass The character's class
     * @throws NullPointerException if name or gameClass is null
     */
    public Character(String name, GameClass gameClass) {
        this.name = Objects.requireNonNull(name, "Character name cannot be null");
        this.gameClass = Objects.requireNonNull(gameClass, "Character class cannot be null");
        this.level = 1;
        this.experiencePoints = 0;
        this.experienceToNextLevel = 100; // Default, can be adjusted by class or game rules

        this.knownSkills = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.equippedItems = new ArrayList<>();
        this.battleMapPosition = new Vector2(-1, -1); // Default off-map or initial setup position

        initializeFromClassDefinition();
    }

    /**
     * Initialize character stats from class definition
     */
    private void initializeFromClassDefinition() {
        // Get the class definition from ClassData
        ClassData.ClassDefinition classDef = ClassData.getInstance().getClassDefinition(gameClass);

        if (classDef != null) {
            // Set stats from class definition
            this.strength = classDef.getBaseStats().getOrDefault("strength", 6);
            this.dexterity = classDef.getBaseStats().getOrDefault("dexterity", 6);
            this.intelligence = classDef.getBaseStats().getOrDefault("intelligence", 6);
            this.constitution = classDef.getBaseStats().getOrDefault("constitution", 6);
            this.wisdom = classDef.getBaseStats().getOrDefault("wisdom", 6);
            this.charisma = classDef.getBaseStats().getOrDefault("charisma", 6);

            this.maxHealthPoints = classDef.getStartingHealth();
            this.healthPoints = this.maxHealthPoints;
            this.maxManaPoints = classDef.getStartingMana();
            this.manaPoints = this.maxManaPoints;
            this.movementRange = classDef.getMovementRange();
            this.experienceToNextLevel = calculateNextLevelXP(); // Initial XP for next level

            // Add starting skills
            SkillData skillDataManager = SkillData.getInstance();
            for (String skillId : classDef.getStartingSkills()) {
                Skill skill = skillDataManager.getSkill(skillId);
                Gdx.app.log("Character", "Skill with ID: " + skillId);
                if (skill != null) {
                    learnSkill(skill);
                } else {
                    Gdx.app.error("Character", "Could not find starting skill with ID: " + skillId + " for class " + gameClass.name());
                }
            }

            // Add starting items
            // TODO: Implement item loading via ItemData once available
            // ItemData itemDataManager = ItemData.getInstance(); // Assuming ItemData singleton
            for (String itemId : classDef.getStartingItems()) {
                // Item item = itemDataManager.getItem(itemId);
                // if (item != null) {
                //     addItemToInventory(item);
                // } else {
                //     Gdx.app.error("Character", "Could not find starting item with ID: " + itemId + " for class " + gameClass.name());
                // }
                Gdx.app.log("Character", "Placeholder for adding starting item ID: " + itemId + " (ItemData system pending)");
            }

        } else {
            // Fall back to default stats if class definition not found
            Gdx.app.error("Character", "ClassDefinition not found for class: " + gameClass.name() + ". Using default stats.");
            setDefaultStats();
        }
    }

    /**
     * Default stats if class definition is not found
     */
    private void setDefaultStats() {
        this.strength = 6;
        this.dexterity = 6;
        this.intelligence = 6;
        this.constitution = 6;
        this.wisdom = 6;
        this.charisma = 6;

        this.maxHealthPoints = 10 + (this.constitution * 1); // Example calculation
        this.healthPoints = this.maxHealthPoints; // Start with full health
        this.maxManaPoints = 20 + (this.intelligence * 3); // Example calculation
        this.manaPoints = this.maxManaPoints;
        this.movementRange = 3; // Default base movement range
        this.experienceToNextLevel = 100;
    }

    // --- Implementation of GameCharacter interface ---

    @Override
    public TextureRegion getBattleSprite() {
        return battleSprite;
    }

    @Override
    public void setBattleSprite(TextureRegion battleSprite) {
        this.battleSprite = battleSprite;
    }

    @Override
    public Vector2 getBattleMapPosition() {
        // Return a copy to prevent external modification
        return new Vector2(battleMapPosition);
    }

    @Override
    public void setBattleMapPosition(float x, float y) {
        this.battleMapPosition.set(x, y);
    }

    @Override
    public int getMovementRange() {
        return movementRange;
    }

    @Override
    public void setMovementRange(int movementRange) {
        this.movementRange = movementRange;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public GameClass getGameClass() {
        return gameClass;
    }

    @Override
    public int getHealthPoints() {
        return healthPoints;
    }

    @Override
    public void setHealthPoints(int healthPoints) {
        this.healthPoints = Math.max(0, Math.min(healthPoints, this.maxHealthPoints));
    }

    @Override
    public int getMaxHealthPoints() {
        return maxHealthPoints;
    }

    @Override
    public int getManaPoints() {
        return manaPoints;
    }

    @Override
    public void setManaPoints(int manaPoints) {
        this.manaPoints = Math.max(0, Math.min(manaPoints, this.maxManaPoints));
    }

    @Override
    public int getMaxManaPoints() {
        return maxManaPoints;
    }

    @Override
    public int getExperiencePoints() {
        return experiencePoints;
    }

    @Override
    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    @Override
    public List<Skill> getKnownSkills() {
        return Collections.unmodifiableList(knownSkills);
    }

    @Override
    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    @Override
    public List<Item> getEquippedItems() {
        return Collections.unmodifiableList(equippedItems);
    }

    @Override
    public int getBaseStrength() {
        return strength;
    }

    @Override
    public int getStrength() {
        int effectiveStrength = this.strength;
        for (Item item : equippedItems) {
            effectiveStrength += item.getStatBonus("strength");
        }
        // TODO: Add strength bonuses from status effects
        return effectiveStrength;
    }

    @Override
    public void setStrength(int strength) {
        this.strength = strength;
    }

    @Override
    public int getBaseDexterity() {
        return dexterity;
    }

    @Override
    public int getDexterity() {
        int effectiveDexterity = this.dexterity;
        for (Item item : equippedItems) {
            effectiveDexterity += item.getStatBonus("dexterity");
        }
        // TODO: Add dexterity bonuses from status effects
        return effectiveDexterity;
    }

    @Override
    public void setDexterity(int dexterity) {
        this.dexterity = dexterity;
    }

    @Override
    public int getBaseIntelligence() {
        return intelligence;
    }

    @Override
    public int getIntelligence() {
        int effectiveIntelligence = this.intelligence;
        for (Item item : equippedItems) {
            effectiveIntelligence += item.getStatBonus("intelligence");
        }
        // TODO: Add intelligence bonuses from status effects
        return effectiveIntelligence;
    }

    @Override
    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    @Override
    public int getBaseConstitution() {
        return constitution;
    }

    @Override
    public int getConstitution() {
        int effectiveConstitution = this.constitution;
        for (Item item : equippedItems) {
            effectiveConstitution += item.getStatBonus("constitution");
        }
        // TODO: Add constitution bonuses from status effects
        return effectiveConstitution;
    }

    @Override
    public void setConstitution(int constitution) {
        this.constitution = constitution;
        // Re-calculate health when constitution changes
        updateMaxHealthPoints();
    }

    /**
     * Updates the character's maximum health points based on constitution
     */
    private void updateMaxHealthPoints() {
        int oldMaxHP = this.maxHealthPoints;
        // Recalculate max HP based on new constitution. Example: 10 base + CON * factor
        // This should ideally use a formula defined by the class or game rules.
        // For now, a simple formula:
        ClassData.ClassDefinition classDef = ClassData.getInstance().getClassDefinition(gameClass);
        int baseHealth = (classDef != null) ? classDef.getStartingHealth() - classDef.getBaseStat("constitution") : 10 - 6; // Estimate base health without initial CON
        this.maxHealthPoints = baseHealth + (getConstitution() * 1); // Assuming 1 HP per CON point beyond base

        if (this.healthPoints > this.maxHealthPoints || oldMaxHP == 0) { // If current HP exceeds new max OR if it was initial setup
            this.healthPoints = this.maxHealthPoints;
        }
        // If max HP decreased, current HP might need to be capped at the new max.
        this.healthPoints = Math.min(this.healthPoints, this.maxHealthPoints);
    }

    @Override
    public int getBaseWisdom() {
        return wisdom;
    }

    @Override
    public int getWisdom() {
        int effectiveWisdom = this.wisdom;
        for (Item item : equippedItems) {
            effectiveWisdom += item.getStatBonus("wisdom");
        }
        // TODO: Add wisdom bonuses from status effects
        return effectiveWisdom;
    }

    @Override
    public void setWisdom(int wisdom) {
        this.wisdom = wisdom;
    }

    @Override
    public int getBaseCharisma() {
        return charisma;
    }

    @Override
    public int getCharisma() {
        int effectiveCharisma = this.charisma;
        for (Item item : equippedItems) {
            effectiveCharisma += item.getStatBonus("charisma");
        }
        // TODO: Add charisma bonuses from status effects
        return effectiveCharisma;
    }

    @Override
    public void setCharisma(int charisma) {
        this.charisma = charisma;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name, "Character name cannot be null");
    }

    @Override
    public void learnSkill(Skill skill) {
        if (skill != null && !knownSkills.contains(skill)) {
            knownSkills.add(skill);
            Gdx.app.log(getName(), "learned skill: " + skill.getName());
        }
    }

    @Override
    public void addItemToInventory(Item item) {
        if (item != null) {
            inventory.add(item);
            Gdx.app.log(getName(), "added to inventory: " + item.getName());
        }
    }

    @Override
    public boolean removeItemFromInventory(Item item) {
        if (item != null && inventory.contains(item)) {
            // If item is equipped, unequip it first (optional, depends on game rules)
            if (isEquipped(item)) {
                unequipItem(item);
            }
            boolean removed = inventory.remove(item);
            if (removed) {
                Gdx.app.log(getName(), "removed from inventory: " + item.getName());
            }
            return removed;
        }
        return false;
    }

    @Override
    public boolean equipItem(Item item) {
        if (item != null && inventory.contains(item) && !isEquipped(item)) {
            // TODO: Add logic to check if item can be equipped (e.g., correct type for slot)
            // TODO: Add logic to unequip existing item in the same slot if necessary
            equippedItems.add(item);
            Gdx.app.log(getName(), "equipped: " + item.getName());
            return true;
        }
        return false;
    }

    @Override
    public boolean unequipItem(Item item) {
        if (item != null && equippedItems.contains(item)) {
            boolean removed = equippedItems.remove(item);
            if (removed) {
                Gdx.app.log(getName(), "unequipped: " + item.getName());
            }
            return removed;
        }
        return false;
    }

    @Override
    public boolean isEquipped(Item item) {
        return equippedItems.contains(item);
    }

    @Override
    public void takeDamage(int amount) {
        if (amount > 0) {
            this.healthPoints = Math.max(0, this.healthPoints - amount);
        }
    }

    @Override
    public void heal(int amount) {
        if (amount > 0) {
            this.healthPoints = Math.min(this.maxHealthPoints, this.healthPoints + amount);
        }
    }

    @Override
    public void gainExperience(int amount) {
        if (amount <= 0) return;
        this.experiencePoints += amount;
        Gdx.app.log(getName(), "gained " + amount + " XP. Total XP: " + this.experiencePoints);
        while (this.experiencePoints >= this.experienceToNextLevel) {
            levelUp();
        }
    }

    /**
     * Increases the character's level
     */
    private void levelUp() {
        level++;
        int xpOver = this.experiencePoints - this.experienceToNextLevel;
        this.experiencePoints = Math.max(0, xpOver); // Carry over excess XP

        // Get class definition to apply level-up bonuses
        ClassData.ClassDefinition classDef = ClassData.getInstance().getClassDefinition(gameClass);

        if (classDef != null) {
            maxHealthPoints += classDef.getHealthPerLevel();
            maxManaPoints += classDef.getManaPerLevel();
        } else {
            // Default level-up values if class definition not found
            maxHealthPoints += 5; // Example default
            maxManaPoints += 3; // Example default
        }

        // Restore health and mana on level up
        healthPoints = maxHealthPoints;
        manaPoints = maxManaPoints;

        // Calculate experience needed for next level
        this.experienceToNextLevel = calculateNextLevelXP();
        Gdx.app.log(getName(), "leveled up to level " + level + "! Next level at " + this.experienceToNextLevel + " XP.");
    }

    /**
     * Calculates the experience needed for the next level
     * Based on a simple formula, can be made more complex.
     * @return The experience needed
     */
    private int calculateNextLevelXP() {
        // Example: 100 base, +50 per level
        return 100 + (level * 50);
    }

    @Override
    public String toString() {
        return name + " (Lvl " + level + " " + gameClass + ")";
    }
}
