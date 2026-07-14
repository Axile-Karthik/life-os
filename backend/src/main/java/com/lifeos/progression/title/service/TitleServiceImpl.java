package com.lifeos.progression.title.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.common.exception.TitleNotFoundException;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.repository.TitleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Production implementation of {@link TitleService}.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TitleServiceImpl implements TitleService {

    private final TitleRepository titleRepository;

    @Override
    public List<Title> getAllTitles() {
        return titleRepository.findAll();
    }

    @Override
    public Title getTitleById(Long id) {
        return titleRepository.findById(id)
                .orElseThrow(() -> new TitleNotFoundException(id));
    }

    @Override
    public Title evaluateTitle(Character character) {
        // Find all titles in descending order of required levels
        List<Title> sortedTitles = titleRepository.findAllByOrderByRequiredLevelDesc();
        
        // Find the first title where character's current level >= title's required level
        for (Title title : sortedTitles) {
            if (character.getCurrentLevel() >= title.getRequiredLevel()) {
                return title;
            }
        }
        return null;
    }
}
