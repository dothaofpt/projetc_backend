package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionResponse(
        Integer questionId,
        Integer quizId,
        String questionText,
        String type
) {}