package com.lifeos.progression.character.controller;

import com.lifeos.progression.character.dto.CharacterResponse;
import com.lifeos.progression.character.dto.StatsUpdateRequest;
import com.lifeos.progression.character.entity.Character;
import com.lifeos.progression.character.entity.CharacterStat;
import com.lifeos.progression.character.mapper.CharacterMapper;
import com.lifeos.progression.character.service.CharacterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/character")
@RequiredArgsConstructor
public class CharacterController {

    private static final UUID DEFAULT_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final CharacterService characterService;
    private final CharacterMapper characterMapper;

    /**
     * GET /api/character
     * Retrieves the character profile for the currently logged-in user.
     * Looks up user by 'X-User-Id' header, falling back to a default system UUID.
     */
    @GetMapping
    public ResponseEntity<CharacterResponse> getCurrentCharacter(
            @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        
        UUID userId = DEFAULT_USER_ID;
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                userId = UUID.fromString(userIdHeader);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }
        
        Character character = characterService.getOrCreateDefaultCharacter(userId);
        return ResponseEntity.ok(characterMapper.toResponse(character));
    }

    /**
     * GET /api/character/{id}
     * Retrieves a specific character by their database ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CharacterResponse> getCharacterById(@PathVariable Long id) {
        Character character = characterService.getCharacterById(id);
        return ResponseEntity.ok(characterMapper.toResponse(character));
    }

    /**
     * PUT /api/character/stats
     * Updates character core attributes (discipline, knowledge, focus, etc.).
     */
    @PutMapping("/stats")
    public ResponseEntity<CharacterResponse> updateStats(@Valid @RequestBody StatsUpdateRequest request) {
        CharacterStat entityStats = characterMapper.toStatEntity(request.getStats());
        Character character = characterService.updateStats(request.getCharacterId(), entityStats);
        return ResponseEntity.ok(characterMapper.toResponse(character));
    }
}
