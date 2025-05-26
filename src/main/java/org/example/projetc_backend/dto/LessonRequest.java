package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Lesson;

public record LessonRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Level is required")
        Lesson.Level level,
        @NotNull(message = "Skill is required")
        Lesson.Skill skill
) {}