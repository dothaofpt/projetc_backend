package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Progress;

public record ProgressRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotNull(message = "Activity type is required")
        Progress.ActivityType activityType,
        @NotNull(message = "Status is required")
        Progress.Status status,
        @NotNull(message = "Completion percentage is required")
        Integer completionPercentage
) {}