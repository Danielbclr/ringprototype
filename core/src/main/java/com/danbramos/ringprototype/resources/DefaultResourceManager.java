package com.danbramos.ringprototype.resources;

import com.badlogic.gdx.Gdx;
import java.util.EnumMap;
import java.util.Map;

/**
 * Default implementation of ResourceManager
 */
public class DefaultResourceManager implements ResourceManager {
    private final Map<ResourceType, Integer> resources;

    public DefaultResourceManager() {
        this.resources = new EnumMap<>(ResourceType.class);
        initializeDefaultResources();
    }

    private void initializeDefaultResources() {
        // Set initial amounts for each resource
        for (ResourceType type : ResourceType.values()) {
            resources.put(type, 0); // Start with 0 of everything by default
        }
        // Example: Set some starting resources
        // resources.put(ResourceType.FOOD, 10);
        // resources.put(ResourceType.GOLD, 50);
        // resources.put(ResourceType.HOPE, 100); // Hope might start high
    }

    @Override
    public int getResourceAmount(ResourceType type) {
        return resources.getOrDefault(type, 0);
    }

    @Override
    public boolean hasEnoughResource(ResourceType type, int amountRequired) {
        if (amountRequired < 0) return true; // No cost or a gain
        return getResourceAmount(type) >= amountRequired;
    }

    @Override
    public boolean spendResource(ResourceType type, int amountToSpend) {
        if (amountToSpend <= 0) {
            Gdx.app.log("ResourceManager", "Attempted to spend non-positive amount of " + type.getDisplayName() + ": " + amountToSpend);
            return true; // Spending 0 or negative is technically successful without change
        }
        if (hasEnoughResource(type, amountToSpend)) {
            resources.put(type, resources.get(type) - amountToSpend);
            Gdx.app.log("ResourceManager", "Spent " + amountToSpend + " " + type.getDisplayName() + ". Remaining: " + resources.get(type));
            // TODO: Fire an event here if other systems need to react to resource changes
            return true;
        } else {
            Gdx.app.log("ResourceManager", "Not enough " + type.getDisplayName() + " to spend " + amountToSpend + ". Required: " + amountToSpend + ", Have: " + getResourceAmount(type));
            return false;
        }
    }

    @Override
    public void addResource(ResourceType type, int amountToAdd) {
        if (amountToAdd <= 0) {
            Gdx.app.log("ResourceManager", "Attempted to add non-positive amount of " + type.getDisplayName() + ": " + amountToAdd);
            return;
        }
        resources.put(type, resources.getOrDefault(type, 0) + amountToAdd);
        Gdx.app.log("ResourceManager", "Added " + amountToAdd + " " + type.getDisplayName() + ". Total: " + resources.get(type));
        // TODO: Fire an event here
    }

    /**
     * Sets the resource amount directly. Use with caution.
     * Primarily for loading game state or specific event outcomes.
     */
    @Override
    public void setResourceAmount(ResourceType type, int newAmount) {
        if (newAmount < 0) {
            Gdx.app.error("ResourceManager", "Attempted to set negative resource amount for " + type.getDisplayName() + ": " + newAmount);
            resources.put(type, 0);
        } else {
            resources.put(type, newAmount);
        }
        Gdx.app.log("ResourceManager", type.getDisplayName() + " set to " + resources.get(type));
        // TODO: Fire an event here
    }

    @Override
    public Map<ResourceType, Integer> getAllResources() {
        return new EnumMap<>(resources); // Return a copy to prevent external modification
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ResourceManager{\n");
        for (Map.Entry<ResourceType, Integer> entry : resources.entrySet()) {
            sb.append("  ").append(entry.getKey().getDisplayName()).append(": ").append(entry.getValue()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
} 