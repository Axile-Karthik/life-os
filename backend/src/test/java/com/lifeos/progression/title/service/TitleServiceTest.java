package com.lifeos.progression.title.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.common.exception.TitleNotFoundException;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.repository.TitleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TitleServiceTest {

    @Mock
    private TitleRepository titleRepository;

    @InjectMocks
    private TitleServiceImpl titleService;

    private List<Title> mockTitles;

    @BeforeEach
    void setUp() {
        mockTitles = Arrays.asList(
                Title.builder().id(3L).name("The Architect").requiredLevel(40).build(),
                Title.builder().id(2L).name("Backend Apprentice").requiredLevel(10).build(),
                Title.builder().id(1L).name("The Beginner").requiredLevel(1).build()
        );
    }

    @Test
    void testGetAllTitles() {
        when(titleRepository.findAll()).thenReturn(mockTitles);
        List<Title> result = titleService.getAllTitles();
        assertEquals(3, result.size());
        verify(titleRepository, times(1)).findAll();
    }

    @Test
    void testGetTitleById_Success() {
        Title title = mockTitles.get(2); // The Beginner
        when(titleRepository.findById(1L)).thenReturn(Optional.of(title));

        Title result = titleService.getTitleById(1L);
        assertNotNull(result);
        assertEquals("The Beginner", result.getName());
    }

    @Test
    void testGetTitleById_NotFound() {
        when(titleRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(TitleNotFoundException.class, () -> titleService.getTitleById(99L));
    }

    @Test
    void testEvaluateTitle_CorrectThresholds() {
        // Return sorted titles
        when(titleRepository.findAllByOrderByRequiredLevelDesc()).thenReturn(mockTitles);

        // Level 1 Character
        Character charLvl1 = Character.builder().currentLevel(1).build();
        Title title1 = titleService.evaluateTitle(charLvl1);
        assertEquals("The Beginner", title1.getName());

        // Level 9 Character -> Beginner
        Character charLvl9 = Character.builder().currentLevel(9).build();
        Title title9 = titleService.evaluateTitle(charLvl9);
        assertEquals("The Beginner", title9.getName());

        // Level 10 Character -> Backend Apprentice
        Character charLvl10 = Character.builder().currentLevel(10).build();
        Title title10 = titleService.evaluateTitle(charLvl10);
        assertEquals("Backend Apprentice", title10.getName());

        // Level 39 Character -> Backend Apprentice
        Character charLvl39 = Character.builder().currentLevel(39).build();
        Title title39 = titleService.evaluateTitle(charLvl39);
        assertEquals("Backend Apprentice", title39.getName());

        // Level 40 Character -> The Architect
        Character charLvl40 = Character.builder().currentLevel(40).build();
        Title title40 = titleService.evaluateTitle(charLvl40);
        assertEquals("The Architect", title40.getName());
    }

    @Test
    void testEvaluateTitle_EmptyDatabase() {
        when(titleRepository.findAllByOrderByRequiredLevelDesc()).thenReturn(Collections.emptyList());
        Character character = Character.builder().currentLevel(10).build();
        Title title = titleService.evaluateTitle(character);
        assertNull(title);
    }
}
