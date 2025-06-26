package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Quiz;

public record QuizResponse(
        Integer quizId,
        Integer lessonId,
        String title,
        Quiz.Skill skill,
        LocalDateTime createdAt
) {}