package com.danbramos.ringprototype.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Default implementation of PartyManager
 */
public class DefaultPartyManager implements PartyManager {
    private final Array<GameCharacter> members;
    private TextureRegion partyMarkerSprite;
    private Vector2 mapPosition;

    public DefaultPartyManager() {
        this.members = new Array<>();
        this.mapPosition = new Vector2(5, 5);
    }

    @Override
    public void addMember(GameCharacter character) {
        if (character != null && !members.contains(character, true)) {
            members.add(character);
            // Gdx.app.log("PartyManager", character.getName() + " joined the party.");
        }
    }

    @Override
    public void removeMember(GameCharacter character) {
        if (character != null) {
            members.removeValue(character, true);
            // Gdx.app.log("PartyManager", character.getName() + " left the party.");
        }
    }

    @Override
    public Array<GameCharacter> getMembers() {
        return members; // Returns the actual array, could return a copy if immutability is strictly needed outside
    }

    @Override
    public List<GameCharacter> getMembersAsList() {
        // Provides a standard Java List view if needed, though Array is efficient
        // Convert LibGDX Array to a standard Java ArrayList first
        ArrayList<GameCharacter> javaList = new ArrayList<>();
        for (GameCharacter member : members) { // LibGDX Array is iterable
            javaList.add(member);
        }
        return Collections.unmodifiableList(javaList); // Now pass the ArrayList
    }

    @Override
    public int getPartySize() {
        return members.size;
    }

    @Override
    public boolean isEmpty() {
        return members.isEmpty();
    }

    @Override
    public TextureRegion getPartyMarkerSprite() {
        return partyMarkerSprite;
    }

    @Override
    public void setPartyMarkerSprite(TextureRegion sprite) {
        this.partyMarkerSprite = sprite;
    }

    @Override
    public Vector2 getMapPosition() {
        return mapPosition;
    }

    @Override
    public void setMapPosition(float x, float y) {
        this.mapPosition.set(x, y);
    }
} 