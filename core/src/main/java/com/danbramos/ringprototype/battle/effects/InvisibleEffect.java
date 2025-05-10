package com.danbramos.ringprototype.battle.effects;

import com.danbramos.ringprototype.battle.StatusEffect;

public class InvisibleEffect extends StatusEffect {
    public InvisibleEffect(float chance, int duration) {
        super("INVISIBLE", chance, duration); // Value is not typically needed for invisibility itself
    }

    // Specific logic for how invisibility (e.g., targetability) is handled 
    // will be managed by systems that check for active effects of type "INVISIBLE" 
    // (e.g., in AI targeting, player input handling for attacks).
    // This class primarily serves to correctly type and store the effect's parameters.
} 