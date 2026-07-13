package com.lifeos.project.dto;

import com.lifeos.project.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public record UpdateTaskRequest(
    @NotBlank(message = "Task title is required") String title,
    String description,
    TaskPriority priority,
    Instant dueDate,
    UUID columnId
) {}
