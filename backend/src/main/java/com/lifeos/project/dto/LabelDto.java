package com.lifeos.project.dto;

import lombok.Builder;
import java.util.UUID;

@Builder
public record LabelDto(
    UUID id,
    String name,
    String color
) {}
