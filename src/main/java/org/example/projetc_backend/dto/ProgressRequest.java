package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgressRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Status is required")
        String status
) {}
