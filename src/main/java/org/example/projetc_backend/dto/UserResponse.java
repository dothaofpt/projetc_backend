package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record UserResponse(
        Integer userId,
        String username,
        String email,
        String fullName,
        String avatarUrl,
        LocalDateTime createdAt,
        String role
) {}