package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Quiz; // Import the Quiz entity

public record QuizRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Title is required")
        String title,
        // Removed Quiz.Skill skill
        @NotNull(message = "Quiz type is required")
        Quiz.QuizType quizType // Added the new QuizType enum
) {}