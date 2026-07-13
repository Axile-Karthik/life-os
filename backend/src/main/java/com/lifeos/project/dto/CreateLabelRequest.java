package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLabelRequest(
    @NotBlank(message = "Label name is required") String name,
    @NotBlank(message = "Label color is required") String color
) {}
