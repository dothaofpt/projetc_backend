package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Question; // Import enum từ entity
import java.util.List; // MỚI: Import List

public record QuestionResponse(
        Integer questionId,
        Integer quizId,
        String questionText,
        Question.QuestionType questionType, // Sử dụng enum trực tiếp
        String audioUrl,
        String imageUrl,
        String correctAnswerText, // Bổ sung
        List<AnswerResponse> answers // MỚI: Danh sách các câu trả lời liên quan
) {}