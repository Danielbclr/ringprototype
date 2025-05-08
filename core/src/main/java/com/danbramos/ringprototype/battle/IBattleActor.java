package com.danbramos.ringprototype.battle; // Or a common combat package

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;

public interface IBattleActor {

    String getName();
    TextureRegion getBattleSprite();
    Vector2 getBattleMapPosition();
    void setBattleMapPosition(float x, float y);

    boolean isAlive();
    int getCurrentHp();
    int getMaxHp();
    void takeDamage(int amount);
    // void heal(int amount); // If healing is a common action

    // Turn-based actions
    void startTurn(); // Called when this actor's turn begins
    void endTurn();   // Called when this actor's turn ends (might not be needed if BattleScreen manages all turn state)
    boolean hasPerformedMajorAction(); // e.g., moved, attacked
    void setHasPerformedMajorAction(boolean value); // Or more granular: hasMoved, hasAttacked

    // Potentially other common battle attributes/methods
    // int getInitiative();
    // Faction getFaction(); // To distinguish between player, enemy, neutral
}
