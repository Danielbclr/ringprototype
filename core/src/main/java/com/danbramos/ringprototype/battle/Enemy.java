package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.Gdx; // Import Gdx for logging
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array; // Import Array for allCombatants
import com.danbramos.ringprototype.screens.BattleScreen; // To use utility methods like isTileOccupied

import java.util.Random;

public class Enemy implements IBattleActor {
    private String name;
    private int currentHp;
    private int maxHp;
    private String damageRoll; // e.g., "1d6"
    private transient TextureRegion battleSprite;
    private Vector2 battleMapPosition;
    private boolean hasTakenTurn;
    private int movementRange; // Added movement range

    private static final Random random = new Random();

    public Enemy(String name, int maxHp, String damageRoll, TextureRegion battleSprite, float startX, float startY, int movementRange) {
        this.name = name;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.damageRoll = damageRoll;
        this.battleSprite = battleSprite;
        this.battleMapPosition = new Vector2(startX, startY);
        this.hasTakenTurn = false;
        this.movementRange = movementRange; // Initialize movement range
    }

    // ... (getName, getCurrentHp, getMaxHp, getBattleSprite, getBattleMapPosition, setBattleMapPosition are the same) ...
    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getCurrentHp() {
        return currentHp;
    }

    @Override
    public int getMaxHp() {
        return maxHp;
    }

    @Override
    public TextureRegion getBattleSprite() {
        return battleSprite;
    }

    @Override
    public Vector2 getBattleMapPosition() {
        return battleMapPosition;
    }

    @Override
    public void setBattleMapPosition(float x, float y) {
        this.battleMapPosition.set(x, y);
    }


    // This method was specific to Enemy, now maps to hasPerformedMajorAction
    public boolean hasTakenTurn() {
        return hasTakenTurn;
    }

    // This method was specific to Enemy, now maps to setHasPerformedMajorAction
    public void setHasTakenTurn(boolean hasTakenTurn) {
        this.hasTakenTurn = hasTakenTurn;
    }

    @Override
    public void takeDamage(int amount) {
        this.currentHp -= amount;
        if (this.currentHp < 0) {
            this.currentHp = 0;
        }
        Gdx.app.log(getName(), "took " + amount + " damage. HP: " + currentHp + "/" + maxHp);
    }

    @Override
    public boolean isAlive() {
        return this.currentHp > 0;
    }

    public int rollDamage() {
        if (damageRoll == null || damageRoll.isEmpty()) {
            return 0;
        }
        try {
            String[] parts = damageRoll.toLowerCase().split("d");
            if (parts.length != 2) return 1; // Default on bad format
            int numDice = Integer.parseInt(parts[0]);
            int diceSides = Integer.parseInt(parts[1]);
            int totalDamage = 0;
            for (int i = 0; i < numDice; i++) {
                totalDamage += random.nextInt(diceSides) + 1;
            }
            return totalDamage;
        } catch (Exception e) {
            Gdx.app.error(getName(), "Failed to parse damage roll: " + damageRoll, e);
            return 1; // Default damage on error
        }
    }

    @Override
    public void startTurn() {
        this.setHasTakenTurn(false);
        Gdx.app.log(getName(), "'s turn started.");
    }

    @Override
    public void endTurn() {
        Gdx.app.log(getName(), "'s turn ended.");
    }

    @Override
    public boolean hasPerformedMajorAction() {
        return this.hasTakenTurn;
    }

    @Override
    public void setHasPerformedMajorAction(boolean value) {
        this.setHasTakenTurn(value);
    }

