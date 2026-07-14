package com.lifeos.progression.skills.repository;

import com.lifeos.progression.skills.entity.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Long> {

    /**
     * Finds skills by their category ID.
     *
     * @param categoryId the category ID
     * @return list of skills
     */
    List<Skill> findByCategoryId(Long categoryId);

    /**
     * Finds a skill by its unique name.
     *
     * @param name the skill name
     * @return an optional containing the skill if found
     */
    Optional<Skill> findByName(String name);
}
