package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuizResponse(
        Integer quizId,
        Integer lessonId,
        String title,
        String createdAt
) {}