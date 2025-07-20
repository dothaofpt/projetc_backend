package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserWritingAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer practiceActivityId,
        String userWrittenText,
        String grammarFeedback,    // Backend sẽ tạo ra
        String spellingFeedback,   // Backend sẽ tạo ra
        String cohesionFeedback,   // Backend sẽ tạo ra
        Integer overallScore,      // Backend sẽ tính
        LocalDateTime attemptDate,
        // MỚI: Các trường từ PracticeActivity để hiển thị thông tin bài viết gốc/đề bài
        String practiceActivityTitle, // Tiêu đề của PracticeActivity
        String originalPromptText,    // promptText của PracticeActivity
        String expectedOutputText     // expectedOutputText của PracticeActivity (hoặc transcriptText)
) {}