package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import java.math.BigDecimal; // Import này là cần thiết

public record LessonResponse(
        Integer lessonId,
        String title,
        String description,
        String level,
        String skill,
        // THAY ĐỔI MỚI: Thêm trường price
        BigDecimal price,
        LocalDateTime createdAt,
        Integer durationMonths
) {}