package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserAnswerResponse(
        Integer userAnswerId,
        Integer quizResultId,
        Integer questionId,
        String userAnswerText,
        Boolean isCorrect,
        LocalDateTime submittedAt
) {}