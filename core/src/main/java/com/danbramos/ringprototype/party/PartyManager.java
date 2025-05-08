package com.danbramos.ringprototype.party;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array; // LibGDX Array is often preferred over ArrayList in LibGDX
import java.util.ArrayList; // Import standard ArrayList
import java.util.Collections;
import java.util.List;

public class PartyManager {
    private final Array<Character> members;
    private TextureRegion partyMarkerSprite;
    private Vector2 mapPosition;

    public PartyManager() {
        this.members = new Array<>();
        this.mapPosition = new Vector2(5, 5);
    }

    public void addMember(Character character) {
        if (character != null && !members.contains(character, true)) {
            members.add(character);
            // Gdx.app.log("PartyManager", character.getName() + " joined the party.");
        }
    }

    public void removeMember(Character character) {
        if (character != null) {
            members.removeValue(character, true);
            // Gdx.app.log("PartyManager", character.getName() + " left the party.");
        }
    }

    public Array<Character> getMembers() {
        return members; // Returns the actual array, could return a copy if immutability is strictly needed outside
    }

    public List<Character> getMembersAsList() {
        // Provides a standard Java List view if needed, though Array is efficient
        // Convert LibGDX Array to a standard Java ArrayList first
        ArrayList<Character> javaList = new ArrayList<>();
        for (Character member : members) { // LibGDX Array is iterable
            javaList.add(member);
        }
        return Collections.unmodifiableList(javaList); // Now pass the ArrayList
    }

    public int getPartySize() {
        return members.size;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public TextureRegion getPartyMarkerSprite() {
        return partyMarkerSprite;
    }

    public void setPartyMarkerSprite(TextureRegion sprite) {
        this.partyMarkerSprite = sprite;
    }

    public Vector2 getMapPosition() {
        return mapPosition;
    }

    public void setMapPosition(float x, float y) {
        this.mapPosition.set(x, y);
    }
}
