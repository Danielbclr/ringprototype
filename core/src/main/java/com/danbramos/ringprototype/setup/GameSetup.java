package com.danbramos.ringprototype.setup;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.battle.skills.SkillData;
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
            // Legolas's sprite location (example)
            legolas.setBattleSprite(new TextureRegion(characterSheet, 25 * tileWidth, 2 * tileHeight, tileWidth, tileHeight));
        }
        legolas.setBattleMapPosition(4, 7); // Default battle start position
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
        Item legolasBow = new Item("ITM_LEGOLAS_BOW", "Bow of the Galadhrim", "A finely crafted bow.", ItemType.WEAPON);
        legolasBow.addStatBonus("dexterity", 1);
        legolas.addItemToInventory(legolasBow);
        legolas.equipItem(legolasBow);
        legolas.addItemToInventory(new Item("ITM_POT_MANA_S", "Small Mana Potion", "Restores a small amount of mana.", ItemType.CONSUMABLE));
        partyManager.addMember(legolas);
        Gdx.app.log("GameSetup", "Legolas (Ranger) added to party. Skills: " + legolas.getKnownSkills());

        // --- Initialize Rogue (Shadow) ---
        GameCharacter shadow = new Character("Shadow", GameClass.ROGUE);
        // Note: Movement range for Rogue will be set by its ClassDefinition in Character.java constructor
        if (characterSheet != null) {
            // Rogue's sprite location (example - needs a unique sprite)
            // Using a placeholder sprite similar to Aragorn for now, adjust X,Y as needed
            // For example, if there's a rogue sprite at row 1, column 28 (0-indexed)
            shadow.setBattleSprite(new TextureRegion(characterSheet, 28 * tileWidth, 0 * tileHeight, tileWidth, tileHeight));
        }
        shadow.setBattleMapPosition(3, 6); // Default battle start position for the Rogue

        // Starting skills for Rogue are now handled by Character.initializeFromClassDefinition()
        // based on rogue.json. No need to explicitly learn them here if SkillData is initialized before party.

        // Starting items for Rogue (hardcoded for now, similar to other characters)
        // These IDs should match what's in rogue.json for consistency when ItemData is implemented
        Item dagger = new Item("ITM_DAGGER_ROGUE", "Rogue's Dagger", "A sharp, easily concealed dagger.", ItemType.WEAPON);
        dagger.addStatBonus("dexterity", 1); // Example bonus
        shadow.addItemToInventory(dagger);
        shadow.equipItem(dagger);

        Item leatherArmor = new Item("ITM_LEATHER_ARMOR_ROGUE", "Supple Leather Armor", "Light armor offering good mobility.", ItemType.ARMOR_CHEST);
        // leatherArmor.addStatBonus("defense", 1); // Example if you have a defense stat
        shadow.addItemToInventory(leatherArmor);
        shadow.equipItem(leatherArmor);

        Item thievesTools = new Item("ITM_THIEVES_TOOLS", "Thieves' Tools", "A set of lockpicks and other useful tools.", ItemType.MISCELLANEOUS);
        shadow.addItemToInventory(thievesTools);
        // Thieves' tools might not be equippable or might provide a passive bonus if Item system supported it.

        partyManager.addMember(shadow);
        Gdx.app.log("GameSetup", "Shadow (Rogue) added to party. Skills: " + shadow.getKnownSkills() + ", Items: " + shadow.getInventory());

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
