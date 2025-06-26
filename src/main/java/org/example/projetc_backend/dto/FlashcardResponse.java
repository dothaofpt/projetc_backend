package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Vocabulary; // Import enum từ entity

public record FlashcardResponse(
        Integer userFlashcardId, // Thay đổi id thành userFlashcardId để rõ ràng
        Integer userId,
        Integer wordId,
        String word,
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String imageUrl, // Bổ sung
        String writingPrompt,
        Vocabulary.DifficultyLevel difficultyLevel, // Sử dụng enum trực tiếp
        Boolean isKnown,
        LocalDateTime lastReviewedAt, // Bổ sung
        LocalDateTime nextReviewAt,   // Bổ sung
        Integer reviewIntervalDays,   // Bổ sung
        Double easeFactor            // Bổ sung
) {}