package com.danbramos.ringprototype.battle.skills;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.danbramos.ringprototype.battle.StatusEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class that loads and manages skill data from JSON
 */
public class SkillData {
    private static SkillData instance;
    private Map<String, Skill> skills;

    /**
     * Get the singleton instance
     * @return The SkillData instance
     */
    public static SkillData getInstance() {
        if (instance == null) {
            instance = new SkillData();
        }
        return instance;
    }

    /**
     * Private constructor to enforce singleton pattern
     */
    private SkillData() {
        skills = new HashMap<>();
        loadSkills();
    }

    /**
     * Load skills from JSON file
     */
    private void loadSkills() {
        try {
            JsonReader jsonReader = new JsonReader();
            JsonValue root = jsonReader.parse(Gdx.files.internal("data/skills.json"));

            JsonValue skillsArray = root.get("skills");
            int count = 0;

            for (JsonValue skillJson : skillsArray) {
                String id = skillJson.getString("id");
                String name = skillJson.getString("name");
                String description = skillJson.getString("description");

                // Get the skill type
                SkillType skillType = SkillType.valueOf(skillJson.getString("skillType"));

                // Get basic properties
                int range = skillJson.getInt("range");
                String damageFormula = skillJson.getString("damageFormula");
                int aoeRadius = skillJson.getInt("aoeRadius", 0);

                // Create the skill
                Skill skill = new Skill(id, name, description, skillType, range, damageFormula, aoeRadius);

                // Add optional properties
                if (skillJson.has("manaCost")) {
                    skill.setManaCost(skillJson.getInt("manaCost"));
                }

                if (skillJson.has("cooldown")) {
                    skill.setCooldown(skillJson.getInt("cooldown"));
                }

                if (skillJson.has("requiredClass")) {
                    skill.setRequiredClass(skillJson.getString("requiredClass"));
                }

                if (skillJson.has("requiredLevel")) {
                    skill.setRequiredLevel(skillJson.getInt("requiredLevel"));
                }

                if (skillJson.has("executorClassName")) {
                    String executorClassName = skillJson.getString("executorClassName");
                    try {
                        Class<?> executorClass = Class.forName(executorClassName);
                        ISkillExecutor executorInstance = (ISkillExecutor) executorClass.getDeclaredConstructor().newInstance();
                        skill.setExecutor(executorInstance);
                        Gdx.app.debug("SkillData", "Assigned executor " + executorClassName + " to skill " + id);
                    } catch (Exception e) {
                        Gdx.app.error("SkillData", "Failed to instantiate executor: " + executorClassName + " for skill " + id, e);
                        // Assign a default executor or leave it null to use Skill's default logic
                        // skill.setExecutor(new DefaultSkillExecutor());
                    }
                } else {
                    // Optionally assign a default executor for skills without a specific one
                    // skill.setExecutor(new DefaultSkillExecutor());
                    Gdx.app.debug("SkillData", "No specific executor for skill " + id + ". It will use default execution if available.");
                }

                // Load status effects if any
                if (skillJson.has("statusEffects")) {
                    JsonValue statusEffectsArray = skillJson.get("statusEffects");
                    List<StatusEffect> statusEffects = new ArrayList<>();

                    for (JsonValue effectJson : statusEffectsArray) {
                        String type = effectJson.getString("type");
                        float chance = effectJson.getFloat("chance");
                        int duration = effectJson.getInt("duration");
                        int value = effectJson.getInt("value", 0);

                        StatusEffect effect = new StatusEffect(type, chance, duration, value);
                        statusEffects.add(effect);
                    }

                    if (!statusEffects.isEmpty()) {
                        skill.setStatusEffects(statusEffects);
                    }
                }

                // Add the skill to the map
                skills.put(id, skill);
                count++;
            }

            Gdx.app.log("SkillData", "Loaded " + count + " skills");
        } catch (Exception e) {
            Gdx.app.error("SkillData", "Error loading skills: " + e.getMessage(), e);
        }
    }

    /**
     * Get a skill by its ID
     * @param id The skill ID
     * @return The skill, or null if not found
     */
    public Skill getSkill(String id) {
        return skills.get(id);
    }

    /**
     * Get all skills
     * @return A map of skill IDs to skills
     */
    public Map<String, Skill> getAllSkills() {
        return skills;
    }

    /**
     * Get skills for a specific class
     * @param className The class name
     * @return A list of skills for the class
     */
    public List<Skill> getSkillsForClass(String className) {
        List<Skill> classSkills = new ArrayList<>();

        for (Skill skill : skills.values()) {
            if (skill.getRequiredClass() == null
                || skill.getRequiredClass().equals("ANY")
                || skill.getRequiredClass().equals(className)) {
                classSkills.add(skill);
            }
        }

        return classSkills;
    }
}
