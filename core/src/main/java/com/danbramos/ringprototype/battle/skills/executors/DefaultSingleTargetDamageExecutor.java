package com.danbramos.ringprototype.battle.skills.executors;

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.StatusEffect;
import com.danbramos.ringprototype.battle.skills.ISkillExecutor;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.List;

public class DefaultSingleTargetDamageExecutor implements ISkillExecutor {
    @Override
    public void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData) {
        if (targets.isEmpty()) {
            Gdx.app.log(skillData.getName(), "No target for skill.");
            battleScreen.getUiManager().updateBattleLog(caster.getName() + " uses " + skillData.getName() + " but finds no target!");
            return;
        }
        IBattleActor target = targets.get(0); // Assumes single target

        int damage = skillData.rollDamage();
        String logMessage = caster.getName() + " uses " + skillData.getName() + " on " + target.getName() + ".";

        if (damage > 0) {
            logMessage += "\nDeals " + damage + " damage!";
            target.takeDamage(damage);
        } else {
            logMessage += "\nNo damage dealt.";
        }

        // Apply status effects from the skill
        if (skillData.getStatusEffects() != null) {
            for (StatusEffect effectPrototype : skillData.getStatusEffects()) {
                if (Math.random() < effectPrototype.getChance()) {
                    if (target instanceof BattleCharacter) { // Check if target can receive status effects
                        ((BattleCharacter) target).addStatusEffect(effectPrototype.copy());
                        logMessage += "\n" + target.getName() + " is now " + effectPrototype.getType() + "!";
                    } else if (target instanceof com.danbramos.ringprototype.battle.Enemy) {
                        // Enemies might not have the addStatusEffect method directly in IBattleActor
                        // This part needs careful handling based on your Enemy class structure
                        // For now, let's assume a similar mechanism or skip for non-BattleCharacters
                        Gdx.app.log(skillData.getName(), "Attempting to apply status effect to Enemy. Ensure Enemy class can handle it.");
                    }
                }
            }
        }
        battleScreen.getUiManager().updateBattleLog(logMessage);
    }
}
