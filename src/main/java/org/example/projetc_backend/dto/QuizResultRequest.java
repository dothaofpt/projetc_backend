package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record QuizResultRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Quiz ID is required")
        Integer quizId,
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score cannot be negative")
        Integer score,
        @Min(value = 0, message = "Duration cannot be negative")
        Integer durationSeconds // Bổ sung
) {}