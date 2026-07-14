package com.lifeos.progression.xp.service;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.repository.CharacterRepository;
import com.lifeos.progression.character.service.CharacterService;
import com.lifeos.progression.common.calculator.LevelCalculator;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.UserSkill;
import com.lifeos.progression.skills.repository.UserSkillRepository;
import com.lifeos.progression.skills.service.SkillService;
import com.lifeos.progression.title.entity.Title;
import com.lifeos.progression.title.service.TitleService;
import com.lifeos.progression.xp.entity.XPHistory;
import com.lifeos.progression.xp.repository.XPHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class XPServiceTest {

    @Mock
    private CharacterService characterService;
    @Mock
    private CharacterRepository characterRepository;
    @Mock
    private SkillService skillService;
    @Mock
    private UserSkillRepository userSkillRepository;
    @Mock
    private XPHistoryRepository xpHistoryRepository;
    @Mock
    private LevelCalculator levelCalculator;
    @Mock
    private TitleService titleService;

    @InjectMocks
    private XPServiceImpl xpService;

    private Character character;
    private Skill skill;
    private UserSkill userSkill;
    private Title title;

    @BeforeEach
    void setUp() {
        title = Title.builder().id(10L).name("The Beginner").requiredLevel(1).build();
        character = Character.builder()
                .id(1L)
                .totalXp(150)
                .currentLevel(2)
                .title(title)
                .build();
        
        skill = Skill.builder().id(20L).name("Java Programming").build();
        
        userSkill = UserSkill.builder()
                .id(5L)
                .character(character)
                .skill(skill)
                .totalXp(50)
                .currentLevel(1)
                .unlocked(true)
                .build();
    }

    @Test
    void testAwardXp_GeneralCharacterOnly() {
        when(characterService.getCharacterById(1L)).thenReturn(character);
        when(levelCalculator.calculateLevel(250)).thenReturn(2); // level didn't change
        when(characterRepository.save(any(Character.class))).thenAnswer(i -> i.getArguments()[0]);

        Character updated = xpService.awardXp(1L, null, 100, "MANUAL", "Completed task");

        assertNotNull(updated);
        assertEquals(250, updated.getTotalXp());
        assertEquals(2, updated.getCurrentLevel());

        // Verify history captured
        ArgumentCaptor<XPHistory> historyCaptor = ArgumentCaptor.forClass(XPHistory.class);
        verify(xpHistoryRepository, times(1)).save(historyCaptor.capture());
        
        XPHistory savedHistory = historyCaptor.getValue();
        assertEquals(character.getId(), savedHistory.getCharacter().getId());
        assertNull(savedHistory.getSkill());
        assertEquals(100, savedHistory.getXp());
        assertEquals("MANUAL", savedHistory.getSource());
        assertEquals("Completed task", savedHistory.getReason());
    }

    @Test
    void testAwardXp_WithSkillProgressionAndLevelUp() {
        when(characterService.getCharacterById(1L)).thenReturn(character);
        when(skillService.getSkillById(20L)).thenReturn(skill);
        when(skillService.getOrCreateUserSkill(character, skill)).thenReturn(userSkill);
        
        // Mock Level Up checks
        when(levelCalculator.calculateLevel(150)).thenReturn(2); // for userSkill calculation (new skill XP = 50 + 100 = 150 -> Level 2)
        when(levelCalculator.calculateLevel(250)).thenReturn(3); // for Character calculation (new character XP = 150 + 100 = 250 -> Level 3)
        
        Title newTitle = Title.builder().id(11L).name("Backend Apprentice").requiredLevel(3).build();
        when(titleService.evaluateTitle(character)).thenReturn(newTitle);
        when(characterRepository.save(any(Character.class))).thenAnswer(i -> i.getArguments()[0]);

        Character updated = xpService.awardXp(1L, 20L, 100, "QUEST", "Completed Java project");

        assertNotNull(updated);
        assertEquals(250, updated.getTotalXp());
        assertEquals(3, updated.getCurrentLevel());
        assertEquals("Backend Apprentice", updated.getTitle().getName()); // title updated

        // Verify skill updated
        verify(userSkillRepository, times(1)).save(userSkill);
        assertEquals(150, userSkill.getTotalXp());
        assertEquals(2, userSkill.getCurrentLevel());

        // Verify history captured
        ArgumentCaptor<XPHistory> historyCaptor = ArgumentCaptor.forClass(XPHistory.class);
        verify(xpHistoryRepository, times(1)).save(historyCaptor.capture());
        
        XPHistory savedHistory = historyCaptor.getValue();
        assertEquals(character.getId(), savedHistory.getCharacter().getId());
        assertEquals(skill.getId(), savedHistory.getSkill().getId());
        assertEquals(100, savedHistory.getXp());
        assertEquals("QUEST", savedHistory.getSource());
    }

    @Test
    void testAwardXp_NegativeValuePreventsSubZeroXp() {
        when(characterService.getCharacterById(1L)).thenReturn(character);
        when(levelCalculator.calculateLevel(0)).thenReturn(1);
        when(titleService.evaluateTitle(character)).thenReturn(title);
        when(characterRepository.save(any(Character.class))).thenAnswer(i -> i.getArguments()[0]);

        // Character has 150 XP. Award -200 XP. Should drop to 0 XP.
        Character updated = xpService.awardXp(1L, null, -200, "MANUAL", "Penalty");

        assertEquals(0, updated.getTotalXp());
        assertEquals(1, updated.getCurrentLevel());
    }
}
