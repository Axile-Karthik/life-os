package com.lifeos.progression.skills.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.common.exception.SkillNotFoundException;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.SkillCategory;
import com.lifeos.progression.skills.entity.UserSkill;
import com.lifeos.progression.skills.repository.SkillCategoryRepository;
import com.lifeos.progression.skills.repository.SkillRepository;
import com.lifeos.progression.skills.repository.UserSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Production implementation of {@link SkillService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillCategoryRepository skillCategoryRepository;
    private final UserSkillRepository userSkillRepository;

    @Override
    public List<Skill> getAllSkills() {
        return skillRepository.findAll();
    }

    @Override
    public List<SkillCategory> getAllCategories() {
        return skillCategoryRepository.findAll();
    }

    @Override
    public List<UserSkill> getCharacterSkills(Long characterId) {
        return userSkillRepository.findByCharacterId(characterId);
    }

    @Override
    @Transactional
    public UserSkill getOrCreateUserSkill(Character character, Skill skill) {
        return userSkillRepository.findByCharacterIdAndSkillId(character.getId(), skill.getId())
                .orElseGet(() -> {
                    UserSkill newUserSkill = UserSkill.builder()
                            .character(character)
                            .skill(skill)
                            .currentLevel(1)
                            .totalXp(0)
                            .unlocked(true)
                            .build();
                    return userSkillRepository.save(newUserSkill);
                });
    }

    @Override
    public Skill getSkillById(Long id) {
        return skillRepository.findById(id)
                .orElseThrow(() -> new SkillNotFoundException(id));
    }
}
