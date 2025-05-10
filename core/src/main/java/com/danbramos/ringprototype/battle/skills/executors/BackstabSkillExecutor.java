package com.danbramos.ringprototype.battle.skills.executors;

import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.battle.skills.ISkillExecutor;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.screens.BattleScreen;

import java.util.List;

public class BackstabSkillExecutor implements ISkillExecutor {
    @Override
    public void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData) {
        if (targets.isEmpty()) {
            Gdx.app.log("Backstab", "No target for Backstab.");
            return;
        }
        IBattleActor target = targets.get(0); // Backstab is single target

        int baseDamage = skillData.rollDamage();
        int finalDamage = baseDamage;
        boolean bonusApplied = false;

        if (caster.hasStatusEffect("INVISIBLE")) {
            finalDamage = baseDamage * 3;
            bonusApplied = true;
            Gdx.app.log("Backstab", caster.getName() + " is INVISIBLE. Damage tripled to: " + finalDamage);
        }

        if (!bonusApplied && battleScreen.isTargetAdjacentToAlly(caster, target)) { // Use BattleScreen's helper
            finalDamage = baseDamage * 3;
            bonusApplied = true;
            Gdx.app.log("Backstab", target.getName() + " is flanked. Damage tripled to: " + finalDamage);
        }

        if (!bonusApplied) {
            Gdx.app.log("Backstab", "Conditions not met, normal damage: " + finalDamage);
        }

        String logMessage = caster.getName() + " uses Backstab on " + target.getName() + ".";
        if (finalDamage > 0) logMessage += "\nDeals " + finalDamage + " damage!";
        else logMessage += "\nNo damage dealt.";
        battleScreen.getUiManager().updateBattleLog(logMessage);

        target.takeDamage(finalDamage);
    }

}
