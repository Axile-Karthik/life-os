package com.lifeos.progression.skills.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SkillCategoryResponse {
    private Long id;
    private String name;
    private String description;
}
