package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Integer paymentId,
        UserResponse user, // Bao gồm DTO của User
        Integer orderId,    // THAY ĐỔI: Sử dụng orderId thay vì LessonResponse
        BigDecimal amount,
        LocalDateTime paymentDate,
        String paymentMethod,
        String transactionId,
        Payment.PaymentStatus status, // Enum của PaymentStatus từ entity
        String description
) {}