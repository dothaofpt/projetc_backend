// VocabularySearchRequest
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import org.example.projetc_backend.entity.Vocabulary;

public record VocabularySearchRequest(
        String word,
        String meaning,
        Vocabulary.DifficultyLevel difficultyLevel,
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public VocabularySearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "wordId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}