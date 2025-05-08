package com.danbramos.ringprototype.items;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Item {
    private final String id; // Unique identifier, e.g., "longsword_basic"
    private final String name;
    private final String description;
    private final ItemType type;
    private final Map<String, Integer> statBonuses; // Added for passive stat bonuses

    // Future additions: effects, value, weight, equippableSlot, etc.

    public Item(String id, String name, String description, ItemType type) {
        this.id = Objects.requireNonNull(id, "Item ID cannot be null");
        this.name = Objects.requireNonNull(name, "Item name cannot be null");
        this.description = Objects.requireNonNull(description, "Item description cannot be null");
        this.type = Objects.requireNonNull(type, "Item type cannot be null");
        this.statBonuses = new HashMap<>(); // Initialize the map
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemType getType() {
        return type;
    }

    /**
     * Adds a stat bonus to this item.
     * Stat names are case-insensitive (stored as lowercase).
     * @param statName The name of the stat (e.g., "strength", "dexterity").
     * @param bonusValue The integer value of the bonus.
     */
    public void addStatBonus(String statName, int bonusValue) {
        if (statName != null && !statName.trim().isEmpty()) {
            this.statBonuses.put(statName.trim().toLowerCase(), bonusValue);
        }
    }

    /**
     * Gets the stat bonuses provided by this item.
     * @return An unmodifiable map of stat bonuses.
     */
    public Map<String, Integer> getStatBonuses() {
        return Collections.unmodifiableMap(statBonuses);
    }

    /**
     * Gets a specific stat bonus value.
     * @param statName The name of the stat.
     * @return The bonus value, or 0 if the stat bonus is not present.
     */
    public int getStatBonus(String statName) {
        if (statName == null) return 0;
        return statBonuses.getOrDefault(statName.trim().toLowerCase(), 0);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" [").append(type.getDisplayName()).append("] (").append(description);
        if (!statBonuses.isEmpty()) {
            sb.append(" Bonuses: ").append(statBonuses);
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return id.equals(item.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
