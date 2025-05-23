package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerResponse(
        Integer answerId,
        Integer questionId,
        String answerText,
        Boolean isCorrect
) {}