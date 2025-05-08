package com.danbramos.ringprototype.battle; // Or com.danbramos.ringprototype.skills

import java.util.Random;

public class Skill {
    private String name;
    private String description;
    private SkillType type;
    private int range; // Max range in tiles (1 for adjacent melee)
    private String damageRoll; // e.g., "1d8", "2d4"
    private int aoeRadius; // For AOE_CIRCLE, radius in tiles (0 for single target)
    // private int manaCost;
    // private int cooldown;
    // private List<StatusEffect> effectsToApply; // Future enhancement

    private static final Random random = new Random();

    public Skill(String name, String description, SkillType type, int range, String damageRoll, int aoeRadius) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.range = range;
        this.damageRoll = damageRoll;
        this.aoeRadius = aoeRadius;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SkillType getType() {
        return type;
    }

    public int getRange() {
        return range;
    }

    public String getDamageRoll() {
        return damageRoll;
    }

    public int getAoeRadius() {
        return aoeRadius;
    }

    public int rollDamage() {
        if (damageRoll == null || damageRoll.isEmpty()) {
            return 0;
        }
        try {
            String[] parts = damageRoll.toLowerCase().split("d");
            if (parts.length != 2) {
                // Gdx.app.error("Skill", "Invalid damage roll format: " + damageRoll);
                return 0;
            }
            int numDice = Integer.parseInt(parts[0]);
            int diceSides = Integer.parseInt(parts[1]);
            int totalDamage = 0;
            for (int i = 0; i < numDice; i++) {
                totalDamage += random.nextInt(diceSides) + 1; // Rolls 1 to diceSides
            }
            return totalDamage;
        } catch (NumberFormatException e) {
            // Gdx.app.error("Skill", "Failed to parse damage roll numbers: " + damageRoll, e);
            return 0;
        } catch (Exception e) {
            // Gdx.app.error("Skill", "Failed to parse damage roll: " + damageRoll, e);
            return 0; // Default damage on error
        }
    }

    @Override
    public String toString() {
        return name + " (Range: " + range + ", Dmg: " + damageRoll +
            (aoeRadius > 0 ? ", AoE Radius: " + aoeRadius : "") + ")";
    }
}
