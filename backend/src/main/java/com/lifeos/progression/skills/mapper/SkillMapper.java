package com.lifeos.progression.skills.mapper;

import com.lifeos.progression.skills.dto.SkillCategoryResponse;
import com.lifeos.progression.skills.dto.SkillResponse;
import com.lifeos.progression.skills.dto.UserSkillResponse;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.SkillCategory;
import com.lifeos.progression.skills.entity.UserSkill;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting skill-related entities to DTOs.
 */
@Component
public class SkillMapper {

    public SkillCategoryResponse toCategoryResponse(SkillCategory category) {
        if (category == null) {
            return null;
        }
        return SkillCategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .build();
    }

    public SkillResponse toSkillResponse(Skill skill) {
        if (skill == null) {
            return null;
        }
        return SkillResponse.builder()
                .id(skill.getId())
                .categoryId(skill.getCategory() != null ? skill.getCategory().getId() : null)
                .categoryName(skill.getCategory() != null ? skill.getCategory().getName() : null)
                .name(skill.getName())
                .description(skill.getDescription())
                .icon(skill.getIcon())
                .displayOrder(skill.getDisplayOrder())
                .build();
    }

    public UserSkillResponse toUserSkillResponse(UserSkill userSkill) {
        if (userSkill == null) {
            return null;
        }
        Skill skill = userSkill.getSkill();
        return UserSkillResponse.builder()
                .id(userSkill.getId())
                .characterId(userSkill.getCharacter() != null ? userSkill.getCharacter().getId() : null)
                .skillId(skill != null ? skill.getId() : null)
                .skillName(skill != null ? skill.getName() : null)
                .skillIcon(skill != null ? skill.getIcon() : null)
                .skillDescription(skill != null ? skill.getDescription() : null)
                .categoryName(skill != null && skill.getCategory() != null ? skill.getCategory().getName() : null)
                .currentLevel(userSkill.getCurrentLevel())
                .totalXp(userSkill.getTotalXp())
                .unlocked(userSkill.isUnlocked())
                .createdAt(userSkill.getCreatedAt())
                .build();
    }
}
