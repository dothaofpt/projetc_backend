package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull(message = "Question ID is required")
        Integer questionId,
        @NotBlank(message = "Answer text is required")
        String answerText,
        @NotNull(message = "Correctness is required")
        Boolean isCorrect
) {}
