package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserWritingAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer questionId,
        String originalPromptText,
        String userWrittenText,
        String grammarFeedback,
        String spellingFeedback,
        String cohesionFeedback,
        Integer overallScore,
        LocalDateTime attemptDate
) {}