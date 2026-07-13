package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateColumnRequest(
    @NotBlank(message = "Column name is required") String name,
    Integer position
) {}
