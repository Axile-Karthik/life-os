package com.lifeos.progression.title.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TitleResponse {
    private Long id;
    private String name;
    private String description;
    private String icon;
    private int requiredLevel;
}
