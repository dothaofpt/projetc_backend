package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Title is required")
        String title
) {}
