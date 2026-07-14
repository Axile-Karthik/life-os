package com.lifeos.progression.skills.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.SkillCategory;
import com.lifeos.progression.skills.entity.UserSkill;

import java.util.List;

/**
 * Service defining skill-related retrieval and progression mapping.
 */
public interface SkillService {

    /**
     * Retrieves all configured skills in the system.
     *
     * @return list of skills
     */
    List<Skill> getAllSkills();

    /**
     * Retrieves all skill categories in the system.
     *
     * @return list of skill categories
     */
    List<SkillCategory> getAllCategories();

    /**
     * Retrieves the skill progression for a specific character.
     *
     * @param characterId the character ID
     * @return list of UserSkill progress tracking details
     */
    List<UserSkill> getCharacterSkills(Long characterId);

    /**
     * Retrieves or creates progress tracking mapping for a specific skill and character.
     *
     * @param character the character
     * @param skill the skill
     * @return the associated UserSkill
     */
    UserSkill getOrCreateUserSkill(Character character, Skill skill);

    /**
     * Retrieves a skill by ID.
     *
     * @param id the skill ID
     * @return the skill
     * @throws com.lifeos.progression.common.exception.SkillNotFoundException if not found
     */
    Skill getSkillById(Long id);
}
