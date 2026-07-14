package com.lifeos.progression.common.calculator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Default implementation of {@link LevelCalculator} using a polynomial leveling algorithm.
 * Formula: XP = baseXp * (level - 1)^exponent
 * Reverse: Level = 1 + (XP / baseXp)^(1 / exponent)
 */
@Component
public class DefaultLevelCalculator implements LevelCalculator {

    private final int baseXp;
    private final double exponent;

    public DefaultLevelCalculator(
            @Value("${app.progression.leveling.base-xp:100}") int baseXp,
            @Value("${app.progression.leveling.exponent:2.0}") double exponent) {
        this.baseXp = baseXp;
        this.exponent = exponent;
    }

    @Override
    public int calculateLevel(int totalXp) {
        if (totalXp < 0) {
            return 1;
        }
        double val = (double) totalXp / baseXp;
        // Level = floor(1 + val^(1 / exponent))
        return (int) Math.floor(1 + Math.pow(val, 1.0 / exponent));
    }

    @Override
    public int calculateXpForLevel(int level) {
        if (level <= 1) {
            return 0;
        }
        // XP = baseXp * (level - 1)^exponent
        return (int) Math.round(baseXp * Math.pow(level - 1, exponent));
    }
}
