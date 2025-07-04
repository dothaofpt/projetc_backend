package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserListeningAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer practiceActivityId, // Changed from questionId
        // Removed String audioMaterialUrl,
        String userTranscribedText,
        // Removed String actualTranscriptText,
        Integer accuracyScore,
        LocalDateTime attemptDate
) {}