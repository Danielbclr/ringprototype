// In com.danbramos.ringprototype.battle.skills
package com.danbramos.ringprototype.battle.skills.executors;

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.StatusEffect;
import com.danbramos.ringprototype.battle.skills.ISkillExecutor;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.List;

public class StealthSkillExecutor implements ISkillExecutor {
    @Override
    public void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData) {
        // Stealth typically targets self (caster)
        Gdx.app.log("Stealth", caster.getName() + " uses Stealth.");
        battleScreen.getUiManager().updateBattleLog(caster.getName() + " uses Stealth and becomes INVISIBLE!");

        if (skillData.getStatusEffects() != null) {
            for (StatusEffect effectPrototype : skillData.getStatusEffects()) {
                if (effectPrototype.getType().equals("INVISIBLE")) { // Ensure it's the correct effect
                    if (Math.random() < effectPrototype.getChance()) {
                        caster.addStatusEffect(effectPrototype.copy());
                        Gdx.app.log("Stealth", "Applied INVISIBLE to " + caster.getName());
                        return; // Applied the primary effect
                    }
                }
            }
        }
    }
}
