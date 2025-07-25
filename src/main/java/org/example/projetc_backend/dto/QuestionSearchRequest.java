package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import org.example.projetc_backend.entity.Question; // Import enum từ entity

public record QuestionSearchRequest(
        Integer quizId,
        String questionText,
        Question.QuestionType questionType, // Sử dụng enum trực tiếp
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public QuestionSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "questionId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}