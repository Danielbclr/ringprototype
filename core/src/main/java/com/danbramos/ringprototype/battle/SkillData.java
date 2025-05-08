package com.danbramos.ringprototype.battle;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;

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
                Skill skill = new Skill(name, description, skillType, range, damageFormula, aoeRadius);
                
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
                
                // Load status effects if any
                if (skillJson.has("statusEffects")) {
                    JsonValue statusEffectsArray = skillJson.get("statusEffects");
                    List<StatusEffect> statusEffects = new ArrayList<>();
                    
                    for (JsonValue effectJson : statusEffectsArray) {
                        String type = effectJson.getString("type");
                        float chance = effectJson.getFloat("chance");
                        int duration = effectJson.getInt("duration");
                        
                        StatusEffect effect = new StatusEffect(type, chance, duration);
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