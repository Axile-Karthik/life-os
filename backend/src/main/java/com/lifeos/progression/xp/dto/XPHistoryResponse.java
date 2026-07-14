package com.lifeos.progression.xp.dto;

import lombok.*;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XPHistoryResponse {
    private Long id;
    private Long characterId;
    private Long skillId;
    private String skillName;
    private int xp;
    private String source;
    private String reason;
    private Instant createdAt;
}
