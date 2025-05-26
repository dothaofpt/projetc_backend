package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record QuizRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Title is required")
        String title,
        @NotNull(message = "Skill is required")
        @Pattern(regexp = "LISTENING|SPEAKING|READING|WRITING|VOCABULARY|GRAMMAR", message = "Skill must be one of: LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR")
        String skill
) {}