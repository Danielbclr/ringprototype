package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.party.GameCharacter; // Use the interface

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

public class BattleCharacter implements IBattleActor {
    private final GameCharacter sourceCharacter;
    private Vector2 battleMapPosition;
    private int currentBattleHp;
    private boolean hasPerformedMajorActionThisTurn;
    private int remainingMovement; // Track remaining movement
    private List<StatusEffect> activeEffects;

    public BattleCharacter(GameCharacter sourceCharacter) {
        this.sourceCharacter = sourceCharacter;
        this.battleMapPosition = new Vector2(sourceCharacter.getBattleMapPosition()); // Initial position
        this.currentBattleHp = sourceCharacter.getHealthPoints(); // Start battle with current HP
        this.hasPerformedMajorActionThisTurn = false;
        this.remainingMovement = sourceCharacter.getMovementRange();
        this.activeEffects = new ArrayList<>();
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
        int modifiedAmount = amount;
        for (StatusEffect effect : activeEffects) {
            if (effect.getType().equals("DAMAGE_REDUCTION") && effect.getRemainingDuration() > 0) {
                modifiedAmount -= effect.getValue();
            }
        }
        modifiedAmount = Math.max(0, modifiedAmount); // Ensure damage is not negative

        this.currentBattleHp -= modifiedAmount;
        if (this.currentBattleHp < 0) {
            this.currentBattleHp = 0;
        }
        // Damage is applied to this battle instance.
        // Applying it to sourceCharacter happens post-battle.
    }

    @Override
    public void startTurn() {
        this.hasPerformedMajorActionThisTurn = false;
        tickStatusEffects(); // Tick effects at the start of the turn
        this.remainingMovement = getMovementRange(); // Reset movement, considering effects
    }

    private void tickStatusEffects() {
        Iterator<StatusEffect> iterator = activeEffects.iterator();
        while (iterator.hasNext()) {
            StatusEffect effect = iterator.next();
            if (!effect.tick()) { // tick() decrements duration and returns false if expired
                iterator.remove();
                // TODO: Add Gdx.app.log for effect removal if desired
            }
        }
    }

    @Override
    public void endTurn() {
        // Tick status effects at the end of turn as well, or just at start?
        // For now, only at start. Can be adjusted if effects need to expire precisely at end of turn before next actor.
    }

    @Override
    public boolean hasPerformedMajorAction() {
        return hasPerformedMajorActionThisTurn;
    }

    @Override
    public void setHasPerformedMajorAction(boolean value) {
        this.hasPerformedMajorActionThisTurn = value;
    }

    // --- Status Effect Management ---
    public void addStatusEffect(StatusEffect newEffect) {
        if (newEffect == null) return;

        // Check for existing effect of the same type
        boolean effectExists = false;
        for (StatusEffect existingEffect : activeEffects) {
            if (existingEffect.getType().equals(newEffect.getType())) {
                existingEffect.reset(); // Refresh duration of existing effect
                // Optionally, update value if the new effect has a stronger value, depending on game rules
                // For now, just refresh duration.
                effectExists = true;
                break;
            }
        }
        if (!effectExists) {
            activeEffects.add(newEffect.copy()); // Add a copy to prevent external modification issues
        }
        // TODO: Add Gdx.app.log for effect application if desired
    }

    public void removeStatusEffect(StatusEffect effectToRemove) {
        if (effectToRemove == null) return;
        activeEffects.removeIf(effect -> effect.getType().equals(effectToRemove.getType()));
    }

    public boolean hasStatusEffect(String effectType) {
        for (StatusEffect effect : activeEffects) {
            if (effect.getType().equals(effectType) && effect.getRemainingDuration() > 0) {
                return true;
            }
        }
        return false;
    }

    public List<StatusEffect> getActiveEffects() {
        return Collections.unmodifiableList(activeEffects);
    }

    // --- BattleCharacter specific methods ---

    public GameCharacter getSourceCharacter() {
        return sourceCharacter;
    }

    public int getMovementRange() {
        int baseMovement = sourceCharacter.getMovementRange();
        int bonusMovement = 0;
//        for (StatusEffect effect : activeEffects) {
//            if (effect.getType().equals("NIMBLE_MOVEMENT_ACTIVE") && effect.getRemainingDuration() > 0) {
//                bonusMovement += effect.getValue();
//            }
//        }
        return Math.max(0, baseMovement + bonusMovement);
    }

    public List<Skill> getKnownSkills() { // Method to access skills
        return sourceCharacter.getKnownSkills();
    }

    // Call this after battle to update the persistent character
    public void applyEndOfBattleState() {
        sourceCharacter.setHealthPoints(this.currentBattleHp);
        // Potentially apply XP, persistent status effects, etc. to sourceCharacter
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
