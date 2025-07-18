package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserAnswerRequest(
        @NotNull(message = "Quiz result ID is required")
        Integer quizResultId,
        @NotNull(message = "Question ID is required")
        Integer questionId,
        @NotBlank(message = "User answer text cannot be empty")
        String userAnswerText,
        @NotNull(message = "Correctness status is required")
        Boolean isCorrect // Kết quả chấm điểm từ client (backend có thể chấm lại)
) {}