package com.lifeos.project.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record MoveTaskRequest(
    @NotNull(message = "Target column ID is required") UUID targetColumnId,
    @NotNull(message = "New index position is required") Integer newIndex
) {}
