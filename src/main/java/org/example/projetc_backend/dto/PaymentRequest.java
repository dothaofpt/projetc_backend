package org.example.projetc_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "User ID is required")
        Integer userId,

        @NotNull(message = "Order ID is required for payment")
        Integer orderId,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String paymentMethod,
        String description,
        // BỔ SUNG HAI TRƯỜNG NÀY
        @NotNull(message = "Cancel URL is required for PayPal payment")
        String cancelUrl,
        @NotNull(message = "Success URL is required for PayPal payment")
        String successUrl
) {}