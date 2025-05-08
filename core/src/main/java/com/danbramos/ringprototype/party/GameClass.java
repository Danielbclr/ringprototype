package com.danbramos.ringprototype.party;

public enum GameClass {
    WARRIOR("warrior", "Warrior", "A brave fighter, skilled in melee combat."),
    MAGE("mage", "Mage", "A wielder of arcane energies, casting powerful spells."),
    ROGUE("rogue", "Rogue", "A cunning operative, excelling in stealth and precision."),
    RANGER("ranger", "Ranger", "A master of the wilds, adept with bows and survival skills.");
    // Add more classes as your design evolves

    private final String id;
    private final String displayName;
    private final String description;

    GameClass(String id, String displayName, String description) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
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

    @Override
    public String toString() {
        return displayName;
    }
}
