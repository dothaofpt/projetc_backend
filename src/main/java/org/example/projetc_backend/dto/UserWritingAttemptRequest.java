package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserWritingAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Practice Activity ID is required") // Changed from questionId
        Integer practiceActivityId,
        // Removed @NotBlank(message = "Original prompt text is required") String originalPromptText,
        @NotBlank(message = "User written text is required")
        String userWrittenText,
        String grammarFeedback,
        String spellingFeedback,
        String cohesionFeedback,
        @Min(0) Integer overallScore
) {}