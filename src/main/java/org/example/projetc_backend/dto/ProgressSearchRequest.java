// src/main/java/org/example/projetc_backend/dto/ProgressSearchRequest.java
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import org.example.projetc_backend.entity.Progress;

public record ProgressSearchRequest(
        Integer userId,
        Integer lessonId,
        Progress.ActivityType activityType,
        Progress.Status status,
        Integer minCompletionPercentage, // <-- Thêm vào (nếu chưa có)
        Integer maxCompletionPercentage, // <-- Thêm vào (nếu chưa có)
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public ProgressSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "progressId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}