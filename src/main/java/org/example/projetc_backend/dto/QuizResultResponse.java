package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record QuizResultResponse(
        Integer resultId,
        Integer userId,
        Integer quizId,
        Integer score,
        LocalDateTime completedAt
) {}