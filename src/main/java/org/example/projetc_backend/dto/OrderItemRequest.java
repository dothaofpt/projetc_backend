package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderItemRequest(
        @NotNull(message = "Lesson ID is required for an order item")
        @Positive(message = "Lesson ID must be positive")
        Integer lessonId,

        @NotNull(message = "Quantity is required for an order item")
        @Positive(message = "Quantity must be positive")
        Integer quantity // Có thể luôn là 1 cho Lesson, nhưng giữ lại để linh hoạt
) {}