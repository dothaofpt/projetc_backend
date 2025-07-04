package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Quiz; // Import the Quiz entity

public record QuizResponse(
        Integer quizId,
        Integer lessonId,
        String title,
        // Removed Quiz.Skill skill
        Quiz.QuizType quizType, // Added the new QuizType enum
        LocalDateTime createdAt
) {}