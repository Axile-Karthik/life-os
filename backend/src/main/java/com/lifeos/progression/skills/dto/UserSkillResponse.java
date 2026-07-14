package com.lifeos.progression.skills.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSkillResponse {
    private Long id;
    private Long characterId;
    private Long skillId;
    private String skillName;
    private String skillIcon;
    private String skillDescription;
    private String categoryName;
    private int currentLevel;
    private int totalXp;
    private boolean unlocked;
    private Instant createdAt;
}
