package com.lifeos.progression.title.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.title.entity.Title;

import java.util.List;

/**
 * Service defining operations on Titles and automatic title evaluation.
 */
public interface TitleService {

    /**
     * Retrieves all available titles.
     *
     * @return list of titles
     */
    List<Title> getAllTitles();

    /**
     * Retrieves a title by its ID.
     *
     * @param id the title ID
     * @return the title
     * @throws com.lifeos.progression.common.exception.TitleNotFoundException if not found
     */
    Title getTitleById(Long id);

    /**
     * Evaluates and returns the highest level-eligible title for a character.
     *
     * @param character the character to evaluate
     * @return the eligible title, or null if no titles are defined
     */
    Title evaluateTitle(Character character);
}
