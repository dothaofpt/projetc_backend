package org.example.projetc_backend.dto;

public record QuizResultRequest(
        Integer userId,
        Integer quizId,
        Integer score
) {}