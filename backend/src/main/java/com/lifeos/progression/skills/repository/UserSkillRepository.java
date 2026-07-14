package com.lifeos.progression.skills.repository;

import com.lifeos.progression.skills.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Long> {

    /**
     * Finds a character's skill progress by character ID and skill ID.
     *
     * @param characterId the character ID
     * @param skillId the skill ID
     * @return an optional containing the UserSkill progress if found
     */
    Optional<UserSkill> findByCharacterIdAndSkillId(Long characterId, Long skillId);

    /**
     * Finds all skill progressions for a character.
     *
     * @param characterId the character ID
     * @return list of UserSkill progress records
     */
    List<UserSkill> findByCharacterId(Long characterId);
}
