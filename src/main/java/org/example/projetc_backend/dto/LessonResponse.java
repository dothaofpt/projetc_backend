package org.example.projetc_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.example.projetc_backend.entity.Lesson; // Import enum từ entity

public record LessonResponse(
        Integer lessonId,
        String title,
        String description,
        Lesson.Level level, // Sử dụng enum trực tiếp
        Lesson.Skill skill, // Sử dụng enum trực tiếp
        BigDecimal price,
        LocalDateTime createdAt,
        Boolean isDeleted // Bổ sung
) {}