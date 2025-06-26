package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Question; // Import enum từ entity

public record QuestionRequest(
        @NotNull(message = "Quiz ID is required")
        Integer quizId,
        @NotBlank(message = "Question text is required")
        String questionText,
        @NotNull(message = "Question type is required")
        Question.QuestionType questionType, // Sử dụng enum trực tiếp
        String audioUrl, // Bổ sung
        String imageUrl, // Bổ sung
        String correctAnswerText // Bổ sung (cho các loại câu hỏi không có Answer riêng)
) {}