package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderDetailRequest(
        @NotNull(message = "Order ID không được để trống")
        Integer orderId,

        @NotNull(message = "Lesson ID không được để trống")
        Integer lessonId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
        Integer quantity,

        @NotNull(message = "Giá tại thời điểm mua không được để trống")
        @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
        BigDecimal priceAtPurchase
) {}