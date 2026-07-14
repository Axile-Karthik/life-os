package com.lifeos.progression.xp.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.xp.entity.XPHistory;

import java.util.List;

/**
 * Service managing XP additions/removals and XP history tracking.
 * All XP changes must pass through this service.
 */
public interface XPService {

    /**
     * Awards (or deducts) XP for a character, optionally targeting a specific skill.
     * Logs the transaction in the history and triggers level recalculations.
     *
     * @param characterId the character ID
     * @param skillId the skill ID (optional, nullable)
     * @param xp the amount of XP to award (positive) or deduct (negative)
     * @param source the source of the XP award (e.g. MANUAL, QUEST, NOTION)
     * @param reason the description/reason for the award
     * @return the updated Character
     */
    Character awardXp(Long characterId, Long skillId, int xp, String source, String reason);

    /**
     * Retrieves the entire XP history logs for a character.
     *
     * @param characterId the character ID
     * @return list of XP transaction records
     */
    List<XPHistory> getXpHistory(Long characterId);
}
