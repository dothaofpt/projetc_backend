package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonResponse(
        Integer lessonId,
        String title,
        String description,
        String level,
        String createdAt
) {}

