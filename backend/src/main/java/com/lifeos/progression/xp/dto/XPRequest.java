package com.lifeos.progression.xp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class XPRequest {

    @NotNull(message = "Character ID is required")
    private Long characterId;

    private Long skillId;

    @NotNull(message = "XP value is required")
    private Integer xp;

    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Source is required")
    private String source;
}
