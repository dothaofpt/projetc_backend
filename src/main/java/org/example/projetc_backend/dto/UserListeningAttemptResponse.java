package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserListeningAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer questionId,
        String audioMaterialUrl,
        String userTranscribedText,
        String actualTranscriptText,
        Integer accuracyScore,
        LocalDateTime attemptDate
) {}