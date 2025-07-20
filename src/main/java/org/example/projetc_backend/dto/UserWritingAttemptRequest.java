package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserWritingAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Practice Activity ID is required")
        Integer practiceActivityId, // ID của PracticeActivity chứa promptText/transcriptText/expectedOutputText
        @NotBlank(message = "User written text is required")
        String userWrittenText // Văn bản người dùng đã viết/điền
        // BỎ các trường feedback và overallScore khỏi Request. Backend sẽ tự chấm.
) {}