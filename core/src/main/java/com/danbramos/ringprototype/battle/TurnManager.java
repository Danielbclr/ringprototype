package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.danbramos.ringprototype.party.GameCharacter;

import java.util.Comparator;

/**
 * Manages the turn order and state for a battle.
 */
public class TurnManager {
    private Array<IBattleActor> turnOrder;
    private int currentTurnIndex;
    private IBattleActor currentTurnActor;

    public TurnManager() {
        this.turnOrder = new Array<>();
        this.currentTurnIndex = -1;
        this.currentTurnActor = null;
    }

    /**
     * Initializes the turn order based on the provided party members and enemies.
     * Sorts the order (e.g., players first).
     *
     * @param partyMembers Player characters involved in the battle.
     * @param enemies      Enemy actors involved in the battle.
     */
    public void initializeTurnOrder(Array<GameCharacter> partyMembers, Array<Enemy> enemies) {
        turnOrder.clear();
        for (GameCharacter character : partyMembers) {
            if (character != null && character.getHealthPoints() > 0) {
                turnOrder.add(new BattleCharacter(character)); // Wrap GameCharacter in BattleCharacter
            }
        }
        for (Enemy enemy : enemies) {
            if (enemy != null && enemy.isAlive()) {
                turnOrder.add(enemy);
            }
        }

        if (turnOrder.size > 0) {
            // Simple initiative: players first, then enemies (can be expanded)
            turnOrder.sort(Comparator.comparingInt(a -> (a instanceof BattleCharacter) ? 0 : 1));

            currentTurnIndex = 0;
            currentTurnActor = turnOrder.get(currentTurnIndex);
            Gdx.app.log("TurnManager", "Turn order initialized. First turn: " + currentTurnActor.getName());
        } else {
            currentTurnIndex = -1;
            currentTurnActor = null;
            Gdx.app.error("TurnManager", "No combatants to initialize turn order.");
        }
    }

    /**
     * Advances to the next actor in the turn order.
     * Automatically removes defeated actors.
     *
     * @return The new current actor, or null if the battle should end.
     */
    public IBattleActor advanceTurn() {
        if (turnOrder.isEmpty()) {
            currentTurnActor = null;
            return null;
        }

        // Remove defeated actors before proceeding
        removeDefeatedActors();

        // Check if battle is over after removing actors
        if (isBattleOver()) {
             currentTurnActor = null;
             return null;
        }
        
        // If the turn order became empty after removals (should be caught by isBattleOver, but safeguard)
        if (turnOrder.isEmpty()){
             currentTurnActor = null;
             return null;
        }

        currentTurnIndex = (currentTurnIndex + 1) % turnOrder.size;
        currentTurnActor = turnOrder.get(currentTurnIndex);
        Gdx.app.log("TurnManager", "Advanced turn. Next actor: " + currentTurnActor.getName());
        return currentTurnActor;
    }

    /**
     * Removes any actors from the turn order who are no longer alive.
     */
    public void removeDefeatedActors() {
         boolean requiresReSort = false;
         for (int i = turnOrder.size - 1; i >= 0; i--) {
            IBattleActor actor = turnOrder.get(i);
            if (!actor.isAlive()) {
                Gdx.app.log("TurnManager", actor.getName() + " is defeated, removing from turn order.");
                turnOrder.removeIndex(i);
                 // If the removed actor was the current one, reset index carefully before next advanceTurn
                if (actor == currentTurnActor) {
                     // Adjust index so the next advance lands correctly
                    currentTurnIndex = (i - 1 + turnOrder.size) % Math.max(1, turnOrder.size); 
                     currentTurnActor = null; // Actor is gone
                 }
                requiresReSort = true; // Optional: Re-sort if order matters strictly even after removals
            }
        }
         // Reset index if the list became empty
         if (turnOrder.isEmpty()) {
             currentTurnIndex = -1;
         }
         // If turn order stability matters after removals, uncomment the sort:
         // if (requiresReSort && !turnOrder.isEmpty()) { 
         //    turnOrder.sort(Comparator.comparingInt(a -> (a instanceof BattleCharacter) ? 0 : 1));
         //    currentTurnIndex = turnOrder.indexOf(currentTurnActor, true); // Find new index if actor still exists
         // }
    }

    /**
     * Checks if the battle is over (either no players or no enemies left alive).
     *
     * @return True if the battle is over, false otherwise.
     */
    public boolean isBattleOver() {
        if (turnOrder.isEmpty()) return true; // No one left

        boolean playersAlive = false;
        boolean enemiesAlive = false;
        for (IBattleActor actor : turnOrder) {
            if (actor.isAlive()) {
                if (actor instanceof BattleCharacter) {
                    playersAlive = true;
                } else if (actor instanceof Enemy) {
                    enemiesAlive = true;
                }
            }
            // Optimization: if both are found, no need to continue loop
            if (playersAlive && enemiesAlive) {
                return false;
            }
        }
        // If the loop finishes, at least one side has no living members
        return !playersAlive || !enemiesAlive;
    }

    /**
     * Gets the current actor whose turn it is.
     *
     * @return The current IBattleActor.
     */
    public IBattleActor getCurrentActor() {
        return currentTurnActor;
    }
    
    /**
     * Gets the complete current turn order list.
     * Use with caution, modifying this list externally is not recommended.
     * 
     * @return The Array of actors in turn order.
     */
     public Array<IBattleActor> getTurnOrder() {
         return turnOrder;
     }
} 