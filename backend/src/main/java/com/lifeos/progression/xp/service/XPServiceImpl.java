package com.lifeos.progression.xp.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.repository.CharacterRepository;
import com.lifeos.progression.character.service.CharacterService;
import com.lifeos.progression.common.calculator.LevelCalculator;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.UserSkill;
import com.lifeos.progression.skills.repository.UserSkillRepository;
import com.lifeos.progression.skills.service.SkillService;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.service.TitleService;
import com.lifeos.progression.xp.entity.XPHistory;
import com.lifeos.progression.xp.repository.XPHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Production implementation of {@link XPService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class XPServiceImpl implements XPService {

    private final CharacterService characterService;
    private final CharacterRepository characterRepository;
    private final SkillService skillService;
    private final UserSkillRepository userSkillRepository;
    private final XPHistoryRepository xpHistoryRepository;
    private final LevelCalculator levelCalculator;
    private final TitleService titleService;

    @Override
    @Transactional
    public Character awardXp(Long characterId, Long skillId, int xp, String source, String reason) {
        // 1. Fetch character
        Character character = characterService.getCharacterById(characterId);
        Skill skill = null;

        // 2. Process skill XP if skillId is provided
        if (skillId != null) {
            skill = skillService.getSkillById(skillId);
            UserSkill userSkill = skillService.getOrCreateUserSkill(character, skill);
            
            int newSkillXp = Math.max(0, userSkill.getTotalXp() + xp);
            userSkill.setTotalXp(newSkillXp);
            
            int newSkillLevel = levelCalculator.calculateLevel(newSkillXp);
            userSkill.setCurrentLevel(newSkillLevel);
            userSkill.setUnlocked(true);
            userSkillRepository.save(userSkill);
        }

        // 3. Update character's total XP and recalculate level
        int newCharacterXp = Math.max(0, character.getTotalXp() + xp);
        character.setTotalXp(newCharacterXp);
        
        int oldLevel = character.getCurrentLevel();
        int newLevel = levelCalculator.calculateLevel(newCharacterXp);
        character.setCurrentLevel(newLevel);

        // 4. Trigger Title Engine check if level has changed
        if (newLevel != oldLevel || character.getTitle() == null) {
            Title eligibleTitle = titleService.evaluateTitle(character);
            if (eligibleTitle != null) {
                character.setTitle(eligibleTitle);
            }
        }

        // 5. Save character
        Character updatedCharacter = characterRepository.save(character);

        // 6. Record history ledger
        XPHistory history = XPHistory.builder()
                .character(updatedCharacter)
                .skill(skill)
                .xp(xp)
                .source(source)
                .reason(reason)
                .build();
        xpHistoryRepository.save(history);

        return updatedCharacter;
    }

    @Override
    public List<XPHistory> getXpHistory(Long characterId) {
        // Verify character exists first
        characterService.getCharacterById(characterId);
        return xpHistoryRepository.findByCharacterIdOrderByCreatedAtDesc(characterId);
    }
}