    /**
     * Contains the simple AI logic for the enemy's turn.
     * 1. Find the closest alive BattleCharacter.
     * 2. If adjacent, attack.
     * 3. Else, if has movement, move one step towards them.
     * 4. Mark action as performed.
     *
     * @param allCombatants List of all actors in the battle.
     * @param battleScreen  Reference to BattleScreen for utility methods (e.g., isTileOccupied, map dimensions).
     */
    public void performSimpleAI(Array<IBattleActor> allCombatants, BattleScreen battleScreen) {
        if (hasPerformedMajorAction() || !isAlive()) {
            return;
        }

        Gdx.app.log(getName(), "is thinking...");

        // 1. Find Target
        BattleCharacter closestTarget = null;
        float minDistanceSq = Float.MAX_VALUE;
        Vector2 currentPos = getBattleMapPosition();

        for (IBattleActor actor : allCombatants) {
            if (actor instanceof BattleCharacter && actor.isAlive()) {
                BattleCharacter bc = (BattleCharacter) actor;
                float distSq = currentPos.dst2(bc.getBattleMapPosition());
                if (distSq < minDistanceSq) {
                    minDistanceSq = distSq;
                    closestTarget = bc;
                }
            }
        }

        if (closestTarget == null) {
            Gdx.app.log(getName(), "No target found or all targets defeated.");
            setHasPerformedMajorAction(true); // No action to take
            return;
        }

        Vector2 targetPos = closestTarget.getBattleMapPosition();
        int manhattanDistance = (int) (Math.abs(currentPos.x - targetPos.x) + Math.abs(currentPos.y - targetPos.y));

        // 2. Attack if adjacent
        if (manhattanDistance <= 1) { // Adjacent (range of 1 for melee)
            Gdx.app.log(getName(), "is adjacent to " + closestTarget.getName() + ". Attacking!");
            int damage = rollDamage();
            closestTarget.takeDamage(damage);
            Gdx.app.log(getName(), "attacked " + closestTarget.getName() + " for " + damage + " damage.");
            setHasPerformedMajorAction(true);
            return;
        }

        // 3. Move if not adjacent (and has movement range)
        if (this.movementRange > 0) {
            float moveX = currentPos.x;
            float moveY = currentPos.y;

            float dx = targetPos.x - currentPos.x;
            float dy = targetPos.y - currentPos.y;

            // Determine primary direction of movement
            boolean movedHorizontally = false;
            boolean movedVertically = false;

            if (Math.abs(dx) > Math.abs(dy)) {
                moveX += Math.signum(dx);
                movedHorizontally = true;
            } else if (Math.abs(dy) > Math.abs(dx)) {
                moveY += Math.signum(dy);
                movedVertically = true;
            } else if (dx != 0) { // Diagonal tie, prioritize X
                moveX += Math.signum(dx);
                movedHorizontally = true;
            } else if (dy != 0) { // Diagonal tie, prioritize Y (only if dx was 0)
                moveY += Math.signum(dy);
                movedVertically = true;
            }

            // Check if the primary proposed move is valid
            if (battleScreen.isTileWithinMapBounds(moveX, moveY) && !battleScreen.isTileOccupiedByAlly(moveX, moveY, this)) {
                Gdx.app.log(getName(), "Moving from " + getBattleMapPosition() + " to (" + moveX + "," + moveY + ") towards " + closestTarget.getName());
                setBattleMapPosition(moveX, moveY);
                setHasPerformedMajorAction(true);
                return;
            } else if (movedHorizontally && dy != 0) { // Primary horizontal move failed, try vertical if possible
                moveX = currentPos.x; // Reset horizontal attempt
                moveY = currentPos.y + Math.signum(dy);
                if (battleScreen.isTileWithinMapBounds(moveX, moveY) && !battleScreen.isTileOccupiedByAlly(moveX, moveY, this)) {
                    Gdx.app.log(getName(), "Moving (alt vertical) from " + getBattleMapPosition() + " to (" + moveX + "," + moveY + ") towards " + closestTarget.getName());
                    setBattleMapPosition(moveX, moveY);
                    setHasPerformedMajorAction(true);
                    return;
                }
            } else if (movedVertically && dx != 0) { // Primary vertical move failed, try horizontal if possible
                moveY = currentPos.y; // Reset vertical attempt
                moveX = currentPos.x + Math.signum(dx);
                if (battleScreen.isTileWithinMapBounds(moveX, moveY) && !battleScreen.isTileOccupiedByAlly(moveX, moveY, this)) {
                    Gdx.app.log(getName(), "Moving (alt horizontal) from " + getBattleMapPosition() + " to (" + moveX + "," + moveY + ") towards " + closestTarget.getName());
                    setBattleMapPosition(moveX, moveY);
                    setHasPerformedMajorAction(true);
                    return;
                }
            }
            Gdx.app.log(getName(), "Could not find a valid move towards " + closestTarget.getName() + ". Staying put.");
        } else {
            Gdx.app.log(getName(), "Cannot move (no movement range).");
        }

        setHasPerformedMajorAction(true); // Mark action as performed even if only thought or couldn't move
    }


    @Override
    public String toString() {
        return "Enemy{" +
            "name='" + name + '\'' +
            ", HP=" + currentHp + "/" + maxHp +
            ", damage='" + damageRoll + '\'' +
            ", move=" + movementRange + // Added move to toString
            ", pos=" + battleMapPosition +
            '}';
    }
}
