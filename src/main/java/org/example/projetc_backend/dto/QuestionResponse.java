package org.example.projetc_backend.dto;

public record QuestionResponse(
        Integer questionId,
        Integer quizId,
        String questionText,
        String type
) {}