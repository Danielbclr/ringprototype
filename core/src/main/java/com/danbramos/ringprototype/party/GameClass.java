package com.danbramos.ringprototype.party;

public enum GameClass {
    WARRIOR("Warrior", "A brave fighter, skilled in melee combat."),
    MAGE("Mage", "A wielder of arcane energies, casting powerful spells."),
    ROGUE("Rogue", "A cunning operative, excelling in stealth and precision."),
    RANGER("Ranger", "A master of the wilds, adept with bows and survival skills.");
    // Add more classes as your design evolves

    private final String displayName;
    private final String description;

    GameClass(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
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
