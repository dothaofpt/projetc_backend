package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record UserSpeakingAttemptRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Practice Activity ID is required")
        Integer practiceActivityId,
        @NotBlank(message = "User audio URL is required")
        String userAudioUrl, // URL của file ghi âm giọng nói người dùng
        // MỚI: Thêm lại trường này. Frontend sẽ gửi nó (có thể là kết quả STT hoặc văn bản người dùng gõ)
        String userTranscribedBySTT
        // BỎ pronunciationScore, fluencyScore, overallScore khỏi Request. Backend sẽ tự chấm.
) {}