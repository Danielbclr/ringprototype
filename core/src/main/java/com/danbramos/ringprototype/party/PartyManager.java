package com.danbramos.ringprototype.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array; // LibGDX Array is often preferred over ArrayList in LibGDX
import java.util.List;

/**
 * Interface defining party management capabilities
 */
public interface PartyManager {
    /**
     * Adds a character to the party
     * @param character The character to add
     */
    void addMember(GameCharacter character);
    
    /**
     * Removes a character from the party
     * @param character The character to remove
     */
    void removeMember(GameCharacter character);
    
    /**
     * Gets all party members as a LibGDX Array
     * @return Array of party members
     */
    Array<GameCharacter> getMembers();
    
    /**
     * Gets all party members as a standard Java List
     * @return List of party members
     */
    List<GameCharacter> getMembersAsList();
    
    /**
     * Gets the party size
     * @return The number of party members
     */
    int getPartySize();
    
    /**
     * Checks if the party is empty
     * @return True if the party has no members
     */
    boolean isEmpty();
    
    /**
     * Gets the sprite representing the party on the map
     * @return The party marker sprite
     */
    TextureRegion getPartyMarkerSprite();
    
    /**
     * Sets the sprite representing the party on the map
     * @param sprite The sprite to use
     */
    void setPartyMarkerSprite(TextureRegion sprite);
    
    /**
     * Gets the party's position on the map
     * @return The party's position
     */
    Vector2 getMapPosition();
    
    /**
     * Sets the party's position on the map
     * @param x The x coordinate
     * @param y The y coordinate
     */
    void setMapPosition(float x, float y);
}
