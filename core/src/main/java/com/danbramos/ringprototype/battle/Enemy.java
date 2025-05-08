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

    /**
     * Get the movement range of this enemy
     * @return The movement range in tiles
     */
    public int getMovementRange() {
        return movementRange;
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
            // Handle damage rolls with modifiers like "1d6+1"
            int modifier = 0;
            String rollPart = damageRoll.toLowerCase();
            
            // Check for modifier in the format "XdY+Z" or "XdY-Z"
            if (rollPart.contains("+")) {
                String[] modifierParts = rollPart.split("\\+");
                rollPart = modifierParts[0]; // The dice part (e.g., "1d6")
                modifier = Integer.parseInt(modifierParts[1]); // The modifier (e.g., "1")
            } else if (rollPart.contains("-")) {
                String[] modifierParts = rollPart.split("-");
                rollPart = modifierParts[0]; // The dice part (e.g., "1d6")
                modifier = -Integer.parseInt(modifierParts[1]); // Negative modifier
            }
            
            // Now parse the dice part "XdY"
            String[] parts = rollPart.split("d");
            if (parts.length != 2) return 1; // Default on bad format
            
            int numDice = Integer.parseInt(parts[0]);
            int diceSides = Integer.parseInt(parts[1]);
            
            // Roll the dice
            int totalDamage = 0;
            for (int i = 0; i < numDice; i++) {
                totalDamage += random.nextInt(diceSides) + 1;
            }
            
            // Apply the modifier
            totalDamage += modifier;
            
            // Ensure minimum damage of 1
            return Math.max(1, totalDamage);
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
     * Calculates the best position to move towards a target given a maximum movement range.
     * Uses a breadth-first search approach to find the optimal path.
     * 
     * @param currentPos Current position
     * @param targetPos Target position
     * @param maxMovement Maximum movement range
     * @param battleScreen Reference to BattleScreen for utility methods
     * @return The best position to move to, or null if no valid move exists
     */
    private Vector2 moveTowardsTarget(Vector2 currentPos, Vector2 targetPos, int maxMovement, BattleScreen battleScreen) {
        // If we can't move, return null
        if (maxMovement <= 0) {
            return null;
        }
        
        Gdx.app.log(getName(), "Planning movement from " + currentPos + " toward " + targetPos + " with max movement " + maxMovement);
        
        // Initialize variables for pathfinding
        Array<PathNode> queue = new Array<>();
        Array<Vector2> visited = new Array<>();
        
        // Start with the current position
        queue.add(new PathNode(currentPos, null, 0));
        visited.add(new Vector2(currentPos));
        
        // Possible move directions (up, right, down, left)
        int[][] directions = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        
        // Best move found so far - will be set to the node closest to target
        // within movement range
        PathNode bestMove = null;
        float bestDistanceSq = Float.MAX_VALUE;
        
        // Breadth-first search to find all reachable positions
        while (queue.size > 0) {
            PathNode current = queue.removeIndex(0);
            
            // If we've moved more than our maximum, skip this node
            if (current.movesUsed > maxMovement) {
                continue;
            }
            
            // Check if this is the best move so far (closest to target)
            float distanceSq = current.position.dst2(targetPos);
            if (distanceSq < bestDistanceSq) {
                bestDistanceSq = distanceSq;
                bestMove = current;
                Gdx.app.debug(getName(), "Found better move: " + current.position + " distance: " + distanceSq + " moves used: " + current.movesUsed);
            }
            
            // If we've reached our movement limit, don't explore further from this node
            if (current.movesUsed >= maxMovement) {
                continue;
            }
            
            // Try each direction
            for (int[] dir : directions) {
                float newX = current.position.x + dir[0];
                float newY = current.position.y + dir[1];
                Vector2 newPos = new Vector2(newX, newY);
                
                // Check if the move is valid
                boolean alreadyVisited = false;
                for (Vector2 v : visited) {
                    if (v.epsilonEquals(newPos)) {
                        alreadyVisited = true;
                        break;
                    }
                }
                
                if (!alreadyVisited && battleScreen.isTileWithinMapBounds(newX, newY)) {
                    // Don't check for occupation for the final position if we're trying to move next to the target
                    boolean isValidPosition = true;
                    
                    // Skip occupation check if this would be adjacent to the target
                    boolean adjacentToTarget = Math.abs(newX - targetPos.x) + Math.abs(newY - targetPos.y) <= 1;
                    if (!adjacentToTarget) {
                        isValidPosition = !battleScreen.isTileOccupied(newX, newY);
                    }
                    
                    if (isValidPosition) {
                        PathNode newNode = new PathNode(newPos, current, current.movesUsed + 1);
                        queue.add(newNode);
                        visited.add(newPos);
                    }
                }
            }
        }
        
        // If we found a valid move
        if (bestMove != null) {
            Gdx.app.log(getName(), "Best move is at " + bestMove.position + " with " + bestMove.movesUsed + " moves used");
            
            // Trace back to the first move in the path
            PathNode pathNode = bestMove;
            
            // If this is the starting position, return null (no movement)
            if (pathNode.parent == null) {
                Gdx.app.log(getName(), "No valid movement found - staying at current position");
                return null;
            }
            
            // Trace back the path to the first step
            while (pathNode.parent != null && pathNode.parent.parent != null) {
                pathNode = pathNode.parent;
            }
            
            // Log the full path for debugging
            StringBuilder pathLog = new StringBuilder("Movement path: ");
            PathNode temp = bestMove;
            while (temp != null) {
                pathLog.append(temp.position);
                if (temp.parent != null) {
                    pathLog.append(" <- ");
                }
                temp = temp.parent;
            }
            Gdx.app.log(getName(), pathLog.toString());
            
            return pathNode.position;
        }
        
        Gdx.app.log(getName(), "No valid path found");
        return null;
    }
    
    /**
     * Checks if a move to the given position is valid.
     */
    private boolean isValidMove(float x, float y, Array<Vector2> visited, BattleScreen battleScreen) {
        // Check if out of bounds
        if (!battleScreen.isTileWithinMapBounds(x, y)) {
            return false;
        }
        
        // Check if occupied by another actor
        if (battleScreen.isTileOccupied(x, y)) {
            return false;
        }
        
        // Check if already visited in our pathfinding
        for (Vector2 pos : visited) {
            if (pos.epsilonEquals(x, y)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Contains the improved AI logic for the enemy's turn.
     * 1. Find the closest alive BattleCharacter.
     * 2. Calculate if we can reach and attack the player this turn.
     * 3. If can reach, move and attack.
     * 4. If cannot reach, move towards them using full movement range.
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
        Gdx.app.log(getName(), "Distance to " + closestTarget.getName() + ": " + manhattanDistance + ", movement range: " + this.movementRange);

        // 2. Attack if already adjacent
        if (manhattanDistance <= 1) { 
            Gdx.app.log(getName(), "is adjacent to " + closestTarget.getName() + ". Attacking!");
            int damage = rollDamage();
            closestTarget.takeDamage(damage);
            Gdx.app.log(getName(), "attacked " + closestTarget.getName() + " for " + damage + " damage.");
            setHasPerformedMajorAction(true);
            return;
        }

        // 3. Check if we can reach and attack the player with our movement
        if (this.movementRange >= manhattanDistance - 1) {
            Gdx.app.log(getName(), "Can reach " + closestTarget.getName() + " this turn!");
            
            // Move close enough to attack (leaving 1 space for adjacency)
            Vector2 finalPos = moveTowardsTarget(currentPos, targetPos, manhattanDistance - 1, battleScreen);
            
            if (finalPos != null) {
                // Verify we actually moved
                int newManhattanDistance = (int) (Math.abs(finalPos.x - targetPos.x) + Math.abs(finalPos.y - targetPos.y));
                Gdx.app.log(getName(), "New distance after movement: " + newManhattanDistance);
                
                // If we successfully moved close enough
                setBattleMapPosition(finalPos.x, finalPos.y);
                Gdx.app.log(getName(), "Moved to (" + finalPos.x + "," + finalPos.y + ") to attack " + closestTarget.getName());
                
                // Now attack if we're adjacent
                if (newManhattanDistance <= 1) {
                    int damage = rollDamage();
                    closestTarget.takeDamage(damage);
                    Gdx.app.log(getName(), "attacked " + closestTarget.getName() + " for " + damage + " damage.");
                }
                setHasPerformedMajorAction(true);
                return;
            }
        }

        // 4. If we can't reach to attack, move as far as possible toward the target
        Vector2 bestPos = moveTowardsTarget(currentPos, targetPos, this.movementRange, battleScreen);
        
        if (bestPos != null) {
            // Verify we're actually moving
            if (!bestPos.epsilonEquals(currentPos)) {
                Gdx.app.log(getName(), "Moving from " + currentPos + " to " + bestPos + " towards " + closestTarget.getName());
                setBattleMapPosition(bestPos.x, bestPos.y);
            } else {
                Gdx.app.log(getName(), "Best move is current position - no change needed");
            }
            setHasPerformedMajorAction(true);
            return;
        }
        
        // If all movement options are blocked
        Gdx.app.log(getName(), "Could not find a valid move towards " + closestTarget.getName() + ". Staying put.");
        setHasPerformedMajorAction(true);
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
    
    /**
     * Helper class for pathfinding
     */
    private static class PathNode {
        Vector2 position;
        PathNode parent;
        int movesUsed;
        
        PathNode(Vector2 position, PathNode parent, int movesUsed) {
            this.position = position;
            this.parent = parent;
            this.movesUsed = movesUsed;
        }
    }
}
