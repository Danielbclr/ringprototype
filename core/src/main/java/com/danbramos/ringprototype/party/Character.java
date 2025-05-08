package com.danbramos.ringprototype.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.danbramos.ringprototype.battle.Skill; // Assuming Skill is a general concept
import com.danbramos.ringprototype.items.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Character {
    private String name;
    private int level;
    private GameClass gameClass;

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
    private Vector2 battleMapPosition; // Default or last known starting position for battles
    private int movementRange;         // Base movement range

    // --- Removed battle-turn-specific fields ---
    // boolean hasMovedThisTurn; // This is now managed by BattleCharacter

    public Character(String name, GameClass gameClass) {
        this.name = Objects.requireNonNull(name, "Character name cannot be null");
        this.gameClass = Objects.requireNonNull(gameClass, "Character class cannot be null");
        this.level = 1;
        this.experiencePoints = 0;
        this.experienceToNextLevel = 100;

        setDefaultStats();

        this.knownSkills = new ArrayList<>();
        this.inventory = new ArrayList<>();
        this.equippedItems = new ArrayList<>();
        this.battleMapPosition = new Vector2(-1, -1); // Default off-map or initial setup position
        // this.hasMovedThisTurn = false; // Removed
    }

    private void setDefaultStats() {
        this.strength = 6;
        this.dexterity = 6;
        this.intelligence = 6;
        this.constitution = 6;
        this.wisdom = 6;
        this.charisma = 6;

        this.maxHealthPoints = 10 + (this.constitution * 1);
        this.healthPoints = this.maxHealthPoints; // Start with full health
        this.maxManaPoints = 20 + (this.intelligence * 3);
        this.manaPoints = this.maxManaPoints;
        this.movementRange = 3; // Default base movement range
    }

    // --- Getters and Setters for persistent battle setup ---
    public TextureRegion getBattleSprite() {
        return battleSprite;
    }

    public void setBattleSprite(TextureRegion battleSprite) {
        this.battleSprite = battleSprite;
    }

    public Vector2 getBattleMapPosition() {
        return battleMapPosition;
    }

    public void setBattleMapPosition(float x, float y) {
        if (this.battleMapPosition == null) {
            this.battleMapPosition = new Vector2();
        }
        this.battleMapPosition.set(x, y);
    }

    public int getMovementRange() {
        return movementRange;
    }

    public void setMovementRange(int movementRange) {
        this.movementRange = movementRange;
    }

    // --- Removed battle-turn-specific getters/setters ---
    // public boolean hasMovedThisTurn() { ... } // Removed
    // public void setHasMovedThisTurn(boolean hasMovedThisTurn) { ... } // Removed

    // --- Existing Getters ---
    public String getName() {
        return name;
    }

    public int getLevel() {
        return level;
    }

    public GameClass getGameClass() {
        return gameClass;
    }

    public int getHealthPoints() {
        return healthPoints;
    }

    /**
     * Sets the character's current health points.
     * This is typically called after a battle or by other game events
     * that affect persistent health.
     * @param healthPoints The new health points value.
     */
    public void setHealthPoints(int healthPoints) {
        this.healthPoints = Math.max(0, Math.min(healthPoints, this.maxHealthPoints));
    }

    public int getMaxHealthPoints() {
        return maxHealthPoints;
    }

    public int getManaPoints() {
        return manaPoints;
    }

    public void setManaPoints(int manaPoints) {
        this.manaPoints = Math.max(0, Math.min(manaPoints, this.maxManaPoints));
    }

    public int getMaxManaPoints() {
        return maxManaPoints;
    }

    public int getExperiencePoints() {
        return experiencePoints;
    }

    public int getExperienceToNextLevel() {
        return experienceToNextLevel;
    }

    public List<Skill> getKnownSkills() {
        return new ArrayList<>(knownSkills);
    }

    public List<Item> getInventory() {
        return new ArrayList<>(inventory);
    }

    public List<Item> getEquippedItems() {
        return new ArrayList<>(equippedItems);
    }

    // --- Effective Stat Getters (based on persistent stats and equipment) ---
    public int getBaseStrength() {
        return strength;
    }

    public int getStrength() {
        int effectiveStrength = this.strength;
        for (Item item : equippedItems) {
            effectiveStrength += item.getStatBonus("strength");
        }
        return effectiveStrength;
    }

    public void setStrength(int strength) { // Sets base strength
        this.strength = strength;
    }

    public int getBaseDexterity() {
        return dexterity;
    }

    public int getDexterity() {
        int effectiveDexterity = this.dexterity;
        for (Item item : equippedItems) {
            effectiveDexterity += item.getStatBonus("dexterity");
        }
        return effectiveDexterity;
    }

    public void setDexterity(int dexterity) { // Sets base dexterity
        this.dexterity = dexterity;
    }

    public int getBaseIntelligence() {
        return intelligence;
    }
    public int getIntelligence() {
        int effectiveIntelligence = this.intelligence;
        for (Item item : equippedItems) {
            effectiveIntelligence += item.getStatBonus("intelligence");
        }
        return effectiveIntelligence;
    }

    public void setIntelligence(int intelligence) { // Sets base intelligence
        this.intelligence = intelligence;
    }

    public int getBaseConstitution() {
        return constitution;
    }

    public int getConstitution() {
        int effectiveConstitution = this.constitution;
        for (Item item : equippedItems) {
            effectiveConstitution += item.getStatBonus("constitution");
        }
        return effectiveConstitution;
    }

    public void setConstitution(int constitution) { // Sets base constitution
        this.constitution = constitution;
        // Optionally, re-calculate maxHealthPoints if base constitution changes
        // this.maxHealthPoints = 50 + (getConstitution() * 5);
        // this.healthPoints = Math.min(this.healthPoints, this.maxHealthPoints);
    }

    public int getBaseWisdom() { return wisdom; }
    public int getWisdom() {
        int effectiveWisdom = this.wisdom;
        for (Item item : equippedItems) {
            effectiveWisdom += item.getStatBonus("wisdom");
        }
        return effectiveWisdom;
    }
    public void setWisdom(int wisdom) { this.wisdom = wisdom; }

    public int getBaseCharisma() { return charisma; }
    public int getCharisma() {
        int effectiveCharisma = this.charisma;
        for (Item item : equippedItems) {
            effectiveCharisma += item.getStatBonus("charisma");
        }
        return effectiveCharisma;
    }
    public void setCharisma(int charisma) { this.charisma = charisma; }

    // --- Basic Mutators / Logic ---
    public void setName(String name) {
        this.name = name;
    }

    public void learnSkill(Skill skill) {
        if (skill != null && !this.knownSkills.contains(skill)) {
            this.knownSkills.add(skill);
        }
    }

    public void addItemToInventory(Item item) {
        if (item != null && !this.inventory.contains(item) && !this.equippedItems.contains(item)) {
            this.inventory.add(item);
        }
    }

    public boolean removeItemFromInventory(Item item) {
        if (isEquipped(item)) {
            return false; // Cannot remove equipped items directly from inventory list
        }
        return this.inventory.remove(item);
    }

    public boolean equipItem(Item item) {
        if (item != null && inventory.contains(item) && !equippedItems.contains(item)) {
            // TODO: Add more complex logic: e.g., checking item type, slots, class restrictions
            if (inventory.remove(item)) {
                equippedItems.add(item);
                return true;
            }
        }
        return false;
    }

    public boolean unequipItem(Item item) {
        if (item != null && equippedItems.contains(item)) {
            if (equippedItems.remove(item)) {
                inventory.add(item); // Add back to general inventory
                return true;
            }
        }
        return false;
    }

    public boolean isEquipped(Item item) {
        return item != null && equippedItems.contains(item);
    }

    /**
     * Applies persistent damage to the character (e.g., from traps, poison outside combat,
     * or when applying end-of-battle results).
     * @param amount The amount of damage to take.
     */
    public void takeDamage(int amount) {
        if (amount <= 0) return;
        this.healthPoints -= amount;
        if (this.healthPoints < 0) {
            this.healthPoints = 0;
        }
    }

    /**
     * Heals the character persistently.
     * @param amount The amount to heal.
     */
    public void heal(int amount) {
        if (amount <= 0) return;
        this.healthPoints += amount;
        if (this.healthPoints > this.maxHealthPoints) {
            this.healthPoints = this.maxHealthPoints;
        }
    }

    public void gainExperience(int amount) {
        if (amount <= 0) return;
        this.experiencePoints += amount;
        while (this.experiencePoints >= this.experienceToNextLevel) {
            levelUp();
        }
    }

    private void levelUp() {
        this.level++;
        this.experiencePoints -= this.experienceToNextLevel;
        this.experienceToNextLevel = calculateNextLevelXP();

        // Improve base stats on level up
        this.strength +=1;
        this.constitution += 1;

        // Recalculate max HP/MP based on new base stats (or effective stats if preferred)
        this.maxHealthPoints = 50 + (getConstitution() * 5); // Using effective constitution
        this.healthPoints = this.maxHealthPoints; // Fully heal on level up (common practice)

        this.maxManaPoints = 20 + (getIntelligence() * 3); // Using effective intelligence
        this.manaPoints = this.maxManaPoints;
    }

    private int calculateNextLevelXP() {
        return (int) (this.experienceToNextLevel * 1.5);
    }

    @Override
    public String toString() {
        // Updated to reflect that this is the persistent character state
        return "Character{" +
            "name='" + name + '\'' +
            ", level=" + level +
            ", gameClass=" + gameClass.getDisplayName() + // Assuming GameClass has getDisplayName
            ", HP=" + healthPoints + "/" + getMaxHealthPoints() +
            ", Str=" + getStrength() + // Shows effective strength based on equipment
            ", MoveRange=" + movementRange +
            ", XP=" + experiencePoints + "/" + experienceToNextLevel +
            ", skills=" + knownSkills.size() +
            ", inventory=" + inventory.size() +
            ", equipped=" + equippedItems.size() +
            '}';
    }
}
