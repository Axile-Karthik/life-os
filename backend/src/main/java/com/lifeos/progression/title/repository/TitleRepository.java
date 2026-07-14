package com.lifeos.progression.title.repository;

import com.lifeos.progression.title.entity.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TitleRepository extends JpaRepository<Title, Long> {

    /**
     * Retrieves all titles ordered by required level descending.
     * Useful for evaluating the highest applicable title.
     *
     * @return sorted list of titles
     */
    List<Title> findAllByOrderByRequiredLevelDesc();

    /**
     * Finds a title by its name.
     *
     * @param name the title name
     * @return an optional containing the title if found
     */
    Optional<Title> findByName(String name);
}
