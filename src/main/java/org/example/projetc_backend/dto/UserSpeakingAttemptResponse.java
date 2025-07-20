package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserSpeakingAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer practiceActivityId,
        String userAudioUrl,
        String userTranscribedBySTT, // Backend sẽ chuyển đổi và lưu
        Integer pronunciationScore,   // Backend sẽ tính
        Integer fluencyScore,         // Backend sẽ tính
        Integer overallScore,         // Backend sẽ tính
        LocalDateTime attemptDate,
        // MỚI: Các trường từ PracticeActivity để hiển thị thông tin bài nói gốc
        String practiceActivityTitle, // Tiêu đề của PracticeActivity
        String originalPromptText,    // promptText của PracticeActivity
        String expectedOutputText     // expectedOutputText của PracticeActivity
) {}