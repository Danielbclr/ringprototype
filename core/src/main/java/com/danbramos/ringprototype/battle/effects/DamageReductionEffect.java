package com.danbramos.ringprototype.battle.effects;

import com.danbramos.ringprototype.battle.StatusEffect;

public class DamageReductionEffect extends StatusEffect {
    public DamageReductionEffect(float chance, int duration, int reductionAmount) {
        super("DAMAGE_REDUCTION", chance, duration, reductionAmount);
    }

    // Specific logic for how damage reduction is applied will be handled 
    // by the systems that process effects (e.g., in BattleCharacter or a combat resolver)
    // when checking for active effects of type "DAMAGE_REDUCTION".
    // This class primarily serves to correctly type and store the effect's parameters.
} 