package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Quiz;

public record QuizRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Skill is required")
        Quiz.Skill skill
) {}