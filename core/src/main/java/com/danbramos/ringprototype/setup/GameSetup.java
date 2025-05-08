package com.danbramos.ringprototype.setup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.danbramos.ringprototype.battle.Skill;
import com.danbramos.ringprototype.battle.SkillData;
import com.danbramos.ringprototype.items.Item;
import com.danbramos.ringprototype.items.ItemType;
import com.danbramos.ringprototype.party.Character;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.party.GameClass;
import com.danbramos.ringprototype.party.PartyManager;
import com.danbramos.ringprototype.resources.ResourceManager;
import com.danbramos.ringprototype.resources.ResourceType;

/**
 * Handles the initial setup of the game state, such as
 * initial resources and party composition.
 */
public class GameSetup {

    private final int tileWidth = 16; // Assuming default tile size, could be passed in if needed
    private final int tileHeight = 16;

    /**
     * Sets the initial amounts for player resources.
     * @param resourceManager The ResourceManager instance.
     */
    public void initializeInitialResources(ResourceManager resourceManager) {
        resourceManager.setResourceAmount(ResourceType.FOOD, 20);
        resourceManager.setResourceAmount(ResourceType.FIREWOOD, 10);
        resourceManager.setResourceAmount(ResourceType.GOLD, 100);
        resourceManager.setResourceAmount(ResourceType.HOPE, 75);
        Gdx.app.log("GameSetup", "Initial resources set: " + resourceManager.toString());
    }

    /**
     * Creates and adds the starting party members.
     * @param partyManager The PartyManager instance.
     * @param skillData The SkillData instance to retrieve skills.
     * @param characterSheet The texture sheet for character sprites.
     */
    public void initializeInitialParty(PartyManager partyManager, SkillData skillData, Texture characterSheet) {
        partyManager.clearParty(); // Ensure party is empty before adding

        // --- Get Skills from SkillData ---
        Skill aragornAttack = skillData.getSkill("skill_slash");
        Skill legolasShoot = skillData.getSkill("skill_precise_shot");
        Skill legolasExplosiveArrow = skillData.getSkill("skill_explosive_arrow");

        // --- Initialize Aragorn ---
        GameCharacter aragorn = new Character("Aragorn", GameClass.WARRIOR);
        aragorn.setMovementRange(4);
        if (characterSheet != null) {
            // Aragorn's sprite location (example)
            aragorn.setBattleSprite(new TextureRegion(characterSheet, 27 * tileWidth, 0 * tileHeight, tileWidth, tileHeight));
        }
        aragorn.setBattleMapPosition(5, 8); // Default battle start position
        if (aragornAttack != null) {
            aragorn.learnSkill(aragornAttack);
        } else {
            Gdx.app.error("GameSetup", "Failed to find skill 'skill_slash' for Aragorn!");
        }
        Item anduril = new Item("ITM_ANDURIL", "Andúril", "Flame of the West, Sword Reforged.", ItemType.ARTIFACT);
        anduril.addStatBonus("strength", 2);
        aragorn.addItemToInventory(anduril);
        aragorn.equipItem(anduril);
        aragorn.addItemToInventory(new Item("ITM_POT_HEALTH_S", "Small Health Potion", "Restores a small amount of health.", ItemType.CONSUMABLE));
        aragorn.addItemToInventory(new Item("ITM_LEATHER_GLOVES", "Leather Gloves", "Basic hand protection.", ItemType.ARMOR_HANDS));
        partyManager.addMember(aragorn);
        Gdx.app.log("GameSetup", "Aragorn (Warrior) added to party. Skills: " + aragorn.getKnownSkills());

        // --- Initialize Legolas ---
        GameCharacter legolas = new Character("Legolas", GameClass.RANGER);
        legolas.setMovementRange(5);
        if (characterSheet != null) {
            // Legolas' sprite location (example)
            legolas.setBattleSprite(new TextureRegion(characterSheet, 24 * tileWidth, 1 * tileHeight, tileWidth, tileHeight));
        }
        legolas.setBattleMapPosition(7, 8); // Default battle start position
        if (legolasShoot != null) {
            legolas.learnSkill(legolasShoot);
        } else {
            Gdx.app.error("GameSetup", "Failed to find skill 'skill_precise_shot' for Legolas!");
        }
         if (legolasExplosiveArrow != null) {
            legolas.learnSkill(legolasExplosiveArrow);
        } else {
            Gdx.app.error("GameSetup", "Failed to find skill 'skill_explosive_arrow' for Legolas!");
        }
        Item elvenBow = new Item("ITM_LONGBOW_ELF", "Elven Longbow", "A finely crafted bow.", ItemType.WEAPON);
        elvenBow.addStatBonus("dexterity", 2);
        legolas.addItemToInventory(elvenBow);
        legolas.equipItem(elvenBow);
        legolas.addItemToInventory(new Item("ITM_QUIVER_ARROWS", "Quiver of Arrows", "Holds many arrows.", ItemType.MISCELLANEOUS));
        partyManager.addMember(legolas);
        Gdx.app.log("GameSetup", "Legolas (Ranger) added to party. Skills: " + legolas.getKnownSkills());

        Gdx.app.log("GameSetup", "Initial party created. Members: " + partyManager.getPartySize());
    }

    /**
     * Sets the initial map position for the party.
     * @param partyManager The PartyManager instance.
     */
    public void initializePartyPosition(PartyManager partyManager) {
        // Set initial party position near the quest giver
        partyManager.setMapPosition(12, 10);
        Gdx.app.log("GameSetup", "Initial party positioned on map at: " + partyManager.getMapPosition());
    }
} 