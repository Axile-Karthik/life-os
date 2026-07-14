package com.lifeos.progression.skills.controller;

import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.service.CharacterService;
import com.lifeos.progression.skills.dto.SkillCategoryResponse;
import com.lifeos.progression.skills.dto.SkillResponse;
import com.lifeos.progression.skills.dto.UserSkillResponse;
import com.lifeos.progression.skills.entity.Skill;
import com.lifeos.progression.skills.entity.SkillCategory;
import com.lifeos.progression.skills.entity.UserSkill;
import com.lifeos.progression.skills.mapper.SkillMapper;
import com.lifeos.progression.skills.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SkillController {

    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final SkillService skillService;
    private final CharacterService characterService;
    private final SkillMapper skillMapper;

    /**
     * GET /api/skills
     * Retrieves all available skills.
     */
    @GetMapping("/api/skills")
    public ResponseEntity<List<SkillResponse>> getAllSkills() {
        List<Skill> skills = skillService.getAllSkills();
        List<SkillResponse> responses = skills.stream()
                .map(skillMapper::toSkillResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/skills/categories
     * Retrieves all skill categories.
     */
    @GetMapping("/api/skills/categories")
    public ResponseEntity<List<SkillCategoryResponse>> getAllCategories() {
        List<SkillCategory> categories = skillService.getAllCategories();
        List<SkillCategoryResponse> responses = categories.stream()
                .map(skillMapper::toCategoryResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * GET /api/character/skills
     * Retrieves the skill progressions for the logged-in user's character.
     * Looks up user by 'X-User-Id' header, falling back to system default.
     */
    @GetMapping("/api/character/skills")
    public ResponseEntity<List<UserSkillResponse>> getCharacterSkills(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader,
            @RequestParam(value = "characterId", required = false) Long characterId) {
        
        Long resolvedCharacterId;
        if (characterId != null) {
            resolvedCharacterId = characterId;
        } else {
            UUID userId = DEFAULT_USER_ID;
            if (userIdHeader != null && !userIdHeader.isBlank()) {
                try {
                    userId = UUID.fromString(userIdHeader);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().build();
                }
            }
            Character character = characterService.getOrCreateDefaultCharacter(userId);
            resolvedCharacterId = character.getId();
        }

        List<UserSkill> userSkills = skillService.getCharacterSkills(resolvedCharacterId);
        List<UserSkillResponse> responses = userSkills.stream()
                .map(skillMapper::toUserSkillResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
