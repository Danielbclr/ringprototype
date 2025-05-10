package com.danbramos.ringprototype.battle;

/**
 * Represents a status effect that can be applied to characters
 */
public class StatusEffect {
    private String type;
    private float chance;
    private int duration;
    private int remainingDuration;
    private int value; // Added for magnitude of the effect
    
    /**
     * Constructor for a status effect
     * 
     * @param type The type of effect
     * @param chance The chance to apply it (0-1)
     * @param duration The duration in turns
     * @param value The magnitude of the effect (e.g., damage amount, stat change)
     */
    public StatusEffect(String type, float chance, int duration, int value) {
        this.type = type;
        this.chance = chance;
        this.duration = duration;
        this.remainingDuration = duration;
        this.value = value;
    }

    /**
     * Constructor for a status effect without a specific value (defaults to 0 or not applicable)
     */
    public StatusEffect(String type, float chance, int duration) {
        this(type, chance, duration, 0); // Default value to 0
    }
    
    /**
     * Get the effect type
     * @return The effect type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Get the chance to apply the effect
     * @return The chance (0-1)
     */
    public float getChance() {
        return chance;
    }
    
    /**
     * Get the total duration of the effect
     * @return The duration in turns
     */
    public int getDuration() {
        return duration;
    }
    
    /**
     * Get the remaining duration of the effect
     * @return The remaining duration in turns
     */
    public int getRemainingDuration() {
        return remainingDuration;
    }

    /**
     * Get the magnitude of the effect
     * @return The value of the effect
     */
    public int getValue() {
        return value;
    }
    
    /**
     * Decrement the remaining duration by 1
     * @return True if the effect is still active, false if expired
     */
    public boolean tick() {
        if (remainingDuration > 0) {
            remainingDuration--;
        }
        return remainingDuration > 0;
    }
    
    /**
     * Reset the effect's remaining duration to the initial duration
     */
    public void reset() {
        this.remainingDuration = this.duration;
    }
    
    /**
     * Create a copy of this effect
     * @return A new instance with the same properties
     */
    public StatusEffect copy() {
        return new StatusEffect(type, chance, duration, value);
    }
    
    @Override
    public String toString() {
        String str = type + " (" + (int)(chance * 100) + "%, " + remainingDuration + "/" + duration + " turns";
        if (value != 0) {
            str += ", val: " + value;
        }
        str += ")";
        return str;
    }
} 