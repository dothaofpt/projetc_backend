package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Progress; // Đảm bảo import đúng entity Progress

public record ProgressRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotNull(message = "Activity type is required")
        Progress.ActivityType activityType, // Sẽ tự động dùng enum ActivityType đã cập nhật từ Progress entity
        @NotNull(message = "Status is required")
        Progress.Status status,           // Sẽ tự động dùng enum Status đã cập nhật từ Progress entity
        @NotNull(message = "Completion percentage is required")
        Integer completionPercentage
) {}