package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record ProgressResponse(
        Integer progressId,
        Integer userId,
        Integer lessonId,
        String skill,
        String status,
        Integer completionPercentage,
        LocalDateTime lastUpdated
) {}