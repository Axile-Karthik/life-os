package com.lifeos.progression.common.calculator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LevelCalculatorTest {

    private LevelCalculator levelCalculator;

    @BeforeEach
    void setUp() {
        // base-xp = 100, exponent = 2.0
        levelCalculator = new DefaultLevelCalculator(100, 2.0);
    }

    @Test
    void testCalculateLevel_NegativeXpReturnsOne() {
        assertEquals(1, levelCalculator.calculateLevel(-100));
        assertEquals(1, levelCalculator.calculateLevel(-1));
    }

    @Test
    void testCalculateLevel_ExactBoundaries() {
        // Level 1: 0 to 99 XP
        assertEquals(1, levelCalculator.calculateLevel(0));
        assertEquals(1, levelCalculator.calculateLevel(50));
        assertEquals(1, levelCalculator.calculateLevel(99));

        // Level 2: 100 to 399 XP
        assertEquals(2, levelCalculator.calculateLevel(100));
        assertEquals(2, levelCalculator.calculateLevel(250));
        assertEquals(2, levelCalculator.calculateLevel(399));

        // Level 3: 400 XP to 899 XP
        assertEquals(3, levelCalculator.calculateLevel(400));
        assertEquals(3, levelCalculator.calculateLevel(899));

        // Level 4: 900+ XP
        assertEquals(4, levelCalculator.calculateLevel(900));
        assertEquals(4, levelCalculator.calculateLevel(1500));
    }

    @Test
    void testCalculateXpForLevel() {
        assertEquals(0, levelCalculator.calculateXpForLevel(0));
        assertEquals(0, levelCalculator.calculateXpForLevel(1));
        assertEquals(100, levelCalculator.calculateXpForLevel(2));
        assertEquals(400, levelCalculator.calculateXpForLevel(3));
        assertEquals(900, levelCalculator.calculateXpForLevel(4));
        assertEquals(8100, levelCalculator.calculateXpForLevel(10));
    }
}
