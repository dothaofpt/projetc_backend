package org.example.projetc_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PaymentRequest(
        @NotNull(message = "User ID is required")
        Integer userId,

        @NotNull(message = "Order ID is required for payment") // THAY ĐỔI: liên kết với OrderId
        Integer orderId,

        // Amount có thể không cần thiết nếu backend tự tính từ Order
        // Nhưng nếu muốn frontend gửi lên để kiểm tra, thì vẫn giữ
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        String paymentMethod,
        String description
) {}