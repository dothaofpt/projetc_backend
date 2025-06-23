package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record ProgressResponse(
        Integer progressId,
        Integer userId,
        Integer lessonId,
        // THAY ĐỔI: Thay thế "Skill" bằng "ActivityType"
        String activityType, // Cập nhật tên trường
        String status,
        Integer completionPercentage,
        LocalDateTime lastUpdated
) {}