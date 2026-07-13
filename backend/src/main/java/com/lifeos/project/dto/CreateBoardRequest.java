package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateBoardRequest(
    @NotBlank(message = "Board name is required") String name,
    String description
) {}
