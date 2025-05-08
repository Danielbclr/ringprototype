package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.danbramos.ringprototype.party.GameCharacter; // Use the interface

import java.util.List;

public class BattleCharacter implements IBattleActor {
    private final GameCharacter sourceCharacter;
    private Vector2 battleMapPosition;
    private int currentBattleHp;
    private boolean hasPerformedMajorActionThisTurn;
    private int remainingMovement; // Track remaining movement
    // Potentially other battle-specific stats like temporary movement boosts

    public BattleCharacter(GameCharacter sourceCharacter) {
        this.sourceCharacter = sourceCharacter;
        this.battleMapPosition = new Vector2(sourceCharacter.getBattleMapPosition()); // Initial position
        this.currentBattleHp = sourceCharacter.getHealthPoints(); // Start battle with current HP
        this.hasPerformedMajorActionThisTurn = false;
        this.remainingMovement = sourceCharacter.getMovementRange();
    }

    @Override
    public String getName() {
        return sourceCharacter.getName();
    }

    @Override
    public TextureRegion getBattleSprite() {
        return sourceCharacter.getBattleSprite();
    }

    @Override
    public Vector2 getBattleMapPosition() {
        return battleMapPosition;
    }

    @Override
    public void setBattleMapPosition(float x, float y) {
        this.battleMapPosition.set(x, y);
    }

    @Override
    public boolean isAlive() {
        return currentBattleHp > 0;
    }

    @Override
    public int getCurrentHp() {
        return currentBattleHp;
    }

    @Override
    public int getMaxHp() {
        return sourceCharacter.getMaxHealthPoints();
    }

    @Override
    public void takeDamage(int amount) {
        this.currentBattleHp -= amount;
        if (this.currentBattleHp < 0) {
            this.currentBattleHp = 0;
        }
        // Damage is applied to this battle instance.
        // Applying it to sourceCharacter happens post-battle.
    }

    @Override
    public void startTurn() {
        this.hasPerformedMajorActionThisTurn = false;
        this.remainingMovement = getMovementRange(); // Reset movement at the start of turn
        // Any other start-of-turn logic for this battle actor
    }

    @Override
    public void endTurn() {
        // Any end-of-turn logic for this battle actor
    }

    @Override
    public boolean hasPerformedMajorAction() {
        return hasPerformedMajorActionThisTurn;
    }

    @Override
    public void setHasPerformedMajorAction(boolean value) {
        this.hasPerformedMajorActionThisTurn = value;
    }

    // --- BattleCharacter specific methods ---

    public GameCharacter getSourceCharacter() {
        return sourceCharacter;
    }

    public int getMovementRange() {
        // Could be sourceCharacter.getMovementRange() or modified by battle effects
        return sourceCharacter.getMovementRange();
    }


    public List<Skill> getKnownSkills() { // Method to access skills
        return sourceCharacter.getKnownSkills();
    }

    // Call this after battle to update the persistent character
    public void applyEndOfBattleState() {
        sourceCharacter.setHealthPoints(this.currentBattleHp);
        // Potentially apply XP, status effects, etc.
    }

    // New methods for movement tracking
    public int getRemainingMovement() {
        return remainingMovement;
    }

    public void useMovement(int amount) {
        this.remainingMovement -= amount;
        if (this.remainingMovement < 0) {
            this.remainingMovement = 0;
        }
    }

    // Method to calculate Manhattan distance between current position and target
    public int calculateMovementCost(float targetX, float targetY) {
        return (int)(Math.abs(battleMapPosition.x - targetX) + Math.abs(battleMapPosition.y - targetY));
    }
}
