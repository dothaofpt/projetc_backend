package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Pattern;

public record AnswerSearchRequest(
        Integer questionId,
        Boolean isCorrect,
        Boolean isActive,
        String answerText,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
    public AnswerSearchRequest {
        // Default values for pagination and sorting if not provided
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "answerId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}