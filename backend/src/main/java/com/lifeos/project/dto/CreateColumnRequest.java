package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateColumnRequest(
    @NotBlank(message = "Column name is required") String name
) {}
