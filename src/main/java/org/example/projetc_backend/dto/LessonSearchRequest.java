package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

public record LessonSearchRequest(
        String title,
        String level,
        String skill,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public LessonSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "lessonId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}