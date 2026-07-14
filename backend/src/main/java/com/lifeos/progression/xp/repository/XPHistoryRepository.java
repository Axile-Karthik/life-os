package com.lifeos.progression.xp.repository;

import com.lifeos.progression.xp.entity.XPHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface XPHistoryRepository extends JpaRepository<XPHistory, Long> {

    /**
     * Finds all XP transaction records for a character, ordered by date descending.
     *
     * @param characterId the character ID
     * @return list of XP transaction history
     */
    List<XPHistory> findByCharacterIdOrderByCreatedAtDesc(Long characterId);
}
