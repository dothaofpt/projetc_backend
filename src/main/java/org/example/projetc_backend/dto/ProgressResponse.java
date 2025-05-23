package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProgressResponse(
        Integer progressId,
        Integer userId,
        Integer lessonId,
        String status,
        String lastUpdated
) {}