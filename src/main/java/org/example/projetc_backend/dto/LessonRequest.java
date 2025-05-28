 package org.example.projetc_backend.dto;

import jakarta.validation.constraints.DecimalMin; // Import này là cần thiết
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Lesson;
import java.math.BigDecimal; // Import này là cần thiết

public record LessonRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        @NotNull(message = "Level is required")
        Lesson.Level level,
        @NotNull(message = "Skill is required")
        Lesson.Skill skill,
        // THAY ĐỔI MỚI: Thêm trường price
        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.00", message = "Price cannot be negative")
        BigDecimal price
) {}