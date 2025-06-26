package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserListeningAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Question ID is required")
        Integer questionId,
        @NotBlank(message = "Audio material URL is required")
        String audioMaterialUrl,
        @NotBlank(message = "User transcribed text is required")
        String userTranscribedText,
        @NotBlank(message = "Actual transcript text is required")
        String actualTranscriptText,
        @NotNull(message = "Accuracy score is required")
        @Min(0) Integer accuracyScore
) {}