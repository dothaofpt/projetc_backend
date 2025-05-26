package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record QuizResponse(
        Integer quizId,
        Integer lessonId,
        String title,
        String skill,
        LocalDateTime createdAt
) {}