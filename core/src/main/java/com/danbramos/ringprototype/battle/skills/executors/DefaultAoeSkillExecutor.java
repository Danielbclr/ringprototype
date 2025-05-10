package com.danbramos.ringprototype.battle.skills.executors;

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.StatusEffect;
import com.danbramos.ringprototype.battle.skills.ISkillExecutor;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.List;

public class DefaultAoeSkillExecutor implements ISkillExecutor {
    @Override
    public void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData) {
        if (targets.isEmpty()) {
            Gdx.app.log(skillData.getName(), "No targets in AoE for skill.");
            battleScreen.getUiManager().updateBattleLog(caster.getName() + " uses " + skillData.getName() + " but hits nothing!");
            return;
        }

        String baseLogMessage = caster.getName() + " uses " + skillData.getName() + "!";
        battleScreen.getUiManager().updateBattleLog(baseLogMessage);

        for (IBattleActor target : targets) {
            if (target.isAlive()) {
                int damage = skillData.rollDamage(); // Roll damage for each target individually
                String targetLogMessage = "";

                if (damage > 0) {
                    targetLogMessage = "\n" + target.getName() + " takes " + damage + " damage!";
                    target.takeDamage(damage);
                } else {
                    targetLogMessage = "\n" + target.getName() + " takes no damage.";
                }

                // Apply status effects from the skill
                if (skillData.getStatusEffects() != null) {
                    for (StatusEffect effectPrototype : skillData.getStatusEffects()) {
                        if (Math.random() < effectPrototype.getChance()) {
                            if (target instanceof BattleCharacter) {
                                ((BattleCharacter) target).addStatusEffect(effectPrototype.copy());
                                targetLogMessage += " And is now " + effectPrototype.getType() + "!";
                            } else if (target instanceof com.danbramos.ringprototype.battle.Enemy) {
                                // Again, ensure Enemy can handle status effects or adapt this
                                Gdx.app.log(skillData.getName(), "Attempting to apply status effect to Enemy: " + target.getName());
                            }
                        }
                    }
                }
                battleScreen.getUiManager().updateBattleLog(targetLogMessage);
            }
        }
    }
}
