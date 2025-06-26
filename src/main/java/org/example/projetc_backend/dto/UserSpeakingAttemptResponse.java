package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserSpeakingAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer questionId,
        String originalPromptText,
        String userAudioUrl,
        String userTranscribedBySTT,
        Integer pronunciationScore,
        Integer fluencyScore,
        Integer overallScore,
        LocalDateTime attemptDate
) {}