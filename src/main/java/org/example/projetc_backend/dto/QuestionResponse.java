package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Question; // Import enum từ entity

public record QuestionResponse(
        Integer questionId,
        Integer quizId,
        String questionText,
        Question.QuestionType questionType, // Sử dụng enum trực tiếp
        String audioUrl,
        String imageUrl,
        String correctAnswerText // Bổ sung
) {}