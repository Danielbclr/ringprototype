package com.danbramos.ringprototype;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.danbramos.ringprototype.battle.skills.Skill;
import com.danbramos.ringprototype.battle.skills.SkillData;
import com.danbramos.ringprototype.battle.skills.SkillType;
import com.danbramos.ringprototype.battle.StatusEffect;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * Example test showing how to use the SkillData system
 */
public class SkillDataTest {

    @Mock
    private Application application;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        Gdx.app = application;
        when(Gdx.app.getLogLevel()).thenReturn(Application.LOG_DEBUG);
    }

    @Test
    public void testLoadSkills() {
        // Get the singleton instance
        SkillData skillData = SkillData.getInstance();

        // Get a specific skill by ID
        Skill slash = skillData.getSkill("skill_slash");
        assertNotNull("Slash skill should be loaded", slash);
        assertEquals("Slash", slash.getName());
        assertEquals(SkillType.MELEE_ATTACK, slash.getType());
        assertEquals(1, slash.getRange());
        assertEquals("1d8", slash.getDamageRoll());
        assertEquals(0, slash.getAoeRadius());
        assertEquals("WARRIOR", slash.getRequiredClass());

        // Get a skill with status effects
        Skill fireball = skillData.getSkill("skill_fireball");
        assertNotNull("Fireball skill should be loaded", fireball);
        List<StatusEffect> effects = fireball.getStatusEffects();
        assertFalse("Fireball should have status effects", effects.isEmpty());
        StatusEffect burnEffect = effects.get(0);
        assertEquals("BURN", burnEffect.getType());
        assertEquals(0.5f, burnEffect.getChance(), 0.01f);
        assertEquals(3, burnEffect.getDuration());

        // Get all skills for a class
        List<Skill> warriorSkills = skillData.getSkillsForClass("WARRIOR");
        assertFalse("Warrior should have skills", warriorSkills.isEmpty());
        assertTrue("Warrior should have slash skill",
                warriorSkills.stream().anyMatch(s -> s.getName().equals("Slash")));
    }
}
