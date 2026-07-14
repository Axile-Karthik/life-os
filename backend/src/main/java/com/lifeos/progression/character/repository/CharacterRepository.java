package com.lifeos.progression.character.repository;

import com.lifeos.progression.character.entity.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CharacterRepository extends JpaRepository<Character, Long> {

    /**
     * Finds a character by the associated User ID.
     *
     * @param userId the user UUID
     * @return an optional containing the character if found
     */
    Optional<Character> findByUserId(UUID userId);
}
