package com.danbramos.ringprototype.battle.skills.executors;

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.StatusEffect;
import com.danbramos.ringprototype.battle.skills.ISkillExecutor;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.List;

public class CripplingStrikeSkillExecutor implements ISkillExecutor {
    @Override
    public void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData) {
        if (targets.isEmpty()) {
            Gdx.app.log("CripplingStrike", "No target for Crippling Strike.");
            return;
        }
        
        IBattleActor target = targets.get(0); // Crippling Strike is single target
        
        // Calculate and apply damage
        int damage = skillData.rollDamage();
        
        // Create log message
        String logMessage = caster.getName() + " uses Crippling Strike on " + target.getName() + ".";
        if (damage > 0) {
            logMessage += "\nDeals " + damage + " damage!";
        } else {
            logMessage += "\nNo damage dealt.";
        }
        
        // Apply status effects if defined in the skill
        if (skillData.getStatusEffects() != null && !skillData.getStatusEffects().isEmpty()) {
            for (StatusEffect effectPrototype : skillData.getStatusEffects()) {
                if (Math.random() < effectPrototype.getChance()) {
                    // Create a copy of the status effect to apply to the target
                    StatusEffect effect = effectPrototype.copy();
                    
                    // Apply the effect to the target (if target supports status effects)
                    if (target instanceof BattleCharacter) {
                        ((BattleCharacter) target).addStatusEffect(effect);
                    } else if (target instanceof com.danbramos.ringprototype.battle.Enemy) {
                        // Assuming Enemy class has addStatusEffect method similar to BattleCharacter
                        try {
                            ((com.danbramos.ringprototype.battle.Enemy) target).addStatusEffect(effect);
                            logMessage += "\n" + target.getName() + " is now " + effect.getType();
                            if (effect.getType().equals("DAMAGE_REDUCTION")) {
                                logMessage += " (damage reduced by " + effect.getValue() + ")";
                            }
                        } catch (Exception e) {
                            Gdx.app.error("CripplingStrike", "Failed to apply status effect to enemy: " + e.getMessage());
                        }
                    }
                }
            }
        }
        
        // Update the battle log
        battleScreen.getUiManager().updateBattleLog(logMessage);
        
        // Apply damage last so we know if the target died from the attack
        target.takeDamage(damage);
    }
} 