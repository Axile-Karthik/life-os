package com.lifeos.progression.common.calculator;

/**
 * Interface defining the calculation rules for converting XP to levels
 * and levels to the minimum required XP.
 */
public interface LevelCalculator {

    /**
     * Calculates the level for a given amount of cumulative XP.
     *
     * @param totalXp cumulative XP
     * @return the calculated level
     */
    int calculateLevel(int totalXp);

    /**
     * Calculates the minimum cumulative XP required to reach a specific level.
     *
     * @param level the level target
     * @return the required cumulative XP
     */
    int calculateXpForLevel(int level);
}
