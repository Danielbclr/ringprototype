package com.danbramos.ringprototype.resources;

public enum ResourceType {
    FOOD("Food", "Sustenance for the party, consumed daily or during rests."),
    FIREWOOD("Firewood", "Used for warmth, cooking, and keeping dangers at bay during rests."),
    HERBS("Herbs", "Ingredients for crafting potions or for direct medicinal use."),
    TOOLS("Tools", "Used for repairing equipment, overcoming obstacles, or crafting."),
    HOPE("Hope", "A measure of the party's morale and determination; affects events and performance."),
    GOLD("Gold", "The primary currency for trade and services."); // Added Gold as a common currency

    private final String displayName;
    private final String description;

    ResourceType(String displayName, String description) {
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
