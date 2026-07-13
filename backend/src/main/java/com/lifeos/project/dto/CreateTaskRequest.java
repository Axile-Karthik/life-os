package com.lifeos.project.dto;

import com.lifeos.project.enums.TaskPriority;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record CreateTaskRequest(
    @NotBlank(message = "Task title is required") String title,
    String description,
    TaskPriority priority,
    Instant dueDate
) {
    public CreateTaskRequest {
        if (priority == null) {
            priority = TaskPriority.MEDIUM;
        }
    }
}
