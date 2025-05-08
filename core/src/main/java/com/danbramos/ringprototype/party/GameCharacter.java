package com.danbramos.ringprototype.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.items.Item;

import java.util.List;

/**
 * Interface defining a game character's capabilities and properties
 */
public interface GameCharacter {
    // Basic information
    String getName();
    void setName(String name);
    int getLevel();
    GameClass getGameClass();
    
    // Health and mana
    int getHealthPoints();
    void setHealthPoints(int healthPoints);
    int getMaxHealthPoints();
    int getManaPoints();
    void setManaPoints(int manaPoints);
    int getMaxManaPoints();
    
    // Experience
    int getExperiencePoints();
    int getExperienceToNextLevel();
    void gainExperience(int amount);
    
    // Battle-related
    TextureRegion getBattleSprite();
    void setBattleSprite(TextureRegion battleSprite);
    Vector2 getBattleMapPosition();
    void setBattleMapPosition(float x, float y);
    int getMovementRange();
    void setMovementRange(int movementRange);
    
    // Skills
    List<Skill> getKnownSkills();
    void learnSkill(Skill skill);
    
    // Inventory
    List<Item> getInventory();
    List<Item> getEquippedItems();
    void addItemToInventory(Item item);
    boolean removeItemFromInventory(Item item);
    boolean equipItem(Item item);
    boolean unequipItem(Item item);
    boolean isEquipped(Item item);
    
    // Stats
    int getBaseStrength();
    int getStrength();
    void setStrength(int strength);
    
    int getBaseDexterity();
    int getDexterity();
    void setDexterity(int dexterity);
    
    int getBaseIntelligence();
    int getIntelligence();
    void setIntelligence(int intelligence);
    
    int getBaseConstitution();
    int getConstitution();
    void setConstitution(int constitution);
    
    int getBaseWisdom();
    int getWisdom();
    void setWisdom(int wisdom);
    
    int getBaseCharisma();
    int getCharisma();
    void setCharisma(int charisma);
    
    // Status effects
    void takeDamage(int amount);
    void heal(int amount);
} 