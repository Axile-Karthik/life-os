package com.lifeos.progression.character.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.entity.CharacterStat;
import com.lifeos.progression.character.repository.CharacterRepository;
import com.lifeos.progression.common.exception.CharacterNotFoundException;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.service.TitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Production implementation of {@link CharacterService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CharacterServiceImpl implements CharacterService {

    private final CharacterRepository characterRepository;
    private final TitleService titleService;

    @Override
    public Character getCharacterById(Long id) {
        return characterRepository.findById(id)
                .orElseThrow(() -> new CharacterNotFoundException(id));
    }

    @Override
    public Character getCharacterByUserId(UUID userId) {
        return characterRepository.findByUserId(userId)
                .orElseThrow(() -> new CharacterNotFoundException(userId.toString()));
    }

    @Override
    @Transactional
    public Character getOrCreateDefaultCharacter(UUID userId) {
        return characterRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Character character = Character.builder()
                            .userId(userId)
                            .currentLevel(1)
                            .totalXp(0)
                            .stats(CharacterStat.builder()
                                    .knowledge(10)
                                    .discipline(10)
                                    .focus(10)
                                    .endurance(10)
                                    .strength(10)
                                    .creativity(10)
                                    .build())
                            .build();

                    // Evaluate starting title
                    Title defaultTitle = titleService.evaluateTitle(character);
                    if (defaultTitle != null) {
                        character.setTitle(defaultTitle);
                    }

                    return characterRepository.save(character);
                });
    }

    @Override
    @Transactional
    public Character updateStats(Long characterId, CharacterStat newStats) {
        Character character = getCharacterById(characterId);
        
        CharacterStat stats = character.getStats();
        if (stats == null) {
            stats = new CharacterStat();
        }
        
        stats.setKnowledge(newStats.getKnowledge());
        stats.setDiscipline(newStats.getDiscipline());
        stats.setFocus(newStats.getFocus());
        stats.setEndurance(newStats.getEndurance());
        stats.setStrength(newStats.getStrength());
        stats.setCreativity(newStats.getCreativity());
        character.setStats(stats);
        return characterRepository.save(character);
    }
}
