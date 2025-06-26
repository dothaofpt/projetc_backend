package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserSpeakingAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Question ID is required")
        Integer questionId,
        @NotBlank(message = "Original prompt text is required")
        String originalPromptText,
        @NotBlank(message = "User audio URL is required")
        String userAudioUrl,
        @NotBlank(message = "User transcribed by STT is required")
        String userTranscribedBySTT,
        @NotNull(message = "Pronunciation score is required")
        @Min(0) Integer pronunciationScore,
        @Min(0) Integer fluencyScore,
        @NotNull(message = "Overall score is required")
        @Min(0) Integer overallScore
) {}