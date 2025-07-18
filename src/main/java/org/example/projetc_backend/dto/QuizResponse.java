package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Quiz; // Import the Quiz entity

public record QuizResponse(
        Integer quizId,
        Integer lessonId,
        String title,
        Quiz.QuizType quizType, // Added the new QuizType enum
        LocalDateTime createdAt,
        String lessonTitle // <--- THÊM DÒNG NÀY ĐỂ TRẢ VỀ TÊN BÀI HỌC
) {}