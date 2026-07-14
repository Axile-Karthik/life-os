package com.lifeos.progression.character.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.entity.CharacterStat;

import java.util.UUID;

/**
 * Service defining operations for Character entities.
 */
public interface CharacterService {

    /**
     * Retrieves a Character by its database ID.
     *
     * @param id the character ID
     * @return the Character
     * @throws com.lifeos.progression.common.exception.CharacterNotFoundException if not found
     */
    Character getCharacterById(Long id);

    /**
     * Retrieves a Character by the User ID.
     *
     * @param userId the user UUID
     * @return the Character
     * @throws com.lifeos.progression.common.exception.CharacterNotFoundException if not found
     */
    Character getCharacterByUserId(UUID userId);

    /**
     * Retrieves or creates a default character for a User ID.
     * Guaranteed to return a Character.
     *
     * @param userId the user UUID
     * @return the Character
     */
    Character getOrCreateDefaultCharacter(UUID userId);

    /**
     * Updates the core RPG stats of a character.
     *
     * @param characterId the character ID
     * @param newStats the new character stats
     * @return the updated character
     */
    Character updateStats(Long characterId, CharacterStat newStats);
}
