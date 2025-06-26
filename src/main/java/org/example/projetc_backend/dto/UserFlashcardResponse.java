package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserFlashcardResponse(
        Integer id,
        Integer userId,
        Integer wordId,
        Boolean isKnown,
        LocalDateTime lastReviewedAt,
        LocalDateTime nextReviewAt,
        Integer reviewIntervalDays,
        Double easeFactor
) {}