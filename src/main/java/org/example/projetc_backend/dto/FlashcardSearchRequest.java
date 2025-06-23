package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;

public record FlashcardSearchRequest(
        Integer lessonId,
        String word,
        String meaning,
        Boolean isKnown,
        String difficultyLevel,
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public FlashcardSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "wordId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}