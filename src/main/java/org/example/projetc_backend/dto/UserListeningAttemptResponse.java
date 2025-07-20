package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserListeningAttemptResponse(
        Integer attemptId,
        Integer userId,
        Integer practiceActivityId,
        String userTranscribedText,
        Integer accuracyScore, // Backend sẽ tính và trả về
        LocalDateTime attemptDate,
        // MỚI: Các trường từ PracticeActivity để hiển thị thông tin bài nghe gốc
        String practiceActivityTitle, // Tiêu đề của PracticeActivity
        String audioMaterialUrl,      // materialUrl của PracticeActivity (là audio)
        String actualTranscriptText   // transcriptText của PracticeActivity (là đáp án đúng)
) {}