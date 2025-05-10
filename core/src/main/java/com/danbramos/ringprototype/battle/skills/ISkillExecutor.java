package com.danbramos.ringprototype.battle.skills;

import com.danbramos.ringprototype.battle.BattleCharacter;
import com.danbramos.ringprototype.battle.IBattleActor;
import com.danbramos.ringprototype.screens.BattleScreen; // To access context if needed

import java.util.List;

public interface ISkillExecutor {
    /**
     * Executes the skill's logic.
     * @param caster The character using the skill.
     * @param targets A list of targets (can be one for single target, multiple for AoE).
     * @param battleScreen The context of the battle, for utility methods or accessing other actors.
     * @param skillData The data object for the skill being executed (contains range, damageFormula, etc.).
     */
    void execute(BattleCharacter caster, List<IBattleActor> targets, BattleScreen battleScreen, Skill skillData);

    /**
     * Optional: Method to determine valid targets for this skill.
     * This could help BattleInputHandler.
     * @param caster The character using the skill.
     * @param potentialTarget The potential target to validate.
     * @param battleScreen The battle context.
     * @param skillData The skill data.
     * @return true if the potentialTarget is valid for this skill.
     */
    // boolean isValidTarget(BattleCharacter caster, IBattleActor potentialTarget, BattleScreen battleScreen, Skill skillData);
}
