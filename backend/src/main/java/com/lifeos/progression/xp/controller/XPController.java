package com.lifeos.progression.xp.controller;

import com.lifeos.progression.character.dto.CharacterResponse;
import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.mapper.CharacterMapper;
import com.lifeos.progression.character.service.CharacterService;
import com.lifeos.progression.xp.dto.XPHistoryResponse;
import com.lifeos.progression.xp.dto.XPRequest;
import com.lifeos.progression.xp.entity.XPHistory;
import com.lifeos.progression.xp.mapper.XPMapper;
import com.lifeos.progression.xp.service.XPService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/xp")
@RequiredArgsConstructor
public class XPController {

    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final XPService xpService;
    private final CharacterService characterService;
    private final CharacterMapper characterMapper;
    private final XPMapper xpMapper;

    /**
     * POST /api/xp
     * Awards (or removes) XP for a character, optionally targeting a specific skill.
     * Returns the updated character.
     */
    @PostMapping
    public ResponseEntity<CharacterResponse> awardXp(@Valid @RequestBody XPRequest request) {
        Character character = xpService.awardXp(
                request.getCharacterId(),
                request.getSkillId(),
                request.getXp(),
                request.getSource(),
                request.getReason()
        );
        return ResponseEntity.ok(characterMapper.toResponse(character));
    }

    /**
     * GET /api/xp/history
     * Retrieves the history of XP transactions for a character.
     * Looks up user by 'X-User-Id' header if characterId is not explicitly provided.
     */
    @GetMapping("/history")
    public ResponseEntity<List<XPHistoryResponse>> getXpHistory(
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

        List<XPHistory> history = xpService.getXpHistory(resolvedCharacterId);
        List<XPHistoryResponse> responses = history.stream()
                .map(xpMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}
