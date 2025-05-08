package com.danbramos.ringprototype.items;

public enum ItemType {
    WEAPON("Weapon"),
    ARMOR_HEAD("Head Armor"),
    ARMOR_CHEST("Chest Armor"),
    ARMOR_LEGS("Leg Armor"),
    ARMOR_FEET("Foot Armor"),
    ARMOR_HANDS("Hand Armor"),
    ACCESSORY("Accessory"),
    CONSUMABLE("Consumable"),
    QUEST_ITEM("Quest Item"),
    ARTIFACT("Artifact"),
    MISCELLANEOUS("Miscellaneous");

    private final String displayName;

    ItemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
