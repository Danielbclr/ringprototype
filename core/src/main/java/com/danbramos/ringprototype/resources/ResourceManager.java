package com.danbramos.ringprototype.resources;

import com.badlogic.gdx.Gdx;
import java.util.EnumMap;
import java.util.Map;

/**
 * Interface defining resource management capabilities
 */
public interface ResourceManager {
    /**
     * Gets the current amount of a specific resource
     * @param type The resource type
     * @return The current amount
     */
    int getResourceAmount(ResourceType type);
    
    /**
     * Checks if there's enough of a specific resource
     * @param type The resource type
     * @param amountRequired The required amount
     * @return True if there's enough of the resource
     */
    boolean hasEnoughResource(ResourceType type, int amountRequired);
    
    /**
     * Spends a specific amount of a resource
     * @param type The resource type
     * @param amountToSpend The amount to spend
     * @return True if the operation was successful
     */
    boolean spendResource(ResourceType type, int amountToSpend);
    
    /**
     * Adds a specific amount of a resource
     * @param type The resource type
     * @param amountToAdd The amount to add
     */
    void addResource(ResourceType type, int amountToAdd);
    
    /**
     * Sets the resource amount directly
     * @param type The resource type
     * @param newAmount The new amount
     */
    void setResourceAmount(ResourceType type, int newAmount);
    
    /**
     * Gets all resources as a map
     * @return A map of resources and their amounts
     */
    Map<ResourceType, Integer> getAllResources();
}
