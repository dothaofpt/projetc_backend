package org.example.projetc_backend.dto;

import java.math.BigDecimal;

public record OrderDetailResponse(
        Integer orderDetailId,
        Integer orderId, // ID của order cha
        LessonResponse lesson, // Bao gồm Lesson DTO
        Integer quantity,
        BigDecimal priceAtPurchase
) {}