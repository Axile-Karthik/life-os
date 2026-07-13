package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateProjectRequest(
    @NotBlank(message = "Project name is required") String name,
    String description
) {}
