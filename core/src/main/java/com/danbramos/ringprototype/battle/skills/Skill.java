package com.danbramos.ringprototype.battle.skills; // Or com.danbramos.ringprototype.skills

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.StatusEffect;
import com.danbramos.ringprototype.party.GameCharacter;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Skill {
    private String id;
    private String name;
    private String description;
    private SkillType type;
    private int range; // Max range in tiles (1 for adjacent melee)
    private String damageRoll; // e.g., "1d8", "2d4"
    private int aoeRadius; // For AOE_CIRCLE, radius in tiles (0 for single target)
    private int manaCost;
    private int cooldown;
    private String requiredClass;
    private int requiredLevel = 1;
    private List<StatusEffect> statusEffects = new ArrayList<>();
    private transient ISkillExecutor executor;

    private static final Random random = new Random();

    public Skill(String id, String name, String description, SkillType type, int range, String damageRoll, int aoeRadius) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.range = range;
        this.damageRoll = damageRoll;
        this.aoeRadius = aoeRadius;
    }

    public String getId() {
        return id;
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

    public int getManaCost() {
        return manaCost;
    }

    public void setManaCost(int manaCost) {
        this.manaCost = manaCost;
    }

    public int getCooldown() {
        return cooldown;
    }

    public ISkillExecutor getExecutor() {
        return executor;
    }

    public void setExecutor(ISkillExecutor executor) {
        this.executor = executor;
    }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
    }

    public String getRequiredClass() {
        return requiredClass;
    }

    public void setRequiredClass(String requiredClass) {
        this.requiredClass = requiredClass;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public void setRequiredLevel(int requiredLevel) {
        this.requiredLevel = requiredLevel;
    }

    public List<StatusEffect> getStatusEffects() {
        return statusEffects;
    }

    public void setStatusEffects(List<StatusEffect> statusEffects) {
        this.statusEffects = statusEffects;
    }

    /**
     * Add a status effect to this skill
     * @param effect The effect to add
     */
    public void addStatusEffect(StatusEffect effect) {
        if (this.statusEffects == null) {
            this.statusEffects = new ArrayList<>();
        }
        this.statusEffects.add(effect);
    }

    /**
     * Check if character meets requirements to use this skill
     * @param character The character to check
     * @return True if requirements are met, false otherwise
     */
    public boolean meetsRequirements(GameCharacter character) {
        // Check level requirement
        if (character.getLevel() < requiredLevel) {
            return false;
        }

        // Check class requirement
        if (requiredClass != null && !requiredClass.equals("ANY")) {
            if (character.getGameClass() == null) {
                return false;
            }
            if (!character.getGameClass().name().equals(requiredClass)) {
                return false;
            }
        }

        return true;
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
        StringBuilder sb = new StringBuilder();
        sb.append(name).append(" (").append(id).append(", Range: ").append(range).append(", Dmg: ").append(damageRoll);

        if (aoeRadius > 0) {
            sb.append(", AoE Radius: ").append(aoeRadius);
        }

        if (manaCost > 0) {
            sb.append(", Mana: ").append(manaCost);
        }

        if (cooldown > 0) {
            sb.append(", CD: ").append(cooldown);
        }

        sb.append(")");
        return sb.toString();
    }

    public void performExecution(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen) {
        if (executor != null) {
            executor.execute(caster, targets, battleScreen, this);
        } else {
            // Default execution for simple skills if no specific executor is set
            Gdx.app.error("Skill", "No executor for skill: " + getName() + ". Implement default or assign one.");
            // Example default:
            // if (this.getType() == SkillType.MELEE_ATTACK || this.getType() == SkillType.RANGED_SINGLE_TARGET) {
            //     if (!targets.isEmpty()) {
            //         IBattleActor target = targets.get(0);
            //         int damage = rollDamage();
            //         target.takeDamage(damage);
            //         battleScreen.getUiManager().updateBattleLog(caster.getName() + " uses " + getName() + " on " + target.getName() + " for " + damage + " damage.");
            //     }
            // }
        }
    }
}
