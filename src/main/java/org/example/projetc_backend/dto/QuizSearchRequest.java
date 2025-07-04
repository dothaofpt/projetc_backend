package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import org.example.projetc_backend.entity.Quiz; // Đảm bảo import enum từ entity

public record QuizSearchRequest(
        Integer lessonId,
        String title,
        Quiz.QuizType quizType, // ĐÃ SỬA: Sử dụng Quiz.QuizType thay vì Quiz.Skill
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public QuizSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "quizId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}