package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Progress;

public record ProgressResponse(
        Integer progressId,
        Integer userId,
        Integer lessonId,
        Progress.ActivityType activityType,
        Progress.Status status,
        Integer completionPercentage,
        LocalDateTime lastUpdated
) {}