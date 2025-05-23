package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Level is required")
        String level
) {}
