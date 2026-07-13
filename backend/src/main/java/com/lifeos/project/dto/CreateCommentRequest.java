package com.lifeos.project.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateCommentRequest(
    @NotBlank(message = "Comment content is required") String content
) {}
