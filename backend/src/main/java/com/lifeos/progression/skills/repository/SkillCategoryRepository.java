package com.lifeos.progression.skills.repository;

import com.lifeos.progression.skills.entity.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SkillCategoryRepository extends JpaRepository<SkillCategory, Long> {

    /**
     * Finds a category by its unique name.
     *
     * @param name the category name
     * @return an optional containing the category if found
     */
    Optional<SkillCategory> findByName(String name);
}
