package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Order;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Integer orderId,
        UserResponse user, // Bao gồm User DTO
        LocalDateTime orderDate,
        BigDecimal totalAmount,
        Order.OrderStatus status,
        String shippingAddress, // Nếu có
        List<OrderDetailResponse> items // Danh sách các chi tiết đơn hàng
) {}