package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionRequest(
        @NotNull(message = "Quiz ID is required")
        Integer quizId,
        @NotBlank(message = "Question text is required")
        String questionText,
        @NotBlank(message = "Question type is required")
        String type
) {}
