package org.example.projetc_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record OrderRequest(
        @NotNull(message = "User ID is required for an order")
        Integer userId,

        @NotEmpty(message = "Order must contain at least one item")
        @Valid // Quan trọng để validation các item trong list
        List<OrderItemRequest> items
) {}