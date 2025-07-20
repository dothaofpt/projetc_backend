package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserListeningAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Practice Activity ID is required")
        Integer practiceActivityId, // ID của PracticeActivity chứa audioUrl và transcriptText
        @NotBlank(message = "User transcribed text is required")
        String userTranscribedText
        // BỎ `accuracyScore` khỏi Request. Backend sẽ tự chấm.
) {}