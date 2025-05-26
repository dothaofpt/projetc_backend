package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record LessonResponse(
        Integer lessonId,
        String title,
        String description,
        String level,
        String skill,
        LocalDateTime createdAt
) {}